package org.reactome.server.tools.diagram.exporter.raster.diagram.common;

import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;

/**
 * Wrapper of all of the extra data to ease the rendering and improve the performance. Refer to each of
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class DiagramData {

	private final DiagramIndex index;
	private final DiagramDecorator decorator;
	private final DiagramAnalysis analysis;

	/**
	 * Generates an index, a decorator and an analysis of the diagram.
	 *
	 * @param diagram the diagram to index
	 * @param graph   the underlying graph
	 * @param args    args for exporting
	 * @param result  analysis to overlay
	 */
	public DiagramData(Diagram diagram, Graph graph, RasterArgs args, AnalysisStoredResult result) {
		this.index = new DiagramIndex(diagram, graph, args, result);
		this.decorator = new DiagramDecorator(index, args, graph);
		this.analysis = new DiagramAnalysis(index, graph, args, result);
	}

	public DiagramAnalysis getAnalysis() {
		return analysis;
	}

	public DiagramDecorator getDecorator() {
		return decorator;
	}

	public DiagramIndex getIndex() {
		return index;
	}
}
