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
	private Set<Long> selected = new HashSet<>();
	// This would be the only necessary array if a list of dbId is passed through flag and selection
	private Set<Long> graphIds;
	// Index of graph nodes only, for a faster parent/children access
	private Map<Long, GraphNode> graphIndex;
	// Map from stId to dbId
	private Map<String, Long> graphMap;

	DiagramDecorator(DiagramIndex index, RasterArgs args, Graph graph) {
		this.index = index;
		this.args = args;
		this.graph = graph;
		decorate();
	}

	private void decorate() {
		// To improve performance, we only index when flag or selected have elements
		if (args.getFlags() != null && !args.getFlags().isEmpty()
				|| args.getSelected() != null && !args.getSelected().isEmpty()) {
			indexGraph();
			// TODO: 24/10/18 if some day args.getSel and args.getFlag are only dbids, these methods are not needed
			final Collection<Long> sel = getSelectedReactomeIds();
			final Collection<Long> flg = getFlaggedReactomeIds();
			selectElements(sel);
			flagElements(flg);
			clearIndex();
		}
	}

	private void indexGraph() {
		graphIndex = new HashMap<>();
		graphIds = new HashSet<>();
		graphMap = new HashMap<>();
		if (graph.getSubpathways() != null)
			for (SubpathwayNode subpathway : graph.getSubpathways()) {
				graphIds.add(subpathway.getDbId());
				graphMap.put(subpathway.getStId(), subpathway.getDbId());
			}
		for (EntityNode node : graph.getNodes()) {
			graphIds.add(node.getDbId());
			graphIndex.put(node.getDbId(), node);
			graphMap.put(node.getStId(), node.getDbId());
		}
		for (EventNode edge : graph.getEdges()) {
			graphIds.add(edge.getDbId());
			graphMap.put(edge.getStId(), edge.getDbId());
		}

	}

	private void clearIndex() {
		graphIds = null;
		graphIndex = null;
	}

	/**
	 * Reads args.sel and returns a collection of reactome ids (dbId). The collection will contain the mapped ids given
	 * that args.sel contains dbIds, stIds or identifiers
	 */
	private Collection<Long> getSelectedReactomeIds() {
		if (args.getSelected() == null)
			return Collections.emptySet();
		return args.getSelected().stream()
				.map(this::getDiagramObjectId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	/**
	 * Reads args.flag and returns a collection of reactome ids (dbId). The collection will contain the mapped ids given
	 * that args.flag contains dbIds, stIds or identifiers and the dbIds of the parents as well.
	 */
	private Collection<Long> getFlaggedReactomeIds() {
		if (args.getFlags() == null)
			return Collections.emptySet();
		return args.getFlags().stream()
				.map(this::getDiagramObjectId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	private Long getDiagramObjectId(String string) {
		// stId
		final Long id = graphMap.get(string);
		if (id != null) return id;
		// dbId
		try {
			final long dbId = Long.parseLong(string);
			if (graphIds.contains(dbId)) return dbId;
		} catch (NumberFormatException ignored) {
			// ignored, not a dbId
		}
		return null;
	}

	private void selectElements(Collection<Long> selected) {
		for (Long id : selected) {
			final Collection<RenderableNode> nodes = index.getNodesByReactomeId().get(id);
			if (nodes != null) {
				for (RenderableNode node : nodes)
					if (!node.isFadeOut())
						selectNode(node);
			} else {
				final Collection<RenderableEdge> edges = index.getEdgesByReactomeId().get(id);
				if (edges != null) {
					for (RenderableEdge edge : edges)
						if (!edge.isFadeOut())
							selectEdge(edge);
				}
			}
		}
	}

	private void flagElements(Collection<Long> flags) {
		for (Long id : flags) {
			final Collection<RenderableNode> nodes = index.getNodesByReactomeId().get(id);
			if (nodes != null) {
				for (RenderableNode node : nodes)
					if (!node.isFadeOut())
						node.setFlag(true);
			} else {
				final Collection<RenderableEdge> edges = index.getEdgesByReactomeId().get(id);
				if (edges != null)
					for (RenderableEdge edge : edges)
						if (!edge.isFadeOut())
							edge.setFlag(true);
			}
		}
	}

	private void selectNode(RenderableNode node) {
		node.setSelected(true);
		node.setHalo(true);
		this.selected.add(node.getNode().getId());
		for (Connector connector : node.getNode().getConnectors()) {
			final RenderableEdge renderableEdge = index.getEdgesById().get(connector.getEdgeId());
			final Edge reaction = renderableEdge.getEdge();
			// When a node is selected, the nodes in the same reaction are haloed
			renderableEdge.setHalo(true);
			haloEdgeParticipants(reaction);
		}
	}

	private void selectEdge(RenderableEdge edge) {
		edge.setSelected(true);
		edge.setHalo(true);
		haloEdgeParticipants(edge.getEdge());
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

	public Set<Long> getSelectedDiagramId() {
		return selected;
	}

}
