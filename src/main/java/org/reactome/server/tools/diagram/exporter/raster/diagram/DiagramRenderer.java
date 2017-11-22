package org.reactome.server.tools.diagram.exporter.raster.diagram;

import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.exporter.common.ResourcesFactory;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.RasterRenderer;
import org.reactome.server.tools.diagram.exporter.raster.TraceLogger;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.gif.AnimatedGifEncoder;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.NodeRenderInfo;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.*;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Logger;

/**
 * Renders Reactome pathway diagrams into <code>BufferedImage</code>s.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class DiagramRenderer implements RasterRenderer {

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
	private static final Logger log = Logger.getLogger(DiagramRenderer.class.getName());
	/**
	 * Max amount of allowed pixels per image
	 */
	private static final double MAX_IMAGE_SIZE = 1e8; // 100Mpixels
	private static final double MAX_GIF_SIZE = 1e7; // 10Mpixels
	private static final int MARGIN = 15;
	private static final Set<String> TRANSPARENT_FORMATS = new HashSet<>(Collections.singletonList("png"));
	private static final Set<String> NO_TRANSPARENT_FORMATS = new HashSet<>(Arrays.asList("jpg", "jpeg", "gif"));
	private static final int T = 0;
	private final Diagram diagram;
	private final DiagramIndex index;
	private final ColorProfiles colorProfiles;
	private final RasterArgs args;
	private DiagramCanvas canvas;
	private LegendRenderer legendRenderer;


	/**
	 * Creates a DiagramRenderer
	 *
	 * @param args        arguments for rendering
	 * @param diagramPath path where to find the json files for the layout and
	 *                    the graph data
	 *
	 * @throws DiagramJsonNotFoundException        if diagram is not found
	 * @throws DiagramJsonDeserializationException if diagram is malformed
	 * @throws AnalysisException                   when problems with the
	 *                                             analysis data
	 * @throws AnalysisServerError                 if some error happens in the
	 *                                             Analysis Server
	 */
	public DiagramRenderer(RasterArgs args, String diagramPath) throws DiagramJsonNotFoundException, DiagramJsonDeserializationException, AnalysisException, AnalysisServerError {
		TraceLogger.trace(args.getStId());
		final Graph graph = ResourcesFactory.getGraph(diagramPath, args.getStId());
		diagram = ResourcesFactory.getDiagram(diagramPath, args.getStId());
		this.args = args;
		this.colorProfiles = args.getProfiles();
		this.index = new DiagramIndex(diagram, graph, args);
		canvas = new DiagramCanvas();
		layout();
	}

	@Override
	public Dimension getDimension() {
		final Rectangle2D bounds = canvas.getBounds();
		double factor = args.getFactor();
		int width = (int) ((2 * MARGIN + bounds.getWidth()) * factor + 0.5);
		int height = (int) ((2 * MARGIN + bounds.getHeight()) * factor + 0.5);
		return new Dimension(width, height);
	}

	/**
	 * Renders an Image with given dimensions
	 *
	 * @return a RenderedImage with the given dimensions
	 */
	@Override
	public BufferedImage render() {
		final Rectangle2D bounds = canvas.getBounds();
//		double factor = limitFactor(bounds, MAX_IMAGE_SIZE);
		double factor = args.getFactor();
		int width = (int) ((2 * MARGIN + bounds.getWidth()) * factor + 0.5);
		int height = (int) ((2 * MARGIN + bounds.getHeight()) * factor + 0.5);
		int offsetX = (int) ((MARGIN - bounds.getMinX()) * factor + 0.5);
		int offsetY = (int) ((MARGIN - bounds.getMinY()) * factor + 0.5);

		final String ext = args.getFormat();
		final BufferedImage image = createImage(width, height, ext);
		final Graphics2D graphics = createGraphics(image, ext, factor, offsetX, offsetY);
		long start = System.nanoTime();
		canvas.render(graphics);
		TraceLogger.trace(String.format(Locale.UK, "[diagram exporter] %10.3f painting canvas%n", ((System.nanoTime() - start) / 1e6)));
		return image;
	}

	/**
	 * Animated GIF are generated into a temp File
	 */
	public void renderToAnimatedGif(OutputStream outputStream) throws IOException {
		if (index.getAnalysisType() != AnalysisType.EXPRESSION)
			throw new IllegalStateException("Only EXPRESSION analysis can be rendered into animated GIFs");

		final Rectangle2D bounds = canvas.getBounds();
//		double factor = limitFactor(bounds, MAX_GIF_SIZE);
		double factor = args.getFactor();
		int width = (int) ((2 * MARGIN + bounds.getWidth()) * factor + 0.5);
		int height = (int) ((2 * MARGIN + bounds.getHeight()) * factor + 0.5);
		int offsetX = (int) ((MARGIN - bounds.getMinX()) * factor + 0.5);
		int offsetY = (int) ((MARGIN - bounds.getMinY()) * factor + 0.5);

		final AnimatedGifEncoder encoder = new AnimatedGifEncoder();
		encoder.setDelay(1000);
		encoder.setRepeat(0);
//		encoder.setQuality(1);
		encoder.start(outputStream);
		for (int t = 0; t < index.getExpressionColumnNames().size(); t++) {
			final BufferedImage image = frame(factor, width, height, offsetX, offsetY, t);
			encoder.addFrame(image);
		}
		encoder.finish();
	}

	private BufferedImage frame(double factor, int width, int height, int offsetX, int offsetY, int t) {
		canvas.getNodeAnalysis().clear();
		// Update expression layer
		diagram.getNodes().forEach(node -> {
			final NodeAbstractRenderer renderer = (NodeAbstractRenderer) RendererFactory.get(node.getRenderableClass());
			// TODO: this operation is costly, should we maintain all the NodeRenderInfo in an index
			final NodeRenderInfo info = new NodeRenderInfo(node, index, colorProfiles, canvas, renderer);
			renderer.expression(colorProfiles, index, info, t);
		});
		// Update legend
		legendRenderer.setCol(t);
		final BufferedImage image = createImage(width, height, "gif");
		final Graphics2D graphics = createGraphics(image, "gif", factor, offsetX, offsetY);
		canvas.render(graphics);
		return image;
	}

	private double limitFactor(Rectangle2D bounds, double maxSize) {
		final double width = args.getFactor() * (MARGIN + bounds.getWidth());
		final double height = args.getFactor() * (MARGIN + bounds.getHeight());
		double size = width * height;
		if (size > maxSize) {
			final double newFactor = Math.sqrt(maxSize / ((MARGIN + bounds.getWidth()) * (MARGIN + bounds.getHeight())));
			log.warning(String.format(
					"Diagram %s is too large. Quality reduced from %.2f to %.2f -> (%d x %d)",
					diagram.getStableId(), args.getFactor(), newFactor, (int) (bounds.getWidth() * newFactor), (int) (bounds.getHeight() * newFactor)));
			return newFactor;
		}
		return args.getFactor();
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
	                                  double factor, double offsetX, double offsetY) {
		final Graphics2D graphics = image.createGraphics();
		if (NO_TRANSPARENT_FORMATS.contains(ext)) {
			Color bgColor = args.getBackground() == null
					? Color.WHITE
					: args.getBackground();
			graphics.setBackground(bgColor);
			graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
		}

		// This transformation allows elements to use their own dimensions,
		// isn't it nice?
		graphics.translate(offsetX, offsetY);
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
		legend();
	}

	private void compartments() {
		final CompartmentRenderer renderer = new CompartmentRenderer();
		renderer.draw(canvas, diagram.getCompartments(), colorProfiles, index);
	}

	private void links() {
		diagram.getLinks().forEach(link ->
				RendererFactory.get(link.getRenderableClass())
						.draw(canvas, link, colorProfiles, index, T));
	}

	private void nodes() {
		diagram.getNodes().forEach(node ->
				RendererFactory.get(node.getRenderableClass())
						.draw(canvas, node, colorProfiles, index, T));
	}

	private void edges() {
		final EdgeRenderer renderer = new EdgeRenderer();
		diagram.getEdges().forEach(edge ->
				renderer.draw(canvas, edge, colorProfiles, index, T));
	}

	private void notes() {
		final NoteRenderer renderer = new NoteRenderer();
		diagram.getNotes().forEach(note -> renderer.draw(canvas, note, colorProfiles));
	}

	private void legend() {
		legendRenderer = new LegendRenderer(canvas, index, colorProfiles);
		if (index.getAnalysisType() == AnalysisType.EXPRESSION) {
			// We add the legend first, so the logo is aligned to the right margin
			legendRenderer.addLegend();
			legendRenderer.addLogo();
			if (args.getColumn() != null) {
				legendRenderer.setCol(args.getColumn());
			} else if (!args.getFormat().equals("gif"))
				legendRenderer.setCol(0);
		} else if (index.getAnalysisType() != AnalysisType.NONE) {
			legendRenderer.addLogo();
			legendRenderer.infoText();
		} else legendRenderer.addLogo();
	}

}
