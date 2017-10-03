package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.graph.GraphNode;
import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.exporter.pptx.model.Decorator;
import org.reactome.server.tools.diagram.exporter.raster.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.RenderType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiagramIndex {

	private final Diagram diagram;
	private final Graph graph;
	private Decorator decorator;
	private AnalysisType analysisType;

	private Map<Long, DiagramObject> diagramIndex;
	private Map<Long, DiagramObject> diagramReactomeIndex;
	private Map<Long, GraphNode> graphIndex;
	private Set<Node> selectedNodes;
	private Set<Edge> selectedReactions;
	private Set<Connector> selectedConnectors;
	private Set<DiagramObject> flagNodes;
	private Set<Edge> haloReactions;
	private Set<DiagramObject> haloNodes;
	private Set<Connector> haloConnectors;
	private Map<String, Map<RenderType, Set<Node>>> classifiedNodes;
	private Map<String, Map<RenderType, Set<Edge>>> classifiedReactions;
	private Map<String, Map<RenderType, Set<Connector>>> classifiedConnectors;
	private Set<Edge> flagReactions;
	private HashSet<Connector> flagConnectors;

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
		diagramReactomeIndex = new HashMap<>();
		graphIndex = new HashMap<>();
		Stream.of(diagram.getEdges(), diagram.getNodes(), diagram.getLinks(),
				diagram.getNotes())
				.flatMap(Collection::stream)
				.forEach(item -> {
					diagramIndex.put(item.getId(), item);
					diagramReactomeIndex.put(item.getReactomeId(), item);
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
			classifiedReactions.putIfAbsent(reaction.getRenderableClass(), new HashMap<>());
			final Map<RenderType, Set<Edge>> reactions = classifiedReactions.get(reaction.getRenderableClass());
			final RenderType renderType = getRenderType(reaction);
			reactions.putIfAbsent(renderType, new HashSet<>());
			reactions.get(renderType).add(reaction);
			// Classify connectors by renderingClass/renderType
			classifiedConnectors.putIfAbsent(reaction.getRenderableClass(), new HashMap<>());
			final Map<RenderType, Set<Connector>> connectors = classifiedConnectors.get(reaction.getRenderableClass());
			connectors.putIfAbsent(renderType, new HashSet<>());
			final Set<Connector> connectorSet = connectors.get(renderType);
			streamParticipants(reaction)
					.map(Node::getConnectors)
					.flatMap(Collection::stream)
					.filter(connector -> connector.getEdgeId().equals(reaction.getId()))
					.forEach(connectorSet::add);
		});
	}


	private void selectNodes() {
		selectedNodes = new HashSet<>();
		haloNodes = new HashSet<>();
		flagNodes = new HashSet<>();
		haloReactions = new HashSet<>();
		haloConnectors = new HashSet<>();
		diagram.getNodes().forEach(node -> {
			// Select node
			if (decorator.getSelected().contains(node.getReactomeId())) {
				selectedNodes.add(node);
				haloNodes.add(node);
				node.getConnectors().forEach(connector -> {
					final Edge reaction = (Edge) diagramIndex.get(connector.getEdgeId());
					haloEdge(reaction, false);
				});
			}
			// Flag node
			if (decorator.getFlags().contains(node.getReactomeId()))
				flagNodes.add(node);
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
		haloReactions.add(reaction);
		streamParticipants(reaction)
				.forEach(node -> {
					haloNodes.add(node);
					node.getConnectors().stream()
							.filter(connector -> connector.getEdgeId().equals(reaction.getId()))
							.forEach(connector -> {
								haloConnectors.add(connector);
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

	public Set<Node> getSelectedNodes() {
		return selectedNodes;
	}

	public Set<DiagramObject> getFlagNodes() {
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


	public Set<DiagramObject> getHaloNodes() {
		return haloNodes;

	}

	public Set<Connector> getHaloConnectors() {
		return haloConnectors;

	}

	public Set<Edge> getHaloReactions() {
		return haloReactions;
	}
}
