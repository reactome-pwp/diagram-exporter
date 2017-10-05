package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.graph.GraphNode;
import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.raster.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.RenderType;

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

	private final Diagram diagram;
	private final Graph graph;
	private Decorator decorator;
	private AnalysisType analysisType;

	private Map<Long, DiagramObject> diagramIndex;
//	private Map<Long, DiagramObject> diagramReactomeIndex;
	private Map<Long, GraphNode> graphIndex;
	private Map<String, Set<Node>> selectedNodes;
	private Set<Edge> selectedReactions;
	private Set<Connector> selectedConnectors;
	private Map<String, Set<DiagramObject>> flagNodes;
	private Map<String, Set<Edge>> haloReactions;
	private Map<String, Set<DiagramObject>> haloNodes;
	private Map<String, Set<Connector>> haloConnectors;
	private Map<String, Map<RenderType, Set<Node>>> classifiedNodes;
	private Map<String, Map<RenderType, Set<Edge>>> classifiedReactions;
	private Map<String, Map<RenderType, Set<Connector>>> classifiedConnectors;
	private Set<Edge> flagReactions;
	private HashSet<Connector> flagConnectors;

	/**
	 * Computes the maps of nodes, reactions and connectors so they are grouped
	 * by renderableClass, renderType and normal, selection, halo or flag
	 * status.
	 *
	 * @param diagram      diagram with nodes and reactions
	 * @param graph        background graph
	 * @param decorator    decorator with selection and flags
	 * @param analysisType type of analysis
	 */
	public DiagramIndex(Diagram diagram, Graph graph, Decorator decorator, AnalysisType analysisType) {
		this.diagram = diagram;
		this.graph = graph;
		this.decorator = decorator;
		this.analysisType = analysisType;
		createIndexes();
		collect();
	}

	private void createIndexes() {
		diagramIndex = new HashMap<>();
//		diagramReactomeIndex = new HashMap<>();
		graphIndex = new HashMap<>();
		Stream.of(diagram.getEdges(), diagram.getNodes(), diagram.getLinks(),
				diagram.getNotes())
				.flatMap(Collection::stream)
				.forEach(item -> {
					diagramIndex.put(item.getId(), item);
//					diagramReactomeIndex.put(item.getReactomeId(), item);
				});
		Stream.of(graph.getEdges(), graph.getNodes())
				.flatMap(Collection::stream)
				.forEach(item -> graphIndex.put(item.getDbId(), item));
	}

	private void collect() {
		classifyNodes();
		classifyReactions();
		if (decorator != null) {
			selectNodes();
			selectReactions();
		}
	}

	private void classifyNodes() {
		classifiedNodes = new HashMap<>();
		classifiedNodes = diagram.getNodes().stream()
				.collect(Collectors.groupingBy(Node::getRenderableClass,
						Collectors.groupingBy(this::getRenderType, Collectors.toSet())));
	}

	private void classifyReactions() {
		classifiedReactions = new HashMap<>();
		classifiedConnectors = new HashMap<>();
		diagram.getEdges().forEach(reaction -> {
			// Classify edges by renderingClass/renderType
			final RenderType renderType = getRenderType(reaction);
			classifiedReactions
					.computeIfAbsent(reaction.getRenderableClass(), k -> new HashMap<>())
					.computeIfAbsent(renderType, k -> new HashSet<>())
					.add(reaction);
			// Classify connectors by renderingClass/renderType
			final Set<Connector> connectorSet = classifiedConnectors
					.computeIfAbsent(reaction.getRenderableClass(), k -> new HashMap<>())
					.computeIfAbsent(renderType, k -> new HashSet<>());
			streamParticipants(reaction)
					.map(Node::getConnectors)
					.flatMap(Collection::stream)
					.filter(connector -> connector.getEdgeId().equals(reaction.getId()))
					.forEach(connectorSet::add);
		});
	}


	private void selectNodes() {
		selectedNodes = new HashMap<>();
		haloNodes = new HashMap<>();
		flagNodes = new HashMap<>();
		haloReactions = new HashMap<>();
		haloConnectors = new HashMap<>();
		diagram.getNodes().forEach(node -> {
			// Select node
			if (decorator.getSelected().contains(node.getReactomeId())) {
				selectedNodes
						.computeIfAbsent(node.getRenderableClass(), k -> new HashSet<>())
						.add(node);
				haloNodes.
						computeIfAbsent(node.getRenderableClass(), k -> new HashSet<>())
						.add(node);
				node.getConnectors().forEach(connector -> {
					final Edge reaction = (Edge) diagramIndex.get(connector.getEdgeId());
					haloEdge(reaction, false);
				});
			}
			// Flag node
			if (decorator.getFlags().contains(node.getReactomeId()))
				flagNodes.computeIfAbsent(node.getRenderableClass(), k -> new HashSet<>()).add(node);
		});
	}

	private void selectReactions() {
		selectedReactions = new HashSet<>();
		selectedConnectors = new HashSet<>();
		flagReactions = new HashSet<>();
		flagConnectors = new HashSet<>();
		diagram.getEdges().forEach(reaction -> {
			// Select edge
			if (decorator.getSelected().contains(reaction.getReactomeId())) {
				selectedReactions.add(reaction);
				haloEdge(reaction, true);
			}
			// Flag edge
			if (decorator.getFlags().contains(reaction.getReactomeId())) {
				flagReactions.add(reaction);
				flagReaction(reaction);
			}
		});
	}


	/**
	 * Adds the reaction to the haloReaction set, participating nodes to
	 * haloNodes and participating connectors to haloConnectors
	 *
	 * @param reaction      reaction to halo
	 * @param withSelection whether this reaction is selected or not. If true,
	 *                      participating connectors will be added to
	 *                      selectedConnectors as well
	 */
	private void haloEdge(Edge reaction, boolean withSelection) {
		haloReactions
				.computeIfAbsent(reaction.getRenderableClass(), k -> new HashSet<>())
				.add(reaction);
		final Set<Connector> connectors = haloConnectors.computeIfAbsent(reaction.getRenderableClass(), k -> new HashSet<>());
		streamParticipants(reaction)
				.forEach(node -> {
					haloNodes
							.computeIfAbsent(node.getRenderableClass(), k -> new HashSet<>())
							.add(node);
					node.getConnectors().stream()
							.filter(connector -> connector.getEdgeId().equals(reaction.getId()))
							.forEach(connector -> {
								connectors.add(connector);
								if (withSelection)
									selectedConnectors.add(connector);
							});
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
		flagReactions.add(reaction);
		streamParticipants(reaction).forEach(node ->
				node.getConnectors().stream()
						.filter(connector -> connector.getEdgeId().equals(reaction.getId()))
						.forEach(flagConnectors::add));
	}

	private RenderType getRenderType(DiagramObject item) {
		if (item.getIsFadeOut() != null)
			return RenderType.FADE_OUT;
		final boolean isDisease = item.getIsDisease() != null;
		if (analysisType == AnalysisType.NONE) {
			return isDisease
					? RenderType.DISEASE
					: RenderType.NORMAL;
		}
		final GraphNode node = getNode(graph, item);
		if (node == null) {
			return isDisease
					? RenderType.NOT_HIT_BY_ANALYSIS_DISEASE
					: RenderType.NOT_HIT_BY_ANALYSIS_NORMAL;
		}
		if (isHit(node)) {
//			return RenderType.HIT_INTERACTORS;
			switch (analysisType) {
				case SPECIES_COMPARISON:
				case OVERREPRESENTATION:
					return isDisease
							? RenderType.HIT_BY_ENRICHMENT_DISEASE
							: RenderType.HIT_BY_ENRICHMENT_NORMAL;
				case EXPRESSION:
					return isDisease
							? RenderType.HIT_BY_EXPRESSION_DISEASE
							: RenderType.HIT_BY_EXPRESSION_NORMAL;
				default:
					return RenderType.HIT_INTERACTORS;
			}
		}
		return isDisease
				? RenderType.NOT_HIT_BY_ANALYSIS_DISEASE
				: RenderType.NOT_HIT_BY_ANALYSIS_NORMAL;
	}

	public Map<String, Set<Node>> getSelectedNodes() {
		return selectedNodes;
	}

	public Map<String, Set<DiagramObject>> getFlagNodes() {
		return flagNodes;
	}

	public Set<Connector> getSelectedConnectors() {
		return selectedConnectors;
	}

	public Set<Edge> getSelectedReactions() {
		return selectedReactions;
	}


	public Map<String, Map<RenderType, Set<Node>>> getClassifiedNodes() {
		return classifiedNodes;
	}

	public Map<String, Map<RenderType, Set<Edge>>> getClassifiedReactions() {
		return classifiedReactions;
	}

	public HashSet<Connector> getFlagConnectors() {
		return flagConnectors;
	}

	public Set<Edge> getFlagReactions() {
		return flagReactions;
	}

	public Map<String, Map<RenderType, Set<Connector>>> getClassifiedConnectors() {
		return classifiedConnectors;
	}

	private boolean isHit(GraphNode item) {
		return false;
	}

	private GraphNode getNode(Graph graph, DiagramObject item) {
		return graph.getNodes().stream()
				.filter(node -> node.getDbId().equals(item.getId()))
				.findFirst().orElse(null);
	}


	public Map<String, Set<DiagramObject>> getHaloNodes() {
		return haloNodes;

	}

	public Map<String, Set<Connector>> getHaloConnectors() {
		return haloConnectors;

	}

	public Map<String, Set<Edge>> getHaloReactions() {
		return haloReactions;
	}
}
