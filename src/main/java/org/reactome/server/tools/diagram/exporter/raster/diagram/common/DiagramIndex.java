package org.reactome.server.tools.diagram.exporter.raster.diagram.common;

import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.graph.GraphNode;
import org.reactome.server.tools.diagram.data.graph.SubpathwayNode;
import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * This is the second most important class of the project. It makes 4 things:
 * <ol>
 * <li>creates a {@link RenderableDiagramObject} for each element in the diagram</li>
 * <li>stores several indexes of these renderable objects</li>
 * <li>sets the decoration data (flag and selection) to each renderable object</li>
 * <li>sets the analysis data to each renderable object</li>
 * </ol>
 * For decoration and analysis it delegates everything to two subclasses: {@link DiagramDecorator} and {@link DiagramAnalysis},
 * and keeps both references. This way, only one object is passed as argument to layout methods.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class DiagramIndex {

	private Map<Long, GraphNode> graphNodesByReactomeId;
	private Map<Long, SubpathwayNode> subPathwaysByReactomeId;

	private Map<Long, Collection<RenderableEdge>> edgesByReactomeId;
	private Map<Long, RenderableEdge> edgesById;

	private Map<Long, Collection<RenderableNode>> nodesByReactomeId;
	private Map<Long, RenderableNode> nodesById;

	private Map<Long, Collection<RenderableProcessNode>> pathwaysByReactomeId;
	private Map<Long, RenderableProcessNode> pathwaysById;

	private Collection<RenderableCompartment> compartments;
	private Collection<RenderableLink> links;
	private Collection<RenderableDiagramObject> allNodes;

	/**
	 * Creates a new DiagramIndex with the information for each node in maps.
	 *
	 * @param diagram diagram with nodes and reactions
	 * @param graph   background graph
	 */
	public DiagramIndex(Diagram diagram, Graph graph, RasterArgs args, AnalysisStoredResult result) {
		index(diagram, graph);
	}

	private void index(Diagram diagram, Graph graph) {
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
			for (Connector connector : node.getConnectors()) {
				final RenderableEdge edge = edgesById.get(connector.getEdgeId());
				edge.getConnectors().add(connector);
			}
		}
		this.edgesById = Collections.unmodifiableMap(edgesById);
		this.edgesByReactomeId = Collections.unmodifiableMap(edgesByReactomeId);
		this.nodesById = Collections.unmodifiableMap(nodesById);
		this.nodesByReactomeId = Collections.unmodifiableMap(nodesByReactomeId);
		this.links = Collections.unmodifiableCollection(links);
		this.compartments = Collections.unmodifiableCollection(compartments);
		this.pathwaysById = Collections.unmodifiableMap(pathwaysById);
		this.pathwaysByReactomeId = Collections.unmodifiableMap(pathwaysByReactomeId);

		allNodes = new ArrayList<>();
		allNodes.addAll(compartments);
		allNodes.addAll(links);
		allNodes.addAll(edgesById.values());
		allNodes.addAll(nodesById.values());
		allNodes.addAll(pathwaysById.values());

		// These are here for flag compatibility
		graphNodesByReactomeId = Collections.unmodifiableMap(
				Stream.of(graph.getEdges(), graph.getNodes())
						.filter(Objects::nonNull)
						.flatMap(Collection::stream)
						.collect(toMap(GraphNode::getDbId, Function.identity(), (a, b) -> a)));

		subPathwaysByReactomeId = Collections.unmodifiableMap(
				graph.getSubpathways() == null
						? Collections.emptyMap()
						: graph.getSubpathways().stream()
						.collect(toMap(SubpathwayNode::getDbId, Function.identity(), (a, b) -> a)));
	}

	Map<Long, GraphNode> getGraphNodesByReactomeId() {
		return graphNodesByReactomeId;
	}

	Map<Long, SubpathwayNode> getSubPathwaysByReactomeId() {
		return subPathwaysByReactomeId;
	}

	public Map<Long, Collection<RenderableEdge>> getEdgesByReactomeId() {
		return edgesByReactomeId;
	}

	public Map<Long, RenderableEdge> getEdgesById() {
		return edgesById;
	}

	public Map<Long, Collection<RenderableNode>> getNodesByReactomeId() {
		return nodesByReactomeId;
	}

	public Map<Long, RenderableNode> getNodesById() {
		return nodesById;
	}

	public Map<Long, Collection<RenderableProcessNode>> getPathwaysByReactomeId() {
		return pathwaysByReactomeId;
	}

	public Map<Long, RenderableProcessNode> getPathwaysById() {
		return pathwaysById;
	}

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
