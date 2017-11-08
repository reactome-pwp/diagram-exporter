package org.reactome.server.tools.diagram.exporter.raster.diagram;

import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.exporter.common.ResourcesFactory;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.gif.Giffer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.NodeRenderInfo;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Renders Reactome pathway diagrams into <code>BufferedImage</code>s.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class DiagramRenderer {

	private static final int LEGEND_WIDTH = 70;
	private static final int LEGEND_HEIGHT = 350;

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
	private static final double MAX_GIF_SIZE = 5e7; // 100Mpixels
	private static final int MARGIN = 15;
	private static final Set<String> TRANSPARENT_FORMATS = new HashSet<>(Collections.singletonList("png"));
	private static final Set<String> NO_TRANSPARENT_FORMATS = new HashSet<>(Arrays.asList("jpg", "jpeg", "gif"));
	private static final int T = 0;
	private final Diagram diagram;
	private final DiagramIndex index;
	private final ColorProfiles colorProfiles;
	private final RasterArgs args;
	private DiagramCanvas canvas;
	private double legend_height;
	private double legend_width;


	/**
	 * Creates a DiagramRenderer
	 *
	 * @param args arguments for rendering
	 *
	 * @param diagramPath
	 * @throws DiagramJsonNotFoundException if diagram is not found
	 * @throws DiagramJsonDeserializationException if diagram is malformed
	 */
	public DiagramRenderer(RasterArgs args, String diagramPath) throws DiagramJsonNotFoundException, DiagramJsonDeserializationException {
		final Graph graph = ResourcesFactory.getGraph(diagramPath, args.getStId());
		diagram = ResourcesFactory.getDiagram(diagramPath, args.getStId());
		this.args = args;
		this.colorProfiles = args.getProfiles();
		this.index = new DiagramIndex(diagram, graph, args);
	}

	/**
	 * Renders an Image with given dimensions
	 *
	 * @return a RenderedImage with the given dimensions
	 */
	public BufferedImage render() {
		double factor = args.getFactor();
		final RasterProperties rasterProperties = new RasterProperties(factor, MAX_IMAGE_SIZE);
		int width = (int) rasterProperties.getWidth();
		int height = (int) rasterProperties.getHeight();
		factor = rasterProperties.getFactor();
		double minX = rasterProperties.minX();
		double minY = rasterProperties.minY();

		// Virtual canvas: full of layers
		canvas = new DiagramCanvas();
		layout();
		// Memory eater
		final String ext = args.getFormat().toLowerCase();
		final BufferedImage image = createImage(width, height, ext);
		final Graphics2D graphics = createGraphics(image, ext, factor, minX, minY, MARGIN);
		canvas.render(graphics);
		return image;
	}

	/**
	 * Animated GIF are generated into a temp File
	 */
	public void renderToAnimatedGif(OutputStream outputStream) throws IOException {
		if (index.getAnalysisType() != AnalysisType.EXPRESSION)
			throw new IllegalStateException("Only EXPRESSION analysis can be rendered into animated GIFs");
		double factor = args.getFactor();
		final RasterProperties rasterProperties = new RasterProperties(factor, MAX_GIF_SIZE);
		int width = (int) rasterProperties.getWidth();
		int height = (int) rasterProperties.getHeight();
		double minX = rasterProperties.minX();
		double minY = rasterProperties.minY();
		factor = rasterProperties.getFactor();

		canvas = new DiagramCanvas();
		layout();

		final Giffer encoder = new Giffer();
//		final AnimatedGifEncoder encoder = new AnimatedGifEncoder();
//		encoder.setDelay(1000);
//		encoder.setRepeat(0);
//		encoder.setQuality(1);
		encoder.start(outputStream);
		for (int t = 0; t < index.getExpressionSize(); t++) {
			System.out.println(t);
			canvas.getNodeAnalysis().clear();
			canvas.getLegendTickArrows().clear();
			canvas.getLegendTicks().clear();
			canvas.getLegendBar().clear();
			canvas.getLegendText().clear();
			canvas.getLegendBackground().clear();
			final int T = t;
			// We render only the expression layer
			diagram.getNodes().forEach(node -> {
				final NodeAbstractRenderer renderer = (NodeAbstractRenderer) RendererFactory.get(node.getRenderableClass());
				final NodeRenderInfo info = new NodeRenderInfo(node, index, colorProfiles, canvas, renderer);
				renderer.expression(colorProfiles, index, info, T);
			});
			// And the legend
			legend(t);
			// Memory eater
			final BufferedImage image = createImage(width, height, "gif");
			final Graphics2D graphics = createGraphics(image, "gif", factor, minX, minY, MARGIN);
			canvas.render(graphics);
			encoder.addFrame(image);
		}
		encoder.finish();
	}

	private double getDiagramMinY() {
		return streamObjects()
				.mapToDouble(DiagramObject::getMinY)
				.min().orElse(0);
	}

	private double getDiagramMinX() {
		return streamObjects()
				.mapToDouble(DiagramObject::getMinX)
				.min().orElse(0);
	}

	private double getDiagramMaxX() {
		return streamObjects()
				.mapToDouble(DiagramObject::getMaxX)
				.max().orElse(0);
	}

	private double getDiagramMaxY() {
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
		// These 3 lines are really important, they add the margin, move the
		// canvas to the minx, miny of the diagram and scale it.
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
		legend(T);
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

	private void legend(int t) {
		if (index.getAnalysisType() == AnalysisType.EXPRESSION) {
			LegendRenderer.addLegend(canvas, colorProfiles, diagram, MARGIN, legend_width, legend_height, index, t);
		}
	}

	private class RasterProperties {
		private double factor;
		private double minX;
		private double minY;
		private long width;
		private long height;

		public RasterProperties(double factor, double maxSize) {
			this.factor = factor;
			// Bounds are recalculated reading nodes, we don't trust diagram bounds
			minX = getDiagramMinX();
			minY = getDiagramMinY();
			final double maxX = getDiagramMaxX();
			final double maxY = getDiagramMaxY();

			final double diagramHeight = maxY - minY + 2 * MARGIN;

			legend_width = 0;
			legend_height = 0;
			if (index.getAnalysisType() == AnalysisType.EXPRESSION) {
				legend_height = LEGEND_HEIGHT;
				if (diagramHeight < LEGEND_HEIGHT) {
					legend_height = diagramHeight;
					legend_width = LEGEND_WIDTH * legend_height / LEGEND_HEIGHT;
				} else legend_width = LEGEND_WIDTH;
			}

			final double diagramWidth = maxX - minX + 2 * MARGIN + legend_width;

			width = (long) (factor * diagramWidth);
			height = (long) (factor * diagramHeight);

			// Limit the size of the images by reducing the factor until it fits
			// into the MAX_IMAGE_SIZE, thus ensuring that a maximum of
			// MAX_IMAGE_SIZE pixels are stored in memory.
			final long size = height * width;
			if (size > maxSize) {
				final double newFactor = Math.sqrt((maxSize / (diagramHeight * diagramWidth)));
				width = (long) (newFactor * diagramWidth);
				height = (long) (newFactor * diagramHeight);
				log.warning(String.format(
						"Diagram %s is too large. Quality reduced from %.1f to %.2f -> %d x %d = %d (%.2f MP)",
						diagram.getStableId(), factor, newFactor, height, width, height * width, height * width / 1e6));
				this.factor = newFactor;
			}
		}

		public double getFactor() {
			return factor;
		}

		public double minX() {
			return minX;
		}

		public double minY() {
			return minY;
		}

		public long getWidth() {
			return width;
		}

		public long getHeight() {
			return height;
		}

	}
}
