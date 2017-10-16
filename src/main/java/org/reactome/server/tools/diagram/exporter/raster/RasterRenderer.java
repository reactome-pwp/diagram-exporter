package org.reactome.server.tools.diagram.exporter.raster;

import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.profile.analysis.AnalysisProfile;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.data.profile.interactors.InteractorProfile;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.CompartmentRenderer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.EdgeRenderer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.NoteRenderer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.RendererFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Renders Reactome pathway diagrams into <code>BufferedImage</code>s.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class RasterRenderer {

	private static final Logger log = Logger.getLogger(RasterRenderer.class.getName());

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
	private static final Set<String> TRANSPARENT_FORMATS = new HashSet<>(Collections.singletonList("png"));
	private static final Set<String> NO_TRANSPARENT_FORMATS = new HashSet<>(Arrays.asList("jpg", "jpeg", "gif"));

	private final Diagram diagram;
	private final DiagramProfile diagramProfile;
	private final AnalysisProfile analysisProfile;
	private final InteractorProfile interactorProfile;
	private final DiagramIndex index;
	private DiagramCanvas canvas;

	/**
	 * Creates a RasterRenderer with specific diagram, graph, color profile and
	 * decorator.
	 *
	 * @param diagram           diagram to render
	 * @param graph             underlying graph
	 * @param decorator         elements to decorate
	 * @param diagramProfile    colouring diagram
	 * @param analysisProfile   profile for analysis
	 * @param interactorProfile profile for interactors
	 */
	RasterRenderer(Diagram diagram, Graph graph, Decorator decorator,
	               DiagramProfile diagramProfile,
	               AnalysisProfile analysisProfile,
	               InteractorProfile interactorProfile) {
		this.diagram = diagram;
		this.diagramProfile = diagramProfile;
		this.analysisProfile = analysisProfile;
		this.interactorProfile = interactorProfile;
		this.index = new DiagramIndex(diagram, graph, decorator);
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

		// Bounds are recalculated reading nodes, we don't trust diagram bounds
		final double minX = getMinX();
		final double maxX = getMaxX();
		final double minY = getMinY();
		final double maxY = getMaxY();

		final double diagramWidth = maxX - minX + 2 * MARGIN;
		final double diagramHeight = maxY - minY + 2 * MARGIN;

		int width = (int) (factor * diagramWidth);
		int height = (int) (factor * diagramHeight);

		// Limit the size of the images by reducing the factor until it fits
		// into the MAX_IMAGE_SIZE, thus ensuring that a maximum of
		// MAX_IMAGE_SIZE pixels are stored in memory.
		final double size = height * width;
		if (size > MAX_IMAGE_SIZE) {
			final double newFactor = Math.sqrt((MAX_IMAGE_SIZE / (diagramHeight * diagramWidth)));
			width = (int) (newFactor * diagramWidth);
			height = (int) (newFactor * diagramHeight);
			log.warning(String.format(
					"Diagram %s too large. Quality reduced from %.1f to %.2f -> %d x %d = %d (%.2f MP)",
					diagram.getStableId(), factor, newFactor, height, width, height * width, height * width / 1e6));
			factor = newFactor;
		}

		// Virtual canvas: full of layers
		canvas = new DiagramCanvas();
		layout();
		final BufferedImage image = createImage(width, height, ext.toLowerCase());
		final Graphics2D graphics = createGraphics(image, ext.toLowerCase(), factor, minX, minY, MARGIN);
		canvas.render(graphics);
		return image;
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

	private BufferedImage createImage(int width, int height, String ext) {
		if (TRANSPARENT_FORMATS.contains(ext))
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		else if (NO_TRANSPARENT_FORMATS.contains(ext))
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		else
			throw new IllegalArgumentException("Unsupported file extension " + ext);
	}

	private Graphics2D createGraphics(BufferedImage image, String ext,
	                                  double factor, double x, double y,
	                                  double margin) {
		final Graphics2D graphics = image.createGraphics();
		if (NO_TRANSPARENT_FORMATS.contains(ext)) {
			graphics.setBackground(Color.WHITE);
			graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
		}
		// These 3 lines are really important, they move the canvas to the minx,
		// miny of the diagram and scale it.
		// Now we can draw elements with their own dimensions, isn't it nice?
		graphics.translate(margin * factor, margin * factor);
		graphics.translate((int) -x * factor, (int) -y * factor);
		graphics.scale(factor, factor);

		graphics.setFont(FontProperties.DEFAULT_FONT);
		graphics.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		return graphics;
	}

	private void layout() {
		compartments();
		links();
		nodes();
		notes();
		edges();
	}

	private void compartments() {
		final CompartmentRenderer renderer = new CompartmentRenderer();
		renderer.draw(canvas, diagram.getCompartments(), diagramProfile);
	}

	private void links() {
		diagram.getLinks().forEach(link ->
				RendererFactory.get(link.getRenderableClass())
						.draw(canvas, link, diagramProfile, analysisProfile, interactorProfile, index));
	}

	private void nodes() {
		diagram.getNodes().forEach(node ->
				RendererFactory.get(node.getRenderableClass())
						.draw(canvas, node, diagramProfile, analysisProfile, interactorProfile, index));
	}

	private void edges() {
		final EdgeRenderer renderer = new EdgeRenderer();
		diagram.getEdges().forEach(edge ->
				renderer.draw(canvas, edge, diagramProfile, analysisProfile, interactorProfile, index));
	}

	private void notes() {
		final NoteRenderer renderer = new NoteRenderer();
		diagram.getNotes().forEach(note -> renderer.draw(canvas, note, diagramProfile));
	}
}
