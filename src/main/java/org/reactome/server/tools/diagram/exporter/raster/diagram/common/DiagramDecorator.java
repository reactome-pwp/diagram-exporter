package org.reactome.server.tools.diagram.exporter.raster.diagram.common;

import org.reactome.server.tools.diagram.data.graph.*;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Edge;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableEdge;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableNode;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Includes the selection, flag and halo information for each node and reaction
 * and adds it to the corresponding RenderableObject.
 */
public class DiagramDecorator {

	private final DiagramIndex index;
	private final RasterArgs args;
	private final Graph graph;
	private final Diagram diagram;
	private Map<Long, EntityNode> graphIndex;
	private Map<Long, DiagramObject> diagramIndex;
	private Map<Long, SubpathwayNode> subPathwayIndex;
	private Set<Long> reactionIds;
	private Set<Long> selected = new TreeSet<>();

	DiagramDecorator(DiagramIndex index, RasterArgs args, Graph graph, Diagram diagram) {
		this.index = index;
		this.args = args;
		this.graph = graph;
		this.diagram = diagram;
		decorate();
	}

	private void decorate() {
		diagramIndex = Stream.of(diagram.getEdges(), diagram.getNodes())
				.flatMap(Collection::stream)
				.collect(Collectors.toMap(DiagramObject::getId, item -> item));

		graphIndex = graph.getNodes() == null
				? Collections.emptyMap()
				: graph.getNodes().stream()
				.collect(Collectors.toMap(GraphNode::getDbId, Function.identity(), (a, b) -> a));

		reactionIds = graph.getEdges() == null
				? Collections.emptySet()
				: graph.getEdges().stream().map(EventNode::getDbId)
				.collect(Collectors.toSet());

		subPathwayIndex = graph.getSubpathways() == null
				? Collections.emptyMap()
				: graph.getSubpathways().stream()
				.collect(Collectors.toMap(SubpathwayNode::getDbId, Function.identity(), (a, b) -> a));

		final Set<Long> sel = getSelectedIds();
		final Set<Long> flg = getFlagged();
		decorateNodes(sel, flg);
		decorateReactions(sel, flg);
	}

	private Set<Long> getSelectedIds() {
		if (args.getSelected() == null)
			return Collections.emptySet();
		return args.getSelected().stream()
				.map(this::getDiagramObjectId)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}

	private Set<Long> getFlagged() {
		if (args.getFlags() == null)
			return Collections.emptySet();
		return args.getFlags().stream()
				.map(this::getDiagramObjectId)
				.flatMap(Collection::stream)
				.map(this::getHitElements)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}

	private Collection<Long> getHitElements(Long id) {
		final Set<Long> ids = new HashSet<>();
		ids.add(id);
		final EntityNode node = graphIndex.get(id);
		if (node == null)
			return ids;
		if (node.getParents() != null)
			node.getParents().forEach(parentId -> ids.addAll(getHitElements(parentId)));
		return ids;
	}

	private List<Long> getDiagramObjectId(String string) {
		// dbId, this is faster because dbId is indexed
		try {
			final long dbId = Long.parseLong(string);
			if (graphIndex.containsKey(dbId)
					|| reactionIds.contains(dbId))
				return Collections.singletonList(dbId);
		} catch (NumberFormatException ignored) {
			// ignored, not a dbId
		}
		// TODO: would it be faster if index of stIds, dbIds, identifier and geneNames?
		// Pros: avoid iterating through the list of nodes and edges
		// Cons: index the list of nodes and edges just for a few selected or flag items
		// Nodes
		for (EntityNode node : graph.getNodes()) {
			// stId
			if (string.equalsIgnoreCase(node.getStId())
					|| string.equalsIgnoreCase(node.getIdentifier())
					|| (node.getGeneNames() != null && node.getGeneNames().contains(string)))
				return Collections.singletonList(node.getDbId());
		}
		// Reactions
		for (EventNode eventNode : graph.getEdges())
			if (eventNode.getStId().equalsIgnoreCase(string))
				return Collections.singletonList(eventNode.getDbId());
		// Subpathways
		for (SubpathwayNode subpathwayNode : subPathwayIndex.values()) {
			if (subpathwayNode.getStId().equals(string))
				return subpathwayNode.getEvents();
		}
		// Bad luck, not found
		return Collections.emptyList();
	}

	private void decorateNodes(Collection<Long> selected, Collection<Long> flags) {
		if (selected.isEmpty() && flags.isEmpty())
			return;
		diagram.getNodes().forEach(node -> {
			if (node.getIsFadeOut() != null && node.getIsFadeOut())
				return;
			if (selected.contains(node.getReactomeId())) {
				final RenderableNode renderableNode = index.getNode(node.getId());
				renderableNode.setSelected(true);
				renderableNode.setHalo(true);
				this.selected.add(node.getId());
				node.getConnectors().forEach(connector -> {
					final Edge reaction = (Edge) diagramIndex.get(connector.getEdgeId());
					// When a node is selected, the nodes in the same reaction
					// are haloed
					index.getEdge(reaction.getId()).setHalo(true);
					haloEdgeParticipants(reaction);
				});
			}
			if (flags.contains(node.getReactomeId()))
				index.getNode(node.getId()).setFlag(true);
		});
	}

	private void decorateReactions(Collection<Long> selected, Collection<Long> flags) {
		diagram.getEdges().forEach(reaction -> {
			if (reaction.getIsFadeOut() != null && reaction.getIsFadeOut())
				return;
			if (selected.contains(reaction.getReactomeId())) {
				final RenderableEdge renderableEdge = index.getEdge(reaction.getId());
				renderableEdge.setSelected(true);
				renderableEdge.setHalo(true);
				haloEdgeParticipants(reaction);
			}
			if (flags.contains(reaction.getReactomeId()))
				index.getEdge(reaction.getId()).setFlag(true);
		});
	}

	/**
	 * Adds the reaction to the haloReaction set, participating nodes to
	 * haloNodes and participating connectors to haloConnectors
	 *
	 * @param reaction reaction to halo
	 */
	private void haloEdgeParticipants(Edge reaction) {
		Stream.of(reaction.getActivators(), reaction.getCatalysts(),
				reaction.getInhibitors(), reaction.getInputs(), reaction.getOutputs())
				.filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.map(part -> diagramIndex.get(part.getId()))
				.map(Node.class::cast)
				.filter(node -> node.getIsFadeOut() == null || !node.getIsFadeOut())
				.forEach(node -> index.getNode(node.getId()).setHalo(true));
	}

	public Set<Long> getSelected() {
		return selected;
	}

}
