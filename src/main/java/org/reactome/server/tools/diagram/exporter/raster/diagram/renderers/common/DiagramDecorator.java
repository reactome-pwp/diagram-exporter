package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common;

import org.reactome.server.tools.diagram.data.graph.EntityNode;
import org.reactome.server.tools.diagram.data.graph.EventNode;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Edge;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiagramDecorator {

	private final DiagramIndex index;
	private final RasterArgs args;
	private final Graph graph;
	private final Diagram diagram;
	private Map<Long, EntityNode> graphIndex;
	private Map<Long, DiagramObject> diagramIndex;
	private Set<Long> reactionIds;
	private Set<Long> selected;

	DiagramDecorator(DiagramIndex index, RasterArgs args, Graph graph, Diagram diagram) {
		this.index = index;
		this.args = args;
		this.graph = graph;
		this.diagram = diagram;
		decorate();
	}

	private void decorate() {
		graphIndex = new HashMap<>();
		graph.getNodes().forEach(item -> graphIndex.put(item.getDbId(), item));
		diagramIndex = new HashMap<>();
		Stream.of(diagram.getEdges(), diagram.getNodes())
				.flatMap(Collection::stream)
				.forEach(item -> diagramIndex.put(item.getId(), item));
		reactionIds = new HashSet<>();
		graph.getEdges().forEach(event -> reactionIds.add(event.getDbId()));

		selected = getSelectedIds();
		final Set<Long> flg = getFlagged();
		decorateNodes(selected, flg);
		decorateReactions(selected, flg);
	}

	private Set<Long> getSelectedIds() {
		if (args.getSelected() == null)
			return Collections.emptySet();
		return args.getSelected().stream()
				.map(this::getDiagramObjectId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	private Set<Long> getFlagged() {
		if (args.getFlags() == null)
			return Collections.emptySet();
		return args.getFlags().stream()
				.map(this::getDiagramObjectId)
				.filter(Objects::nonNull)
				.flatMap(this::streamMeAndAncestors)
				.collect(Collectors.toSet());
	}

	private Stream<Long> streamMeAndAncestors(Long id) {
		final Set<Long> ids = new HashSet<>();
		ids.add(id);
		final EntityNode node = graphIndex.get(id);
		if (node == null)
			return ids.stream();
		if (node.getParents() != null)
			node.getParents().forEach(parentId -> streamMeAndAncestors(parentId).forEach(ids::add));
		return ids.stream();
	}

	private Long getDiagramObjectId(String string) {
		// dbId, this is faster because dbId is indexed
		try {
			final long dbId = Long.parseLong(string);
			if (graphIndex.containsKey(dbId)
					|| reactionIds.contains(dbId))
				return dbId;
		} catch (NumberFormatException ignored) {
			// ignored, not a dbId
		}
		// TODO: would it be faster if index of stIds, dbIds, identifier and geneNames?
		// Pros: avoid iterating through the list of nodes and edges
		// Cons: index the list of nodes and edges just for a few selected of flag items
		// Nodes
		for (EntityNode node : graph.getNodes()) {
			// stId
			if (string.equalsIgnoreCase(node.getStId()))
				return node.getDbId();
			// identifier
			if (string.equalsIgnoreCase(node.getIdentifier()))
				return node.getDbId();
			// geneNames
			if (node.getGeneNames() != null && node.getGeneNames().contains(string))
				return node.getDbId();
		}
		// Reactions
		for (EventNode eventNode : graph.getEdges())
			if (eventNode.getStId().equalsIgnoreCase(string))
				return eventNode.getDbId();
		// Bad luck, not found
		return null;
	}

	private void decorateNodes(Collection<Long> selected, Collection<Long> flags) {
		if (selected.isEmpty() && flags.isEmpty())
			return;
		diagram.getNodes().forEach((Node node) -> {
			if (selected.contains(node.getReactomeId())) {
				final DiagramIndex.NodeDecorator decorator = index.getNodeDecorator(node.getId());
				decorator.setSelected(true);
				decorator.setHalo(true);
				node.getConnectors().forEach(connector -> {
					final Edge reaction = (Edge) diagramIndex.get(connector.getEdgeId());
					// When a node is selected, the nodes in the same reaction
					// are haloed
					index.getEdgeDecorator(reaction.getId()).setHalo(true);
					haloEdgeParticipants(reaction);
				});
			}
			if (flags.contains(node.getReactomeId()))
				index.getNodeDecorator(node.getId()).setFlag(true);
		});
	}

	private void decorateReactions(Collection<Long> selected, Collection<Long> flags) {
		diagram.getEdges().forEach(reaction -> {
			if (selected.contains(reaction.getReactomeId())) {
				final DiagramIndex.EdgeDecorator decorator = index.getEdgeDecorator(reaction.getId());
				decorator.setSelected(true);
				decorator.setHalo(true);
				haloEdgeParticipants(reaction);
			}
			if (flags.contains(reaction.getReactomeId()))
				index.getEdgeDecorator(reaction.getId()).setFlag(true);
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
				.forEach(node -> index.getNodeDecorator(node.getId()).setHalo(true));
	}

	public Set<Long> getSelected() {
		return selected;
	}
}
