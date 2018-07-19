package org.reactome.server.tools.diagram.exporter.raster.diagram.common;

import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.graph.GraphNode;
import org.reactome.server.tools.diagram.data.graph.SubpathwayNode;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableDiagramObject;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableEdge;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableNode;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * Creates a RenderableObject per Node in the diagram. Computes all the
 * information that modifies each node basic rendering: selection, flag, halo
 * and analysis (enrichments and expressions). This data is not in the Node
 * class.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class DiagramIndex {

	private final DiagramDecorator decorator;
	private final DiagramAnalysis analysis;
	private Map<Long, Set<RenderableDiagramObject>> diagramByReactomeId;
	private Map<Long, RenderableDiagramObject> byId;
	private Map<Long, GraphNode> graphNodesById;
	private Map<String, GraphNode> graphByStId;

	private Map<Long, SubpathwayNode> subPathwaysById;
	private Map<String, SubpathwayNode> subPathwaysByStId;
	/*
	 * Diagram:
	 *  - RenderableEdge
	 *  - RenderableNode
	 *  - k: reactomeId
	 *  - k: id
	 *
	 * Graph:
	 *  - GraphNode
	 *  - SubpathwayNode
	 *  - k: stId
	 *  - k: dbId
	 */

	/**
	 * Creates a new DiagramIndex with the information for each node in maps.
	 *
	 * @param diagram diagram with nodes and reactions
	 * @param graph   background graph
	 */
	public DiagramIndex(Diagram diagram, Graph graph, RasterArgs args, AnalysisStoredResult result) {
		index(diagram, graph);
		decorator = new DiagramDecorator(this, args, graph, diagram);
		analysis = new DiagramAnalysis(result, this, args, graph, diagram);
	}

	private void index(Diagram diagram, Graph graph) {
		byId = Collections.unmodifiableMap(Stream.of(diagram.getEdges(), diagram.getNodes(), diagram.getLinks(), diagram.getCompartments())
				.flatMap(Collection::stream)
				.map(RenderableFactory::getRenderableObject)
				.collect(toMap(o -> o.getDiagramObject().getId(), Function.identity())));
		diagramByReactomeId = Collections.unmodifiableMap(byId.values().stream()
				.collect(groupingBy(o -> o.getDiagramObject().getId(), TreeMap::new, toSet())));
		// Add connectors to reactions, so they can be rendered together
		diagram.getNodes().stream()
				.map(Node::getConnectors)
				.flatMap(Collection::stream)
				.forEach(connector -> {
					final RenderableEdge edge = (RenderableEdge) getDiagramObjectsById().get(connector.getEdgeId());
					edge.getConnectors().add(connector);
				});

		graphNodesById = Collections.unmodifiableMap(
				Stream.of(graph.getEdges(), graph.getNodes())
						.filter(Objects::nonNull)
						.flatMap(Collection::stream)
						.collect(toMap(GraphNode::getDbId, Function.identity(), (a, b) -> a)));

		subPathwaysById = Collections.unmodifiableMap(
				graph.getSubpathways() == null
						? Collections.emptyMap()
						: graph.getSubpathways().stream()
						.collect(toMap(SubpathwayNode::getDbId, Function.identity(), (a, b) -> a)));

		graphByStId = Collections.unmodifiableMap(Stream.of(graph.getEdges(), graph.getNodes())
				.filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.collect(toMap(GraphNode::getStId, Function.identity(), (a, b) -> a)));

		subPathwaysByStId = Collections.unmodifiableMap(
				graph.getSubpathways() == null
						? Collections.emptyMap()
						: graph.getSubpathways().stream()
						.collect(toMap(SubpathwayNode::getStId, Function.identity(), (a, b) -> a)));
	}

	public Map<Long, GraphNode> getGraphIndex() {
		return graphNodesById;
	}

	public Map<Long, SubpathwayNode> getSubPathwaysById() {
		return subPathwaysById;
	}

	public Map<Long, RenderableDiagramObject> getDiagramObjectsById() {
		return byId;
	}

	public Map<Long, Set<RenderableDiagramObject>> getDiagramObjectsByReactomeId() {
		return diagramByReactomeId;
	}

	public Map<String, SubpathwayNode> getSubPathwaysByStId() {
		return subPathwaysByStId;
	}

	public Map<String, GraphNode> getGraphByStId() {
		return graphByStId;
	}

	public DiagramAnalysis getAnalysis() {
		return analysis;
	}

	public DiagramDecorator getDecorator() {
		return decorator;
	}

	public Collection<RenderableNode> getNodes() {
		return byId.values().stream()
				.filter(RenderableNode.class::isInstance)
				.map(RenderableNode.class::cast)
				.collect(toList());
	}

	public Collection<RenderableEdge> getReactions() {
		return byId.values().stream()
				.filter(RenderableEdge.class::isInstance)
				.map(RenderableEdge.class::cast)
				.collect(toList());
	}
}
