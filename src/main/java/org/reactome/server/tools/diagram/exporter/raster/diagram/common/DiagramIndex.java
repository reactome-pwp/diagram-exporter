package org.reactome.server.tools.diagram.exporter.raster.diagram.common;

import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableEdge;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
	private final Map<Long, RenderableNode> nodes = new HashMap<>();
	private final Map<Long, RenderableEdge> edges = new HashMap<>();
	private DiagramAnalysis analysis;

	/**
	 * Creates a new DiagramIndex with the information for each node in maps.
	 *
	 * @param diagram diagram with nodes and reactions
	 * @param graph   background graph
	 */
	public DiagramIndex(Diagram diagram, Graph graph, RasterArgs args) throws Exception {
		index(diagram);
		decorator = new DiagramDecorator(this, args, graph, diagram);
		analysis = new DiagramAnalysis(args.getToken(), this, args, graph, diagram);

	}

	public DiagramIndex(Diagram diagram, Graph graph, RasterArgs args, AnalysisStoredResult result) {
		index(diagram);
		decorator = new DiagramDecorator(this, args, graph, diagram);
		analysis = new DiagramAnalysis(result, this, args, graph, diagram);
	}

	private void index(Diagram diagram) {
		diagram.getNodes().forEach(node -> nodes.put(node.getId(), RenderableFactory.getRenderableNode(node)));
		diagram.getEdges().forEach(edge -> edges.put(edge.getId(), RenderableFactory.getRenderableEdge(edge)));
		diagram.getLinks().forEach(link -> edges.put(link.getId(), RenderableFactory.getRenderableEdge(link)));
		// Add connectors to reactions, so they can be rendered together
		diagram.getNodes().stream()
				.map(Node::getConnectors)
				.flatMap(Collection::stream)
				.forEach(connector -> getEdge(connector.getEdgeId()).getConnectors().add(connector));
	}

	public RenderableNode getNode(Long id) {
		return nodes.get(id);
	}

	public Collection<RenderableNode> getNodes() {
		return nodes.values();
	}

	public RenderableEdge getEdge(Long id) {
		return edges.get(id);
	}

	public Collection<RenderableEdge> getEdges() {
		return edges.values();
	}

	public DiagramAnalysis getAnalysis() {
		return analysis;
	}

	public DiagramDecorator getDecorator() {
		return decorator;
	}

}
