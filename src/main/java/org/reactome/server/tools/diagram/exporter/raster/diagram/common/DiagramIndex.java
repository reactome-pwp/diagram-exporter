package org.reactome.server.tools.diagram.exporter.raster.diagram.common;

import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.*;

import java.util.*;

/**
 * Encapsulates every node in a RenderableDiagramObject and stores a dual index on diagram/graph id.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class DiagramIndex {

	private Map<Long, Collection<RenderableEdge>> edgesByReactomeId;
	private Map<Long, RenderableEdge> edgesById;

	private Map<Long, Collection<RenderableNode>> nodesByReactomeId;
	private Map<Long, RenderableNode> nodesById;

	private Map<Long, Collection<RenderableProcessNode>> pathwaysByReactomeId;
//	private Map<Long, RenderableProcessNode> pathwaysById;

	private Collection<RenderableCompartment> compartments;
	private Collection<RenderableLink> links;
	private Collection<RenderableDiagramObject> allNodes;

	/**
	 * Creates a new DiagramIndex with the information for each node in maps.
	 *
	 * @param diagram diagram with nodes and reactions
	 */
	DiagramIndex(Diagram diagram) {
		final Map<Long, Collection<RenderableEdge>> edgesByReactomeId = new HashMap<>();
		final Map<Long, RenderableEdge> edgesById = new HashMap<>();
		final Map<Long, Collection<RenderableNode>> nodesByReactomeId = new HashMap<>();
		final Map<Long, RenderableNode> nodesById = new HashMap<>();
		final Map<Long, Collection<RenderableProcessNode>> pathwaysByReactomeId = new HashMap<>();
		final Map<Long, RenderableProcessNode> pathwaysById = new HashMap<>();
		final Collection<RenderableCompartment> compartments = new ArrayList<>();
		final Collection<RenderableLink> links = new ArrayList<>();

		for (Edge edge : diagram.getEdges()) {
			final RenderableEdge renderableEdge = (RenderableEdge) RenderableFactory.getRenderableObject(edge);
			edgesById.put(edge.getId(), renderableEdge);
			edgesByReactomeId.computeIfAbsent(edge.getReactomeId(), dbId -> new ArrayList<>()).add(renderableEdge);
		}
		for (Compartment compartment : diagram.getCompartments()) {
			final RenderableDiagramObject renderableObject = RenderableFactory.getRenderableObject(compartment);
			compartments.add((RenderableCompartment) renderableObject);
		}
		for (Link link : diagram.getLinks()) {
			final RenderableDiagramObject renderableObject = RenderableFactory.getRenderableObject(link);
			links.add((RenderableLink) renderableObject);
		}
		for (Node node : diagram.getNodes()) {
			final RenderableDiagramObject renderableObject = RenderableFactory.getRenderableObject(node);
			if (renderableObject instanceof RenderableProcessNode) {
				final RenderableProcessNode renderableProcessNode = (RenderableProcessNode) renderableObject;
				pathwaysById.put(node.getId(), renderableProcessNode);
				pathwaysByReactomeId.computeIfAbsent(node.getReactomeId(), dbId -> new ArrayList<>()).add(renderableProcessNode);
			} else {
				final RenderableNode renderableNode = (RenderableNode) renderableObject;
				nodesById.put(node.getId(), renderableNode);
				nodesByReactomeId.computeIfAbsent(node.getReactomeId(), dbId -> new ArrayList<>()).add(renderableNode);
			}
		}
		this.edgesById = Collections.unmodifiableMap(edgesById);
		this.edgesByReactomeId = Collections.unmodifiableMap(edgesByReactomeId);
		this.nodesById = Collections.unmodifiableMap(nodesById);
		this.nodesByReactomeId = Collections.unmodifiableMap(nodesByReactomeId);
		this.links = Collections.unmodifiableCollection(links);
		this.compartments = Collections.unmodifiableCollection(compartments);
//		this.pathwaysById = Collections.unmodifiableMap(pathwaysById);
		this.pathwaysByReactomeId = Collections.unmodifiableMap(pathwaysByReactomeId);

		allNodes = new ArrayList<>();
		allNodes.addAll(compartments);
		allNodes.addAll(links);
		allNodes.addAll(edgesById.values());
		allNodes.addAll(nodesById.values());
		allNodes.addAll(pathwaysById.values());

	}

	public Map<Long, Collection<RenderableEdge>> getEdgesByReactomeId() {
		return edgesByReactomeId;
	}

	public Map<Long, RenderableEdge> getEdgesById() {
		return edgesById;
	}

	Map<Long, Collection<RenderableNode>> getNodesByReactomeId() {
		return nodesByReactomeId;
	}

	public Map<Long, RenderableNode> getNodesById() {
		return nodesById;
	}

	Map<Long, Collection<RenderableProcessNode>> getPathwaysByReactomeId() {
		return pathwaysByReactomeId;
	}

//	public Map<Long, RenderableProcessNode> getPathwaysById() {
//		return pathwaysById;
//	}

	public Collection<RenderableCompartment> getCompartments() {
		return compartments;
	}

	public Collection<RenderableLink> getLinks() {
		return links;
	}

	public Collection<RenderableDiagramObject> getAllObjects() {
		return allNodes;
	}
}
