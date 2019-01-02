package org.reactome.server.tools.diagram.exporter.raster.diagram.common;

import org.reactome.server.tools.diagram.data.graph.EntityNode;
import org.reactome.server.tools.diagram.data.graph.EventNode;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.graph.SubpathwayNode;
import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.Edge;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableEdge;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableNode;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableProcessNode;

import java.util.*;
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
	private GraphIndex graphIndex;

	DiagramDecorator(DiagramIndex index, RasterArgs args, Graph graph) {
		this.index = index;
		this.args = args;
		this.graph = graph;
		decorate();
	}

	private void decorate() {
		// To improve performance, we only index when flag or selected have elements
		if (args.getFlags().isEmpty() && args.getSelected().isEmpty()) return;
		graphIndex = new GraphIndex(graph);
		// TODO: 24/10/18 if some day args.getSel and args.getFlag are only dbids, these methods are not needed
		final Collection<Long> sel = getSelectedReactomeIds();
		final Collection<Long> flg = getFlaggedReactomeIds();
		selectElements(sel);
		flagElements(flg);
		graphIndex = null;
	}

	/**
	 * Reads args.sel and returns a collection of reactome ids (dbId). The collection will contain the mapped ids given
	 * that args.sel contains dbIds, stIds or identifiers
	 */
	private Collection<Long> getSelectedReactomeIds() {
		if (args.getSelected() == null)
			return Collections.emptySet();
		final Set<Long> ids = new HashSet<>();
		for (String string : args.getSelected()) {
			final Long id = getReactomeId(string);
			if (id != null) {
				ids.addAll(includeSubEvents(id));
			}
		}
		return ids;
	}

	/**
	 * Reads args.flag and returns a collection of reactome ids (dbId). The collection will contain the mapped ids given
	 * that args.flag contains dbIds, stIds or identifiers and the dbIds of the parents as well.
	 */
	private Collection<Long> getFlaggedReactomeIds() {
		if (args.getFlags() == null)
			return Collections.emptySet();
		final Set<Long> ids = new HashSet<>();
		for (String string : args.getFlags()) {
			final Long id = getReactomeId(string);
			if (id != null) {
				ids.addAll(includeSubEvents(id));
				ids.addAll(includeSuperNodes(id));
			}
		}
		return ids;
	}

	private Long getReactomeId(String string) {
		// stId
		final EventNode event = graphIndex.getEventsByStId().get(string);
		if (event != null) return event.getDbId();
		final EntityNode entity = graphIndex.getNodesByStId().get(string);
		if (entity != null) return entity.getDbId();
		final SubpathwayNode pathway = graphIndex.getPathwaysByStId().get(string);
		if (pathway != null) return pathway.getDbId();
		// dbId
		try {
			return Long.parseLong(string);
		} catch (NumberFormatException ignored) {
			// ignored, not a dbId
		}
		return null;
	}

	/**
	 * If the id corresponds to a pathway, add all of its events
	 */
	private Collection<Long> includeSubEvents(Long id) {
		final Set<Long> ids = new HashSet<>();
		ids.add(id);
		final SubpathwayNode pathway = graphIndex.getPathwaysByDbId().get(id);
		if (pathway != null) {
			if (pathway.getEvents() != null) {
				for (Long event : pathway.getEvents())
					ids.addAll(includeSubEvents(event));
			}
		}
		return ids;

	}

	/**
	 * Returns a list containing <em>id</em> and all the ancestors containing the graph node represented by id.
	 *
	 * @param id id of an element to flag
	 * @return the list of elements containing the graph node id, including the node itself
	 */
	private Collection<Long> includeSuperNodes(Long id) {
		final Set<Long> ids = new HashSet<>();
		ids.add(id);
		final EntityNode entity = graphIndex.getNodesByDbId().get(id);
		if (entity != null) {
			if (entity.getParents() != null) {
				for (Long aLong : entity.getParents())
					ids.addAll(includeSuperNodes(aLong));
			}
		}
		return ids;
	}

	private void selectElements(Collection<Long> selected) {
		for (Long id : selected) {
			final Collection<RenderableNode> nodes = index.getNodesByReactomeId().get(id);
			if (nodes != null) {
				for (RenderableNode node : nodes) {
					if (!node.isFadeOut()) selectNode(node);
				}
				continue;
			}
			final Collection<RenderableProcessNode> pathways = index.getPathwaysByReactomeId().get(id);
			if (pathways != null) {
				for (RenderableProcessNode pathway : pathways) {
					if (!pathway.isFadeOut()) selectPathway(pathway);
				}
				continue;
			}
			final Collection<RenderableEdge> edges = index.getEdgesByReactomeId().get(id);
			if (edges != null) {
				for (RenderableEdge edge : edges) {
					if (!edge.isFadeOut()) selectEdge(edge);
				}
			}
		}
	}

	private void flagElements(Collection<Long> flags) {
		for (Long id : flags) {
			final Collection<RenderableNode> nodes = index.getNodesByReactomeId().get(id);
			if (nodes != null) {
				for (RenderableNode node : nodes) {
					if (!node.isFadeOut()) node.setFlag(true);
				}
				continue;
			}
			final Collection<RenderableProcessNode> pathways = index.getPathwaysByReactomeId().get(id);
			if (pathways != null) {
				for (RenderableProcessNode pathway : pathways) {
					if (!pathway.isFadeOut()) pathway.setFlag(true);
				}
				continue;
			}
			final Collection<RenderableEdge> edges = index.getEdgesByReactomeId().get(id);
			if (edges != null)
				for (RenderableEdge edge : edges) {
					if (!edge.isFadeOut()) edge.setFlag(true);
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

	private void selectPathway(RenderableProcessNode pathway) {
		pathway.setSelected(true);
		this.selected.add(pathway.getNode().getId());
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
