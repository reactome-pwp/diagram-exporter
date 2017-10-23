package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import org.reactome.server.tools.diagram.data.graph.EntityNode;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.graph.GraphNode;
import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisClient;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Computes all the information that modifies each node basic rendering:
 * selection, flag, halo and analysis (enrichments and expressions). This data
 * is not in the Node class.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class DiagramIndex {

	private static final double MIN_ENRICHMENT = 0.05;
	private final Diagram diagram;
	private final Graph graph;
	private final HashMap<Long, DiagramObjectDecorator> index;
	private final Decorator decorator;
	private final String token;

	/**
	 * [diagram id] diagramNode.getId()
	 */
	private Map<Long, DiagramObject> diagramIndex;
	/**
	 * [reactome id] diagramNode.getReactomeId(), graphNode.getDbId()
	 */
	private Map<Long, DiagramObject> reactomeIndex;
	/**
	 * [reactome id] graphNode.getDbId()
	 */
	private Map<Long, EntityNode> graphIndex;

	private double maxExpression = Double.MAX_VALUE;
	private double minExpression = 0;
	private AnalysisType analysisType = AnalysisType.NONE;

	/**
	 * Creates a new DiagramIndex with the information for each node in maps.
	 *
	 * @param diagram   diagram with nodes and reactions
	 * @param graph     background graph
	 * @param decorator decorator with selection and flags
	 */
	public DiagramIndex(Diagram diagram, Graph graph, Decorator decorator, String token) {
		this.diagram = diagram;
		this.graph = graph;
		this.decorator = decorator;
		this.token = token;
		this.index = new HashMap<>();
		index();
		collect();
	}

	private void index() {
		diagramIndex = new HashMap<>();
		reactomeIndex = new HashMap<>();
		Stream.of(diagram.getEdges(), diagram.getNodes(), diagram.getLinks())
				.flatMap(Collection::stream)
				.forEach(item -> {
					diagramIndex.put(item.getId(), item);
					reactomeIndex.put(item.getReactomeId(), item);
				});
		graphIndex = new HashMap<>();
		graph.getNodes().forEach(item -> graphIndex.put(item.getDbId(), item));
		diagram.getNodes().stream()
				.map(Node::getConnectors)
				.flatMap(Collection::stream)
				.forEach(connector -> getEdgeDecorator(connector.getEdgeId()).getConnectors().add(connector));
	}

	private void collect() {
		if (decorator != null) {
			collectNodes();
			collectReactions();
			collectAnalysis();
		}
	}


	private void collectNodes() {
		diagram.getNodes().forEach(node -> {
			// Select node
			if (decorator.getSelected().contains(node.getReactomeId())) {
				final NodeDecorator decorator = getNodeDecorator(node.getId());
				decorator.setSelected(true);
				decorator.setHalo(true);
				node.getConnectors().forEach(connector -> {
					final Edge reaction = (Edge) diagramIndex.get(connector.getEdgeId());
					// When a node is selected, the nodes in the same reaction
					// are haloed
					haloEdgeParticipants(reaction);
				});
			}
			// Flag node
			if (decorator.getFlags().contains(node.getReactomeId()))
				getNodeDecorator(node.getId()).setFlag(true);
		});
	}

	private void collectReactions() {
		diagram.getEdges().forEach(reaction -> {
			// Select edge
			if (decorator.getSelected().contains(reaction.getReactomeId())) {
				final EdgeDecorator decorator = getEdgeDecorator(reaction.getId());
				decorator.setSelected(true);
				decorator.setHalo(true);
				haloEdgeParticipants(reaction);
			}
			// Flag edge
			if (decorator.getFlags().contains(reaction.getReactomeId()))
				getEdgeDecorator(reaction.getId()).setFlag(true);
		});
	}


	/**
	 * Adds the reaction to the haloReaction set, participating nodes to
	 * haloNodes and participating connectors to haloConnectors
	 *
	 * @param reaction reaction to halo
	 */
	private void haloEdgeParticipants(Edge reaction) {
		streamParticipants(reaction)
				.filter(node -> node.getIsFadeOut() == null || !node.getIsFadeOut())
				.forEach(node -> getNodeDecorator(node.getId()).setHalo(true));
	}

	/**
	 * Very convenient method to iterate over all of the activators, catalysts,
	 * inhibitors, inputs and outputs in a unique stream as diagram Nodes
	 *
	 * @param edge the chosen one
	 *
	 * @return a stream with all participant nodes in the reaction
	 */
	private Stream<Node> streamParticipants(Edge edge) {
		return Stream.of(edge.getActivators(), edge.getCatalysts(),
				edge.getInhibitors(), edge.getInputs(), edge.getOutputs())
				.filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.map(part -> diagramIndex.get(part.getId()))
				.map(Node.class::cast);
	}

	/**
	 * Extracts analysis information and attaches it to each diagram node.
	 */
	private void collectAnalysis() {
		if (token == null || token.isEmpty())
			return;
		final String stId = graph.getStId();
		System.out.println(token); // TODO: delete on production
		System.out.println(stId);
		try {
			final AnalysisResult result = AnalysisClient.getAnalysisResult(token);
			final List<ResourceSummary> summaryList = result.getResourceSummary();
			final ResourceSummary resourceSummary = summaryList.size() == 2
					? summaryList.get(1)
					: summaryList.get(0);
			final String resource = resourceSummary.getResource();
			analysisType = AnalysisType.getType(result.getSummary().getType());
			subPathways(token, resource);
			if (analysisType == AnalysisType.EXPRESSION) {
				maxExpression = result.getExpression().getMax();
				minExpression = result.getExpression().getMin();
				expression(token, stId, resource);
			} else if (analysisType == AnalysisType.OVERREPRESENTATION)
				enrichment(token, stId, resource);

		} catch (AnalysisException | AnalysisServerError e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds to the analysis index the subPathways present in the diagram (as
	 * ProcessNodes). So for each it takes its PathwaySummary and divides
	 * entities.getFound() / entities.getTotal() to compute the percentage of
	 * the fill area.
	 */
	private void subPathways(String token, String resource) throws AnalysisServerError, AnalysisException {
		// 1 extract list of subPathways stId (for ProcessNodes)
		final List<String> subPathways = diagram.getNodes().stream()
				.filter(node -> node.getRenderableClass().equals("ProcessNode"))
				.map(node -> graphIndex.get(node.getReactomeId()).getStId())
				.collect(Collectors.toList());
		if (subPathways.isEmpty())
			return;
		// 2 get subPathways summary
		final PathwaySummary[] pathwaysSummary = AnalysisClient.getPathwaysSummary(subPathways, token, resource);
		// extract %
		for (PathwaySummary summary : pathwaysSummary) {
			final EntityStatistics entities = summary.getEntities();
			if (entities != null) {
				int found = entities.getFound();
				int total = entities.getTotal();
				double percentage = (double) found / total;
				if (percentage < MIN_ENRICHMENT && percentage > 0)
					percentage = MIN_ENRICHMENT;
//			final String stId = summary.getStId();
//			System.out.printf("%s %d/%d(%.3f)\n", stId, found, total, percentage);
				final DiagramObject diagramNode = reactomeIndex.get(summary.getDbId());
				if (diagramNode != null)
					getNodeDecorator(diagramNode.getId()).setEnrichment(percentage);
			}
		}
	}

	/**
	 * Computes the list of expressions of components. For each diagram object,
	 * except ProcessNodes, you get a list of lists of doubles
	 */
	private void expression(String token, String stId, String resource) throws AnalysisServerError, AnalysisException {
		final FoundElements foundElements = AnalysisClient.getFoundElements(stId, token, resource);
		// Index analysis nodes via mapsTo
		final Map<String, FoundEntity> analysisIndex = new HashMap<>();
		foundElements.getEntities().forEach(analysisNode ->
				analysisNode.getMapsTo().stream()
						.map(IdentifierMap::getIds)
						.flatMap(Collection::stream)
						.forEach(identifier -> analysisIndex.put(identifier, analysisNode)));

		diagram.getNodes().forEach(diagramNode -> {
			final EntityNode graphNode = graphIndex.get(diagramNode.getReactomeId());
			if (graphNode == null) return;
			final List<FoundEntity> leaves = getLeaves(graphNode).stream()
					.map(leafId -> analysisIndex.get(graphIndex.get(leafId).getIdentifier()))
					.collect(Collectors.toList());
			if (leaves.stream().anyMatch(Objects::nonNull)) {
				leaves.sort((p1, p2) -> {
					// put nulls at the end
					if (p1 == null) return 1;
					if (p2 == null) return -1;
					return p1.getId().compareTo(p2.getId());
				});
				getNodeDecorator(diagramNode.getId()).setExpressions(leaves);
			}
		});
		System.out.printf("[%.2f-%.2f]\n", minExpression, maxExpression);
	}


	/** Computes only the relation of hit found components and found component */
	private void enrichment(String token, String stId, String resource) throws AnalysisException, AnalysisServerError {
		final FoundElements analysisNodes = AnalysisClient.getFoundElements(stId, token, resource);
		// foundEntity.getMapsTo().getIds() -> graphNode.getIdentifier()
		final Set<String> identifiers = analysisNodes.getEntities().stream()
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
						double percentage = getPercentage(graphNodeHit, diagramNode, graphNode);
						getNodeDecorator(diagramNode.getId()).setEnrichment(percentage);
					}
				});
	}

	private double getPercentage(Set<Long> hitIds, Node node, EntityNode graphNode) {
		final Set<Long> leaves = getLeaves(graphNode);
		final int total = leaves.size();
		final long count = leaves.stream().filter(hitIds::contains).count();
		double percentage = (double) count / total;
		if (percentage > 0 && percentage < MIN_ENRICHMENT)
			percentage = MIN_ENRICHMENT;
		// TODO: remove on production
//		System.out.printf("[%s]\t %s \t(%d/%d) %.2f\n", node.getRenderableClass(), node.getDisplayName(),
//				count, total, percentage);
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

	NodeDecorator getNodeDecorator(long id) {
		return (NodeDecorator) index.computeIfAbsent(id, k -> new NodeDecorator());
	}

	EdgeDecorator getEdgeDecorator(long id) {
		return (EdgeDecorator) index.computeIfAbsent(id, k -> new EdgeDecorator());
	}

	public double getMaxExpression() {
		return maxExpression;
	}

	public double getMinExpression() {
		return minExpression;
	}

	public AnalysisType getAnalysisType() {
		return analysisType;
	}

	public class DiagramObjectDecorator {
		private boolean flag = false;
		private boolean selected = false;
		private boolean halo = false;

		public boolean isFlag() {
			return flag;
		}

		void setFlag(boolean flag) {
			this.flag = flag;
		}

		public boolean isSelected() {
			return selected;
		}

		void setSelected(boolean selected) {
			this.selected = selected;
		}

		public boolean isHalo() {
			return halo;
		}

		void setHalo(boolean halo) {
			this.halo = halo;
		}
	}

	public class EdgeDecorator extends DiagramObjectDecorator {
		private List<Connector> connectors = new LinkedList<>();

		public List<Connector> getConnectors() {
			return connectors;
		}
	}

	public class NodeDecorator extends DiagramObjectDecorator {
		private List<FoundEntity> expressions = null;
		private Double enrichment = null;

		public Double getEnrichment() {
			return enrichment;
		}

		void setEnrichment(Double enrichment) {
			this.enrichment = enrichment;
		}

		public List<FoundEntity> getExpressions() {
			return expressions;
		}

		void setExpressions(List<FoundEntity> expressions) {
			this.expressions = expressions;
		}

	}

}
