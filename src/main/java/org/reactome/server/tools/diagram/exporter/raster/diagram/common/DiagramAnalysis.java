package org.reactome.server.tools.diagram.exporter.raster.diagram.common;

import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.model.*;
import org.reactome.server.tools.diagram.data.graph.EntityNode;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.graph.GraphNode;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableNode;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableProcessNode;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	private final Diagram diagram;
	private final AnalysisType type;
	private Map<Long, DiagramObject> diagramIndex;
	private Map<Long, EntityNode> graphIndex;
	private AnalysisStoredResult result;
	private String resource;
	private AnalysisResult summary;

	DiagramAnalysis(AnalysisStoredResult result, DiagramIndex index, RasterArgs args, Graph graph, Diagram diagram) {
		this.index = index;
		this.args = args;
		this.graph = graph;
		this.diagram = diagram;
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
		diagramIndex = null;
		graphIndex = null;
	}

	private void index() {
		// Indexes to map layout <-> graph
		diagramIndex = new HashMap<>();
		Stream.of(diagram.getEdges(), diagram.getNodes(), diagram.getLinks())
				.flatMap(Collection::stream)
				.forEach(item -> diagramIndex.put(item.getReactomeId(), item));
		graphIndex = new HashMap<>();
		graph.getNodes().forEach(item -> graphIndex.put(item.getDbId(), item));
	}

	/**
	 * Extracts analysis information and attaches it to each diagram node.
	 */
	private void addAnalysisData() {
		this.summary = result.getResultSummary(resource);
		// Get subpathways (green boxes) % of analysis area
		subPathways();
		foundElements();
	}

	private void foundElements() {
		final FoundElements foundElements = result.getFoundElmentsForPathway(args.getStId(), resource);
		if (foundElements == null) return;
		if (type == AnalysisType.EXPRESSION) expression(foundElements);
		else if (type == AnalysisType.OVERREPRESENTATION ||
				type == AnalysisType.SPECIES_COMPARISON)
			enrichment(foundElements);
	}

	/**
	 * Adds to the analysis index the subPathways present in the diagram (as
	 * ProcessNodes). So for each it takes its PathwaySummary and divides
	 * entities.getFound() / entities.getTotal() to compute the percentage of
	 * the fill area.
	 */
	private void subPathways() {
		final Map<Long, List<RenderableProcessNode>> subPathways = index.getDiagramObjectsById().values().stream()
				.filter(RenderableProcessNode.class::isInstance)
				.map(RenderableProcessNode.class::cast)
				.collect(Collectors.groupingBy(o -> o.getDiagramObject().getReactomeId()));
		final List<PathwaySummary> summaries = result.filterByPathways(subPathways.keySet().stream().map(String::valueOf).collect(Collectors.toList()), resource);
		for (PathwaySummary summary : summaries) {
			final EntityStatistics entities = summary.getEntities();
			if (entities == null) continue;
			int found = entities.getFound();
			int total = entities.getTotal();
			double percentage = (double) found / total;
			if (percentage < MIN_VISIBLE_ENRICHMENT && percentage > 0) {
				percentage = MIN_VISIBLE_ENRICHMENT;
			}
			final Double median = getMedian(entities.getExp());
			for (RenderableProcessNode node : subPathways.get(summary.getDbId())) {
				node.setEnrichment(percentage);
				node.setExpressionValue(median);  // TODO: 19/07/18 how subpathways behave in expression
			}
		}
	}

	private Double getMedian(List<Double> exp) {
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
	private void expression(FoundElements foundElements) {
		// analysis -> graph: analysis.mapsTo.id.contains(graph.identifier)
		// graph -> layout:   layout.reactomeId == graph.dbId
		// Index analysis nodes via mapsTo
		final Map<String, FoundEntity> analysisIndex = new HashMap<>();
		foundElements.getEntities().forEach(analysisNode ->
				analysisNode.getMapsTo().stream()
						.map(IdentifierMap::getIds)
						.flatMap(Collection::stream)
						.forEach(id -> analysisIndex.put(id, analysisNode)));
		diagram.getNodes().forEach(diagramNode -> {
			final EntityNode graphNode = graphIndex.get(diagramNode.getReactomeId());
			if (graphNode == null) return;
			final List<FoundEntity> leaves = getLeaves(graphNode).stream()
					.map(leafId -> analysisIndex.get(graphIndex.get(leafId).getIdentifier()))
					.collect(Collectors.toList());
			final RenderableNode renderableNode = (RenderableNode) index.getDiagramObjectsById().get(diagramNode.getId());
			renderableNode.setHitExpressions(leaves);
		});
	}

	/** Computes only the relation of hit found components and found component */
	private void enrichment(FoundElements foundElements) {
		// analysis -> graph: analysis.mapsTo.ids.contains(graph.identifier)
		final Set<String> identifiers = foundElements.getEntities().stream()
				.map(FoundEntity::getMapsTo)
				.flatMap(Collection::stream)
				.map(IdentifierMap::getIds)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
		// graphNode.getIdentifier() -> graphNode.getDbId()
		final Set<Long> graphNodeHit = graph.getNodes().stream()
				.filter(entityNode -> identifiers.contains(entityNode.getIdentifier()))
				.map(GraphNode::getDbId)
				.collect(Collectors.toSet());
		// run through the diagram nodes and compute the enrichment level for
		// its associated graph node.
		diagram.getNodes().stream()
				.filter(node -> !node.getRenderableClass().equals("ProcessNode"))
				.filter(node -> !node.getRenderableClass().equals("EncapsulatedNode"))
				.forEach(diagramNode -> {
					final EntityNode graphNode = graphIndex.get(diagramNode.getReactomeId());
					if (graphNode != null) {
						double percentage = getPercentage(graphNodeHit, graphNode);
						final RenderableNode renderableNode = (RenderableNode) index.getDiagramObjectsById().get(diagramNode.getId());
						renderableNode.setEnrichment(percentage);
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
		if (node.getChildren() == null) {
			return Collections.singleton(node.getDbId());
		} else {
			return node.getChildren().stream()
					.map(graphIndex::get)
					.filter(Objects::nonNull)
					.map(this::getLeaves)
					.flatMap(Collection::stream)
					.collect(Collectors.toSet());
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
