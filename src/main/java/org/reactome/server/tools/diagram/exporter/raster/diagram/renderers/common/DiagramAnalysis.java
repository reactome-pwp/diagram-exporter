package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common;

import org.reactome.server.tools.diagram.data.graph.EntityNode;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.graph.GraphNode;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisClient;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.*;
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
	private AnalysisResult result;

	private AnalysisType type = AnalysisType.NONE;
	private String analysisName;

	DiagramAnalysis(DiagramIndex index, RasterArgs args, Graph graph, Diagram diagram) throws AnalysisException, AnalysisServerError {
		this.index = index;
		this.args = args;
		this.graph = graph;
		this.diagram = diagram;
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
	private void addAnalysisData() throws AnalysisServerError, AnalysisException {
		if (args.getToken() == null || args.getToken().isEmpty())
			return;
		final String stId = graph.getStId();
		result = AnalysisClient.getAnalysisResult(args.getToken());
		final List<ResourceSummary> summaryList = result.getResourceSummary();
		final ResourceSummary resourceSummary = summaryList.size() == 2
				? summaryList.get(1)
				: summaryList.get(0);
		// result.getSummary().getFileName() seems to be null
		analysisName = result.getSummary().getSampleName();
		String resource = args.getResource() == null
				? resourceSummary.getResource()
				: args.getResource();
		type = AnalysisType.getType(result.getSummary().getType());
		// Get subpathways (green boxes) % of analysis area
		subPathways(args.getToken(), resource);

		try {
			final FoundElements foundElements = AnalysisClient.getFoundElements(stId, args.getToken(), resource);
			if (foundElements == null) return;
			if (type == AnalysisType.EXPRESSION) {
				expression(foundElements);
			} else if (type == AnalysisType.OVERREPRESENTATION ||
					type == AnalysisType.SPECIES_COMPARISON)
				enrichment(foundElements);
		} catch (AnalysisException e) {
			// token is valid, but this pathway has no analysis
		}
	}

	/**
	 * Adds to the analysis index the subPathways present in the diagram (as
	 * ProcessNodes). So for each it takes its PathwaySummary and divides
	 * entities.getFound() / entities.getTotal() to compute the percentage of
	 * the fill area.
	 */
	private void subPathways(String token, String resource) throws AnalysisServerError, AnalysisException {
		// 1 extract list of dbIds for ProcessNodes
		final List<String> subPathways = diagram.getNodes().stream()
				.filter(node -> node.getRenderableClass().equals("ProcessNode"))
				.map(DiagramObject::getReactomeId)
				.map(String::valueOf)
				.collect(Collectors.toList());
		if (subPathways.isEmpty())
			return;
		// 2 get subPathways summary
		final PathwaySummary[] pathwaysSummary = AnalysisClient.getPathwaysSummary(subPathways, token, resource);
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
			index.getNode(diagramNode.getId()).setEnrichment(percentage);
		}
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
		return analysisName;
	}

	public AnalysisResult getResult() {
		return result;
	}

}
