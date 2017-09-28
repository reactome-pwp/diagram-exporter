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
	private Set<Node> selectedNodes;

	private Map<Long, DiagramObject> diagramIndex;
	private Map<Long, DiagramObject> diagramReactomeIndex;
	private Map<Long, GraphNode> graphIndex;
	private Set<Edge> selectedReactions;
	private Set<Connector> selectedConnectors;
	private Set<DiagramObject> flags;
	private Set<Edge> haloEdges;
	private Set<DiagramObject> haloNodes;
	private Set<Connector> haloConnectors;
	private Map<String, Map<RenderType, Set<Node>>> classifiedNodes;
	private Map<String, Map<RenderType, Set<Edge>>> classifiedReactions;

	public DiagramIndex(Diagram diagram, Graph graph, Decorator decorator, AnalysisType analysisType) {
		this.diagram = diagram;
		this.graph = graph;
		this.decorator = decorator;
		this.analysisType = analysisType;
		createIndexes();
		collectSelected();
		collectFlags();
		collectHalo();
		collectClassifiedNodes();
		collectClassifiedEdges();
	}

	private void createIndexes() {
		diagramIndex = new HashMap<>();
		diagramReactomeIndex = new HashMap<>();
		graphIndex = new HashMap<>();
		Stream.of(diagram.getEdges(), diagram.getNodes(), diagram.getLinks())
				.flatMap(Collection::stream)
				.forEach(item -> {
					diagramIndex.put(item.getId(), item);
					diagramReactomeIndex.put(item.getReactomeId(), item);
				});
		Stream.of(graph.getEdges(), graph.getNodes())
				.flatMap(Collection::stream)
				.forEach(item -> graphIndex.put(item.getDbId(), item));
	}

	private void collectSelected() {
		collectSelectedNodes();
		collectSelectedReactions();
		collectSelectedConnectors();
	}

	private void collectSelectedNodes() {
		selectedNodes = Stream.of(diagram.getNodes())
				.flatMap(Collection::stream)
				.filter(item -> decorator.getSelected().contains(item.getReactomeId()))
				.collect(Collectors.toSet());
	}

	private void collectSelectedReactions() {
		selectedReactions = diagram.getEdges().stream()
				.filter(item -> decorator.getSelected().contains(item.getReactomeId()))
				.collect(Collectors.toSet());
	}

	private void collectSelectedConnectors() {
		selectedConnectors = new HashSet<>();
		selectedReactions.forEach(reaction ->
				Stream.of(reaction.getActivators(), reaction.getInputs(),
						reaction.getOutputs(), reaction.getInhibitors(),
						reaction.getCatalysts())
						.filter(Objects::nonNull)
						.flatMap(Collection::stream)
						.forEach(part -> {
							final Node node = (Node) diagramIndex.get(part.getId());
							node.getConnectors().stream()
									.filter(connector -> connector.getEdgeId().equals(reaction.getId()))
									.forEach(selectedConnectors::add);
						}));
	}

	private void collectFlags() {
		flags = Stream.of(diagram.getEdges(), diagram.getNodes())
				.flatMap(Collection::stream)
				.filter(item -> decorator.getFlags().contains(item.getReactomeId()))
				.collect(Collectors.toSet());
	}

	private void collectHalo() {
		haloConnectors = new HashSet<>();
		haloEdges = new HashSet<>();
		haloNodes = new HashSet<>();
		// If we have selected reactions, we need to halo
		// - reactions
		// - nodes participating in those reactions
		// - segments of nodes that participate in the reaction

		// If we have selected nodes, we have to halo
		// - nodes
		// - reactions where nodes participate
		// - nodes participating in those reactions
		// - segments of nodes that participate in the reaction
		final Set<Long> ids = selectedNodes.stream()
				.map(Node::getId)
				.collect(Collectors.toSet());
		diagram.getEdges().stream()
				.filter(reaction ->
						// A reaction is haloed if it is selected
						// or any of its parts is selected
						selectedReactions.contains(reaction) ||
								Stream.of(reaction.getActivators(), reaction.getCatalysts(),
										reaction.getInhibitors(), reaction.getInputs(),
										reaction.getOutputs())
										.filter(Objects::nonNull)
										.flatMap(Collection::stream)
										.anyMatch(part -> ids.contains(part.getId()))
				)
				// For each reaction, we halo it, its participating parts and
				// the segments that join parts with the reaction
				.forEach(reaction -> {
					haloEdges.add(reaction);
					Stream.of(reaction.getActivators(), reaction.getCatalysts(),
							reaction.getInhibitors(), reaction.getInputs(),
							reaction.getOutputs())
							.filter(Objects::nonNull)
							.flatMap(Collection::stream)
							.map(part -> getNode(part.getId(), diagram))
							.filter(Objects::nonNull)
							.forEach(node -> {
								haloNodes.add(node);
								node.getConnectors().stream()
										.filter(connector -> connector.getEdgeId().equals(reaction.getId()))
										.forEach(haloConnectors::add);
							});
				});

	}

	private void collectClassifiedNodes() {
		classifiedNodes = diagram.getNodes().stream()
				.collect(Collectors.groupingBy(
						DiagramObject::getRenderableClass,
						Collectors.groupingBy((DiagramObject item) -> getRenderType(item, analysisType), Collectors.toSet())));
	}

	public void collectClassifiedEdges() {
		classifiedReactions = diagram.getEdges().stream()
				.collect(Collectors.groupingBy(
						DiagramObject::getRenderableClass,
						Collectors.groupingBy((DiagramObject item) -> getRenderType(item, analysisType), Collectors.toSet())));
	}

	public Set<Node> getSelectedNodes() {
		return selectedNodes;
	}

	public Set<DiagramObject> getFlags() {
		return flags;
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

	private RenderType getRenderType(DiagramObject item, AnalysisType analysisType) {
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

	public Set<Edge> getHaloEdges() {
		return haloEdges;

	}

	private Node getNode(long id, Diagram diagram) {
		return diagram.getNodes().stream()
				.filter(node -> node.getId().equals(id))
				.findFirst()
				.orElse(null);
	}

}
