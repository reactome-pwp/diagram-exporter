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
 * Computes a Diagram to group nodes by renderableClass and RenderType, so all
 * nodes in a group can be rendered with the same colors for filling and
 * stroking.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class DiagramIndex {

	public static final double MIN_ENRICHMENT = 0.05;
	private final Diagram diagram;
	private final Graph graph;
	private Decorator decorator;

	/**
	 * [diagram id] item.getId()
	 */
	private Map<Long, DiagramObject> diagramIndex;
	/**
	 * [reactome id] node.getDbId()
	 */
	private Map<Long, EntityNode> graphIndex;
	/**
	 * [reactome id] item.getReactomeId()
	 */
	private Map<Long, DiagramObject> reactomeIndex;

	/**
	 * [diagram id] item.getId()
	 */
	private Set<Long> selected = new TreeSet<>();
	/**
	 * [diagram id] item.getId()
	 */
	private Set<Long> flags = new TreeSet<>();
	/**
	 * [diagram id] item.getId()
	 */
	private Set<Long> haloed = new HashSet<>();
	/**
	 * [diagram id] item.getId()
	 */
	private Map<Long, Double> analysisIndex = new HashMap<>();

	/**
	 * Computes the maps of nodes, reactions and connectors so they are grouped
	 * by renderableClass, renderType and normal, selection, halo or flag
	 * status.
	 *
	 * @param diagram   diagram with nodes and reactions
	 * @param graph     background graph
	 * @param decorator decorator with selection and flags
	 */
	public DiagramIndex(Diagram diagram, Graph graph, Decorator decorator) {
		this.diagram = diagram;
		this.graph = graph;
		this.decorator = decorator;
		createIndexes();
		collect();
		analysis();
	}

	private void createIndexes() {
		diagramIndex = new HashMap<>();
		reactomeIndex = new HashMap<>();
		Stream.of(diagram.getEdges(), diagram.getNodes(), diagram.getLinks(),
				diagram.getNotes())
				.flatMap(Collection::stream)
				.forEach(item -> {
					diagramIndex.put(item.getId(), item);
					reactomeIndex.put(item.getReactomeId(), item);
				});
		graphIndex = new HashMap<>();
		graph.getNodes().forEach(item -> graphIndex.put(item.getDbId(), item));
	}

	private void collect() {
		if (decorator != null) {
			selectNodes();
			selectReactions();
		}
	}


	private void selectNodes() {
		diagram.getNodes().forEach(node -> {
			// Select node
			if (decorator.getSelected().contains(node.getReactomeId())) {
				selected.add(node.getId());
				haloed.add(node.getId());
				node.getConnectors().forEach(connector -> {
					final Edge reaction = (Edge) diagramIndex.get(connector.getEdgeId());
					haloEdge(reaction);
				});
			}
			// Flag node
			if (decorator.getFlags().contains(node.getReactomeId()))
				flags.add(node.getId());
		});
	}

	private void selectReactions() {
		diagram.getEdges().forEach(reaction -> {
			// Select edge
			if (decorator.getSelected().contains(reaction.getReactomeId())) {
				selected.add(reaction.getId());
				haloEdge(reaction);
			}
			// Flag edge
			if (decorator.getFlags().contains(reaction.getReactomeId())) {
				flags.add(reaction.getId());
				flagReaction(reaction);
			}
		});
	}


	/**
	 * Adds the reaction to the haloReaction set, participating nodes to
	 * haloNodes and participating connectors to haloConnectors
	 *
	 * @param reaction      reaction to halo
	 */
	private void haloEdge(Edge reaction) {
		haloed.add(reaction.getId());
		streamParticipants(reaction)
				.forEach(node -> {
					if (node.getIsFadeOut() == null || !node.getIsFadeOut())
						haloed.add(node.getId());
				});
	}

	/**
	 * Very convenient method to iterate over all of the activators, catalysts,
	 * inhibitors, inputs and outputs in a unique stream
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

	private void flagReaction(Edge reaction) {
		flags.add(reaction.getId());
		streamParticipants(reaction).forEach(node ->
				node.getConnectors().stream()
						.filter(connector -> connector.getEdgeId().equals(reaction.getId()))
						.map(Connector::getEdgeId)
						.forEach(flags::add));
	}

	private void analysis() {
		if (decorator == null)
			return;
		final String token = decorator.getToken();
		if (token == null || token.isEmpty())
			return;
		final String stId = graph.getStId();
		System.out.println(token);
		try {
			final AnalysisResult result = AnalysisClient.getAnalysisResult(token);
			final List<ResourceSummary> summaryList = result.getResourceSummary();
			final ResourceSummary resourceSummary = summaryList.size() == 2 ? summaryList.get(1) : summaryList.get(0);
			final String resource = resourceSummary.getResource();
			subPathways(token, resource);
			enrichment(token, stId, resource);
		} catch (AnalysisException | AnalysisServerError e) {
			e.printStackTrace();
		}


	}

	private void subPathways(String token, String resource) throws AnalysisServerError, AnalysisException {
		// I need a list of process nodes (id, stid, displayname) with %
		// 1 list of processnodes (stid)
		final List<String> subPathways = diagram.getNodes().stream()
				.filter(node -> node.getRenderableClass().equals("ProcessNode"))
				.map(node -> graphIndex.get(node.getReactomeId()).getStId())
				.collect(Collectors.toList());
		// 2 call analysis
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
				final DiagramObject item = reactomeIndex.get(summary.getDbId());
				if (item != null) analysisIndex.put(item.getId(), percentage);
			}
		}
	}

	private void enrichment(String token, String stId, String resource) throws AnalysisException, AnalysisServerError {
		final FoundElements foundElements = AnalysisClient.getFoundElements(stId, token, resource);
		// 1 map foundEntities to graph nodes (EntityNode)
		final Set<String> identifiers = foundElements.getEntities().stream()
				.map(FoundEntity::getMapsTo)
				.flatMap(Collection::stream)
				.map(IdentifierMap::getIds)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
		final Set<EntityNode> hitEntities = graph.getNodes().stream()
				.filter(entityNode -> identifiers.contains(entityNode.getIdentifier()))
				.collect(Collectors.toSet());

		final Set<Long> hitIds = hitEntities.stream()
				.map(GraphNode::getDbId)
				.collect(Collectors.toSet());
		// 3 Find graph nodes with children, count found children, map to diagram item
		diagram.getNodes().stream()
				.filter(node -> !node.getRenderableClass().equals("ProcessNode"))
				.forEach(node -> {
					final EntityNode graphNode = graphIndex.get(node.getReactomeId());
					if (graphNode != null) {
						final Set<Long> leaves = getLeaves(graphNode);
						final int total = leaves.size();
						final long count = leaves.stream().filter(hitIds::contains).count();
						double percentage = (double) count / total;
						if (percentage < MIN_ENRICHMENT && percentage > 0)
							percentage = MIN_ENRICHMENT;
						System.out.printf("[%s]\t %s \t(%d/%d) %.2f\n", node.getRenderableClass(), node.getDisplayName(),
								count, total, percentage);
						analysisIndex.put(node.getId(), percentage);
					}
				});
	}

	private Set<Long> getLeaves(EntityNode node) {
		if (node.getChildren() == null) {
			return Collections.singleton(node.getDbId());
		} else {
			return node.getChildren().stream()
					.map(id -> graphIndex.get(id))
					.filter(Objects::nonNull)
					.map(this::getLeaves)
					.flatMap(Collection::stream)
					.collect(Collectors.toSet());
		}
	}


	public Set<Long> getSelected() {
		return selected;
	}

	public Set<Long> getFlags() {
		return flags;
	}

	public Set<Long> getHaloed() {
		return haloed;
	}

	public Double getAnalysisValue(NodeCommon node) {
		return analysisIndex.get(node.getId());
	}
}
