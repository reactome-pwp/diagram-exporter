package org.reactome.server.tools.diagram.exporter.raster.diagram.common;

import org.reactome.server.tools.diagram.data.graph.*;
import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.Edge;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableEdge;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableNode;

import java.util.*;
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
	// We need to keep a list of diagram ids of selected nodes for the legend
	private Set<Long> selected = new TreeSet<>();

	DiagramDecorator(DiagramIndex index, RasterArgs args, Graph graph) {
		this.index = index;
		this.args = args;
		this.graph = graph;
		decorate();
	}

	private void decorate() {
		final Collection<Long> sel = getSelectedIds();
		final Collection<Long> flg = getFlagged();
		decorateNodes(sel, flg);
		decorateReactions(sel, flg);
	}

	private Collection<Long> getSelectedIds() {
		if (args.getSelected() == null)
			return Collections.emptySet();
		return args.getSelected().stream()
				.map(this::getDiagramObjectId)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}

	private Collection<Long> getFlagged() {
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
		final GraphNode graphNode = index.getGraphNodesByReactomeId().get(id);
		final EntityNode node = (EntityNode) graphNode;
		if (node == null)
			return ids;
		if (node.getParents() != null)
			node.getParents().forEach(parentId -> ids.addAll(getHitElements(parentId)));
		return ids;
	}

	/**
	 * @deprecated once the exporter accepts only dbIds, this will not be needed
	 */
	@Deprecated
	private List<Long> getDiagramObjectId(String string) {
		// dbId, this is faster because dbId is indexed
		try {
			final long dbId = Long.parseLong(string);
			if (index.getGraphNodesByReactomeId().containsKey(dbId)
					|| index.getSubPathwaysByReactomeId().containsKey(dbId))
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
		for (SubpathwayNode subpathwayNode : index.getSubPathwaysByReactomeId().values()) {
			if (subpathwayNode.getStId().equals(string))
				return subpathwayNode.getEvents();
		}
		// Bad luck, not found
		return Collections.emptyList();
	}

	private void decorateNodes(Collection<Long> selected, Collection<Long> flags) {
		if (selected.isEmpty() && flags.isEmpty())
			return;
		for (RenderableNode node : index.getNodesById().values()) {
			if (node.isFadeOut()) continue;
			if (selected.contains(node.getNode().getReactomeId())) {
				node.setSelected(true);
				node.setHalo(true);
				this.selected.add(node.getNode().getId());
				for (Connector connector : node.getNode().getConnectors()) {
					final RenderableEdge renderableEdge = index.getEdgesById().get(connector.getEdgeId());
					final Edge reaction = renderableEdge.getEdge();
					// When a node is selected, the nodes in the same reaction
					// are haloed
					renderableEdge.setHalo(true);
					haloEdgeParticipants(reaction);
				}
			}
			if (flags.contains(node.getNode().getReactomeId())) {
				node.setFlag(true);
			}
		}
	}

	private void decorateReactions(Collection<Long> selected, Collection<Long> flags) {
		for (RenderableEdge edge : index.getEdgesById().values()) {
			if (edge.isFadeOut()) continue;
			if (selected.contains(edge.getEdge().getReactomeId())) {
				edge.setSelected(true);
				edge.setHalo(true);
				haloEdgeParticipants(edge.getEdge());
			}
			if (flags.contains(edge.getEdge().getReactomeId()))
				edge.setFlag(true);
		}
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
				.map(part -> index.getNodesById().get(part.getId()))
				.filter(node -> !node.isFadeOut())
				.forEach(node -> node.setHalo(true));
	}

	public Set<Long> getSelected() {
		return selected;
	}

}
