package org.reactome.server.tools.diagram.exporter.svg;

import org.apache.batik.anim.dom.SVG12DOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.SVGConstants;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.exporter.common.ResourcesFactory;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.CompartmentRenderer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.LegendRenderer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.NoteRenderer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.svg.SVGDocument;

import java.awt.geom.Rectangle2D;

public class SVGExporter {

	private static final DOMImplementation SVG_IMPL = SVG12DOMImplementation.getDOMImplementation();
	private static final int T = 0;
	private static Diagram diagram;
	private static DiagramCanvas canvas;
	//	private final SVGDocument document;
	private final RasterArgs args;
	private final ColorProfiles colorProfiles;
	private final DiagramIndex index;
	private String title;
	private SVGDocument document;

	SVGExporter(RasterArgs args, String diagramPath, String EHLDPath) throws DiagramJsonNotFoundException, DiagramJsonDeserializationException, AnalysisException, AnalysisServerError {
		document = (SVGDocument) SVG_IMPL.createDocument(SVGConstants.SVG_NAMESPACE_URI, "svg", null);
		final Graph graph = ResourcesFactory.getGraph(diagramPath, args.getStId());
		diagram = ResourcesFactory.getDiagram(diagramPath, args.getStId());
		this.title = args.getWriteTitle() != null && args.getWriteTitle()
				? diagram.getDisplayName()
				: null;
		this.args = args;
		this.colorProfiles = args.getProfiles();
		this.index = new DiagramIndex(diagram, graph, args);
		canvas = new DiagramCanvas();
		layout();
		final SVGGraphics2D graphics2D = new SVGGraphics2D(document);
		canvas.render(graphics2D);
		document.removeChild(document.getRootElement());
		document.appendChild(graphics2D.getRoot());
		final Rectangle2D bounds = canvas.getBounds();
		final String viewBox = String.format("%.0f %.0f %.0f %.0f", bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
		document.getRootElement().setAttribute(SVGConstants.SVG_VIEW_BOX_ATTRIBUTE, viewBox);
	}


	public SVGDocument export() {
		return document;
	}

	private void layout() {
		compartments();
		nodes();
		notes();
		edges();
		legend();
	}

	private void compartments() {
		final CompartmentRenderer renderer = new CompartmentRenderer();
		renderer.draw(canvas, diagram.getCompartments(), colorProfiles, index);
	}

	private void nodes() {
		index.getNodes().forEach(node -> node.render(canvas, colorProfiles, index, T));
	}

	private void edges() {
		index.getEdges().forEach(edge -> edge.render(canvas, colorProfiles, index));
	}

	private void notes() {
		final NoteRenderer renderer = new NoteRenderer();
		diagram.getNotes().forEach(note -> renderer.draw(canvas, note, colorProfiles));
	}

	private void legend() {
		LegendRenderer legendRenderer = new LegendRenderer(canvas, index, colorProfiles);
		if (index.getAnalysis().getType() == AnalysisType.EXPRESSION) {
			// We add the legend first, so the logo is aligned to the right margin
			legendRenderer.addLegend();
			legendRenderer.addLogo();
			if (args.getColumn() != null) {
				legendRenderer.setCol(args.getColumn(), title);
			} else if (!args.getFormat().equals("gif"))
				legendRenderer.setCol(0, title);
		} else {
			legendRenderer.addLogo();
			legendRenderer.infoText(title);
		}
	}

}
