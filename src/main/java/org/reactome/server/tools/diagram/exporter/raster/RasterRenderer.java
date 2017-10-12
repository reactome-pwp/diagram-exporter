package org.reactome.server.tools.diagram.exporter.raster;

import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.profile.analysis.AnalysisProfile;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.data.profile.interactors.InteractorProfile;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.*;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.*;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Renders Reactome pathway diagrams into <code>BufferedImage</code>s.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class RasterRenderer {

	/*
	 * physical size of image using the best print quality (600 ppi).
	 * magazines are printed at 300 ppi
	 *
	 * MAX_IMG_SIZE | memory |  eur         | uk
	 * -------------|--------|--------------|-------------
	 *  1e7         |  40MB  | 29cm x 29cm  | 11” x 11” (10Mp, as smart phones)
	 *  2.5e7       | 100MB  | 36cm x 36cm  | 14” x 14”
	 *  5e7         | 200MB  | 43cm x 43cm  | 17” x 17”
	 *  1e8         | 400MB  | 51cm x 51cm  | 20” x 20”
	 *  2e8         | 800MB  | 61cm x 61cm  | 24” x 24”
	 */
	/**
	 * Max amount of allowed pixels per image
	 */
	private static final double MAX_IMAGE_SIZE = 1e8; // 100Mpixels
	private static final int MARGIN = 15;
	private final Diagram diagram;
	private final DiagramProfile diagramProfile;
	private final AnalysisProfile analysisProfile;
	private final InteractorProfile interactorProfile;
	private DiagramIndex index;
	private DiagramCanvas canvas;
	private double factor;

	/**
	 * Creates a RasterRenderer with specific diagram, graph, diagram and
	 * decorator.
	 *
	 * @param diagram           diagram to render
	 * @param graph             underlying graph
	 * @param decorator         elements to decorate
	 * @param diagramProfile    colouring diagram
	 * @param analysisProfile   profile for analysis
	 * @param interactorProfile profile for interactors
	 */
	RasterRenderer(Diagram diagram, Graph graph, Decorator decorator, DiagramProfile diagramProfile, AnalysisProfile analysisProfile, InteractorProfile interactorProfile) {
		this.diagram = diagram;
		this.diagramProfile = diagramProfile;
		this.analysisProfile = analysisProfile;
		this.interactorProfile = interactorProfile;
		this.index = new DiagramIndex(diagram, graph, decorator, AnalysisType.NONE);
	}

	/**
	 * Renders an Image with given dimensions
	 *
	 * @param factor scale of the image
	 * @param ext    file format. To select the proper in-memory structure
	 *
	 * @return a RenderedImage with the given dimensions
	 */
	public BufferedImage render(double factor, String ext) {
		this.factor = factor;

		// Bounds are recalculated reading nodes, we don't trust diagram bounds
		final double minX = getMinX();
		final double maxX = getMaxX();
		final double minY = getMinY();
		final double maxY = getMaxY();

		final double diagramWidth = maxX - minX;
		final double diagramHeight = maxY - minY;

		double width = factor * (diagramWidth + 2 * MARGIN);
		double height = factor * (diagramHeight + 2 * MARGIN);

		// Limit the size of the images by reducing the factor until it fits
		// into the MAX_IMAGE_SIZE, thus ensuring that a maximum of
		// MAX_IMAGE_SIZE pixels are stored in memory.
		double newFactor = factor;
		while (height * width > MAX_IMAGE_SIZE && newFactor > 1) {
			newFactor -= 0.1;
			width = newFactor * (diagramWidth + 2 * MARGIN);
			height = newFactor * (diagramHeight + 2 * MARGIN);
		}
		if (newFactor < factor) {
			Logger.getLogger(getClass().getName())
					.warning(String.format(
							"Image too large. Quality reduced from %.1f to %.1f -> %.0f x %.0f = %.0f (%.2f MP)",
							factor, newFactor, height, width, height * width, height * width / 1e6));
			factor = newFactor;
		}

		FontProperties.setFactor(factor);
		RendererProperties.setFactor(factor);
		StrokeProperties.setFactor(factor);

		final double x = minX * factor;
		final double y = minY * factor;
		final double margin = factor * MARGIN;
		final AdvancedGraphics2D graphics = new AdvancedGraphics2D(width, height, x, y, margin, ext);
		graphics.getGraphics().setFont(FontProperties.DEFAULT_FONT);


		canvas = new DiagramCanvas();
		compartments();
		links();
		nodes();
		notes();
		edges();
		canvas.render(graphics.getGraphics());
		return graphics.getImage();
	}

	private double getMinY() {
		return streamObjects()
				.mapToDouble(DiagramObject::getMinY)
				.min().orElse(0);
	}

	private double getMinX() {
		return streamObjects()
				.mapToDouble(DiagramObject::getMinX)
				.min().orElse(0);
	}

	private double getMaxX() {
		return streamObjects()
				.mapToDouble(DiagramObject::getMaxX)
				.max().orElse(0);
	}

	private double getMaxY() {
		return streamObjects()
				.mapToDouble(DiagramObject::getMaxY)
				.max().orElse(0);
	}

	private Stream<DiagramObject> streamObjects() {
		return Stream.of(
				diagram.getCompartments(),
				diagram.getLinks(),
				diagram.getEdges(),
				diagram.getNodes())
				.flatMap(Collection::stream);
	}

	private void compartments() {
		final CompartmentRenderer renderer = new CompartmentRenderer();
		renderer.draw(canvas, diagram.getCompartments(), diagramProfile, factor);
	}

	private void links() {
		diagram.getLinks().forEach(link -> {
			final EdgeRenderer renderer = (EdgeRenderer) RendererFactory.get(link.getRenderableClass());
			renderer.draw(canvas, link, diagramProfile, factor, index);
		});
	}

	private void nodes() {
		diagram.getNodes().forEach(node ->
				RendererFactory.get(node.getRenderableClass())
						.draw(canvas, node, diagramProfile, analysisProfile, interactorProfile, factor, index));
	}

	private void edges() {
		final ReactionRenderer renderer = new ReactionRenderer();
		diagram.getEdges().forEach(edge -> renderer.draw(canvas, edge, diagramProfile, factor, index));
		final ConnectorRenderer connectorRenderer = new ConnectorRenderer();
		diagram.getNodes().stream()
				.map(Node::getConnectors)
				.flatMap(Collection::stream)
				.forEach(connector -> connectorRenderer.draw(canvas, connector, diagramProfile, factor, index));

	}

	private void notes() {
		final NoteRenderer renderer = new NoteRenderer();
		diagram.getNotes().forEach(note -> renderer.draw(canvas, note, diagramProfile, analysisProfile, interactorProfile, factor, index));
	}
}
