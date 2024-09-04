package org.reactome.server.tools.diagram.exporter.raster.diagram.common;

import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.model.*;
import org.reactome.server.tools.diagram.data.graph.EntityNode;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableDiagramObject;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableNode;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableProcessNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Computes the analysis data of the diagram. Adds to the DiagramIndex the
 * analysis info of each node. If the analysis is an ENRICHMENT or
 * SPECIES_COMPARISON, adds to each RenderableNode the percentage covered by the
 * analysis. If it is an EXPRESSION analysis, adds the hitExpression values. If
 * there is no analysis, it doesn't add anything.
 */
public class DiagramAnalysis {

	public static final double MIN_ENRICHMENT = 0.05;
	private static final double MIN_VISIBLE_ENRICHMENT = 0.05;
	private final DiagramIndex index;
	private final RasterArgs args;
	private final Graph graph;
	private final AnalysisType type;
	private Map<Long, EntityNode> graphIndex;
	private AnalysisStoredResult result;
	private String resource;
	private AnalysisResult summary;

	/**
	 * Instantiates a DiagramAnalysis. It will calculate values for nodes in case there is an analysis.
	 *  @param index   diagram index
	 * @param graph   the underlying graph
	 * @param args    raster arguments
	 * @param result  analysis results
	 */
	DiagramAnalysis(DiagramIndex index, Graph graph, RasterArgs args, AnalysisStoredResult result) {
		this.index = index;
		this.args = args;
		this.graph = graph;
		this.result = result;
		this.type = result == null ? null : AnalysisType.getType(result.getSummary().getType());
		this.resource = getResource();
		initialise();
	}

	@SuppressWarnings("Duplicates")
	private String getResource() {
		if (args.getResource() != null) return args.getResource();
		if (result == null) return null;
		final List<ResourceSummary> summaryList = result.getResourceSummary();
		final ResourceSummary resourceSummary = summaryList.size() == 2
				? summaryList.get(1)
				: summaryList.get(0);
		// result.getSummary().getFileName() seems to be null
//		analysisName = result.getSummary().getSampleName();
		return resourceSummary.getResource();
	}

	private void initialise() {
		if (result != null) {
			index();
			addAnalysisData();
			clearIndex();
		}
	}

	private void clearIndex() {
		graphIndex = null;
	}

	private void index() {
		// Indexes to map layout <-> graph. They share dbId
		graphIndex = new HashMap<>();
		graph.getNodes().forEach(item -> graphIndex.put(item.getDbId(), item));
	}

	/**
	 * Extracts analysis information and attaches it to each diagram node.
	 */
	private void addAnalysisData() {
		this.summary = result.getResultSummary(resource, args.isImportableOnly());
		// Get subpathways (green boxes) % of analysis area
		addSubPathwaysData();
		addNodesData();
	}

	private void addNodesData() {
		final FoundElements foundElements = result.getFoundElmentsForPathway(args.getStId(), resource);
		if (foundElements == null) return;
		switch (type) {
			case SPECIES_COMPARISON:
			case OVERREPRESENTATION:
				addEnrichmentData(foundElements);
				break;
			case EXPRESSION:
			case GSA_STATISTICS:
			case GSVA:
			case GSA_REGULATION:
				addExpressionData(foundElements);
				break;
		}
	}

	/**
	 * Adds to the analysis index the subPathways present in the diagram (as
	 * ProcessNodes). So for each it takes its PathwaySummary and divides
	 * entities.getFound() / entities.getTotal() to compute the percentage of
	 * the fill area.
	 */
	private void addSubPathwaysData() {
		// Just a reminder: the same subpathway can appear twice in the diagram
		// filterByPathway needs ids as strings
		final List<String> ids = new ArrayList<>();
		for (Long dbId : index.getPathwaysByReactomeId().keySet()) ids.add(String.valueOf(dbId));
		final List<PathwaySummary> summaries = result.filterByPathways(ids, resource, args.isImportableOnly());

		for (PathwaySummary summary : summaries) {
			final EntityStatistics entities = summary.getEntities();
			if (entities == null) continue;
			int found = entities.getFound();
			int total = entities.getTotal();
			double percentage = (double) found / total;
			if (percentage < MIN_VISIBLE_ENRICHMENT && percentage > 0) {
				percentage = MIN_VISIBLE_ENRICHMENT;
			}
			final Double median = computeMedian(entities.getExp());
			for (RenderableProcessNode node : index.getPathwaysByReactomeId().get(summary.getDbId())) {
				node.setEnrichmentPercentage(percentage);
				node.setEnrichmentValue(median);  // TODO: 19/07/18 how subpathways behave in expression
			}
		}
	}

	private Double computeMedian(List<Double> exp) {
		if (exp.isEmpty()) return null;
		if (exp.size() == 1) return exp.get(0);
		// Avoid modifying original list
		final List<Double> numbers = new ArrayList<>(exp);
		Collections.sort(numbers);
		int half = numbers.size() / 2;
		if (numbers.size() % 2 == 0)
			return 0.5 * (numbers.get(half - 1) + numbers.get(half));
		return numbers.get(half);
	}

	/**
	 * Computes the list of expressions of components. For each diagram object,
	 * except ProcessNodes, you get a list of lists of doubles
	 */
	private void addExpressionData(FoundElements foundElements) {
		// There is no direct mapping diagram <-> analysis, so we map through graph
		// analysis -> graph: analysis.mapsTo.id.contains(graph.identifier)
		// graph -> layout:   layout.reactomeId == graph.dbId
		// Index analysis nodes via mapsTo
		final Map<String, FoundEntity> analysisIndex = new HashMap<>();
		for (FoundEntity analysisNode : foundElements.getEntities()) {
			for (IdentifierMap identifierMap : analysisNode.getMapsTo()) {
				for (String id : identifierMap.getIds()) {
					analysisIndex.put(id, analysisNode);
				}
			}
		}
		index.getNodesByReactomeId().forEach((id, objects) -> {
			final EntityNode graphNode = graphIndex.get(id);
			if (graphNode == null) return;
			final List<FoundEntity> leaves = new ArrayList<>();
			for (Long leafId : getLeaves(graphNode)) {
				leaves.add(analysisIndex.get(graphIndex.get(leafId).getIdentifier()));
			}
			for (RenderableDiagramObject object : objects) {
				final RenderableNode renderableNode = (RenderableNode) object;
				renderableNode.setHitExpressions(leaves);
			}
		});
	}

	/**
	 * Computes only the relation of hit found components and found component
	 */
	private void addEnrichmentData(FoundElements foundElements) {
		// analysis -> graph: analysis.mapsTo.ids.contains(graph.identifier)
		final Set<String> identifiers = foundElements.getEntities().stream()
				.flatMap(foundEntity -> foundEntity.getMapsTo().stream())
				.flatMap(identifierMap -> identifierMap.getIds().stream())
				.collect(Collectors.toSet());
		// graphNode.getIdentifier() -> graphNode.getDbId()
		final Set<Long> graphNodeHit = new HashSet<>();
		for (EntityNode entityNode : graph.getNodes())
			if (identifiers.contains(entityNode.getIdentifier()))
				graphNodeHit.add(entityNode.getDbId());
		// run through the diagram nodes and compute the enrichment level for
		// its associated graph node.
		index.getNodesByReactomeId().forEach((id, objects) -> {
			final EntityNode graphNode = graphIndex.get(id);
			if (graphNode != null) {
				double percentage = getPercentage(graphNodeHit, graphNode);
				for (RenderableDiagramObject object : objects) {
					final RenderableNode renderableNode = (RenderableNode) object;
					renderableNode.setEnrichmentPercentage(percentage);
				}
			}
		});
	}

	private double getPercentage(Set<Long> hitIds, EntityNode graphNode) {
		final Set<Long> leaves = getLeaves(graphNode);
		final int total = leaves.size();
		final long count = leaves.stream().filter(hitIds::contains).count();
		double percentage = (double) count / total;
		if (percentage > 0 && percentage < MIN_VISIBLE_ENRICHMENT)
			percentage = MIN_VISIBLE_ENRICHMENT;
		return percentage;
	}

	private Set<Long> getLeaves(EntityNode node) {
		if (node.getChildren() == null || node.getChildren().isEmpty()) {
			return Collections.singleton(node.getDbId());
		} else {
			final Set<Long> set = new HashSet<>();
			for (Long dbId : node.getChildren()) {
				final EntityNode entityNode = graphIndex.get(dbId);
				if (entityNode != null)
					set.addAll(getLeaves(entityNode));
			}
			return set;
		}
	}

	public AnalysisType getType() {
		return type;
	}

	public String getAnalysisName() {
		return result.getSummary().getSampleName();
	}

	public AnalysisResult getResult() {
		return summary;
	}

}
