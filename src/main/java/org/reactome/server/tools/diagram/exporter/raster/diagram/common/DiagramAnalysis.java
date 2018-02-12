package org.reactome.server.tools.diagram.exporter.raster.diagram.common;

import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.exception.ResourceGoneException;
import org.reactome.server.analysis.core.result.exception.ResourceNotFoundException;
import org.reactome.server.analysis.core.result.model.*;
import org.reactome.server.tools.diagram.data.graph.EntityNode;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.graph.GraphNode;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.exporter.common.AnalysisClient;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableNode;

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
	private Map<Long, DiagramObject> diagramIndex;
	private Map<Long, EntityNode> graphIndex;

	private AnalysisType type = null;

	private AnalysisStoredResult asr;

	public DiagramAnalysis(String token, DiagramIndex index, RasterArgs args, Graph graph, Diagram diagram) throws Exception {
		this(index, args, graph, diagram);
		try {
			if (token != null)
				this.asr = AnalysisClient.token.getFromToken(token);
		} catch (ResourceGoneException | ResourceNotFoundException e) {
			throw new Exception(e);
		}
		initialise();
	}

	public DiagramAnalysis(AnalysisStoredResult asr, DiagramIndex index, RasterArgs args, Graph graph, Diagram diagram) {
		this(index, args, graph, diagram);
		this.asr = asr;
		initialise();
	}

	private DiagramAnalysis(DiagramIndex index, RasterArgs args, Graph graph, Diagram diagram) {
		this.index = index;
		this.args = args;
		this.graph = graph;
		this.diagram = diagram;
	}

	private void initialise() {
		index();
		addAnalysisData();
		clearIndex();
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
		if (args.getToken() == null || args.getToken().isEmpty())
			return;
//		final List<ResourceSummary> summaryList = result.getResourceSummary();
		final List<ResourceSummary> summaryList = asr.getResourceSummary();
		final ResourceSummary resourceSummary = summaryList.size() == 2
				? summaryList.get(1)
				: summaryList.get(0);
		// result.getSummary().getFileName() seems to be null
//		analysisName = result.getSummary().getSampleName();
		String resource = args.getResource() == null
				? resourceSummary.getResource()
				: args.getResource();
		type = AnalysisType.getType(asr.getSummary().getType());
		// Get subpathways (green boxes) % of analysis area
		subPathways(resource);
		final FoundElements foundElements = asr.getFoundElmentsForPathway(args.getStId(), resource);
		if (foundElements == null) return;
		if (type == AnalysisType.EXPRESSION) {
			expression(foundElements);
		} else if (type == AnalysisType.OVERREPRESENTATION ||
				type == AnalysisType.SPECIES_COMPARISON)
			enrichment(foundElements);
	}

	/**
	 * Adds to the analysis index the subPathways present in the diagram (as
	 * ProcessNodes). So for each it takes its PathwaySummary and divides
	 * entities.getFound() / entities.getTotal() to compute the percentage of
	 * the fill area.
	 */
	private void subPathways(String resource) {
		// 1 extract list of dbIds for ProcessNodes
		final List<String> subPathways = diagram.getNodes().stream()
				.filter(node -> node.getRenderableClass().equals("ProcessNode"))
				.map(DiagramObject::getReactomeId)
				.map(String::valueOf)
				.collect(Collectors.toList());
		if (subPathways.isEmpty()) return;
		// 2 get subPathways summary
//		final PathwaySummary[] pathwaysSummary = AnalysisClient.getPathwaysSummary(subPathways, token, resource);
		final List<PathwaySummary> pathwaysSummary = asr.filterByPathways(subPathways, resource);
		// extract %
		for (PathwaySummary summary : pathwaysSummary) {
			final EntityStatistics entities = summary.getEntities();
			if (entities == null) continue;
			final DiagramObject diagramNode = diagramIndex.get(summary.getDbId());
			if (diagramNode == null) continue;
			int found = entities.getFound();
			int total = entities.getTotal();
			double percentage = (double) found / total;
			if (percentage < MIN_VISIBLE_ENRICHMENT && percentage > 0)
				percentage = MIN_VISIBLE_ENRICHMENT;
			final RenderableNode node = index.getNode(diagramNode.getId());
			node.setEnrichment(percentage);
			node.setExpressionValue(getMedian(entities.getExp()));
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
			final RenderableNode renderableNode = index.getNode(diagramNode.getId());
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
				.forEach(diagramNode -> {
					final EntityNode graphNode = graphIndex.get(diagramNode.getReactomeId());
					if (graphNode != null) {
						double percentage = getPercentage(graphNodeHit, graphNode);
						index.getNode(diagramNode.getId()).setEnrichment(percentage);
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
		return asr.getSummary().getSampleName();
	}

	public AnalysisStoredResult getResult() {
		return asr;
	}

}
