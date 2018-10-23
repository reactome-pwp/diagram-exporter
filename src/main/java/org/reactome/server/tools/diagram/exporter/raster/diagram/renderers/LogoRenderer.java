package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers;

import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableCompartment;
import org.reactome.server.tools.diagram.exporter.raster.resources.Resources;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class LogoRenderer {
	/**
	 * For measuring text width
	 */
	private static final FontMetrics FONT_METRICS = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB)
			.createGraphics().getFontMetrics(FontProperties.DEFAULT_FONT);

	private static final double LOGO_PADDING = 5;

	private static final double RELATIVE_LOGO_WIDTH = 0.1;
	private static final double MIN_LOGO_WIDTH = 50;
	private static final int LEGEND_TO_DIAGRAM_SPACE = 15;
	private static final double COMPARTMENT_PADDING = 15;

	private  LogoRenderer() {
	}

	/**
	 * Adds a logo in the bottom right corner of the canvas.
	 *
	 * @param args the exporter arguments
	 */
	public static NodeProperties addLogo(DiagramCanvas canvas, RasterArgs args, Diagram diagram) {
		final Rectangle2D bounds = canvas.getBounds();
		final BufferedImage logo = getLogo();
		double logoWidth = bounds.getWidth() * RELATIVE_LOGO_WIDTH;
		if (logoWidth > logo.getWidth()) logoWidth = logo.getWidth();
		if (logoWidth < MIN_LOGO_WIDTH) logoWidth = MIN_LOGO_WIDTH;
		final double logoHeight = logoWidth / logo.getWidth() * logo.getHeight();
		if (!args.getWriteTitle()) {
			final NodeProperties limits = findLogoPlace(bounds, diagram, logoWidth, logoHeight);
			if (limits != null) {
				canvas.getLogoLayer().add(logo, limits);
				return limits;
			}
		}
		// This point will be reached only if logo couldn't be written inside the diagram or if writeTitle is true
		final NodeProperties limits = NodePropertiesFactory.get(
				bounds.getMaxX() - logoWidth,
				bounds.getMaxY() + LEGEND_TO_DIAGRAM_SPACE,
				logoWidth, logoHeight);
		canvas.getLogoLayer().add(logo, limits);
		// Now we can reserve the rest of space for the text
//		createBottomTextBox(logoWidth, logoHeight);
		return limits;
	}

	private static NodeProperties findLogoPlace(Rectangle2D bounds, Diagram diagram, double width, double height) {
		// Let's find a nice place inside the diagram
		// +----------+
		// |  4  6  3 |
		// |  8  9  7 |
		// |  2  5  1 |
		// +----------+
		final double x = bounds.getX() + LOGO_PADDING;
		final double y = bounds.getY() + LOGO_PADDING;
		final double mx = bounds.getMaxX() - LOGO_PADDING - width;
		final double my = bounds.getMaxY() - LOGO_PADDING - height;
		final double cx = bounds.getCenterX() - 0.5 * width;
		final double cy = bounds.getCenterY() - 0.5 * height;
		final List<Rectangle2D> positions = Arrays.asList(
				new Rectangle2D.Double(mx, my, width, height),
				new Rectangle2D.Double(mx - COMPARTMENT_PADDING, my - COMPARTMENT_PADDING, width, height),
				new Rectangle2D.Double(x, my, width, height),
				new Rectangle2D.Double(x + COMPARTMENT_PADDING, my - COMPARTMENT_PADDING, width, height),
				new Rectangle2D.Double(mx, y, width, height),
				new Rectangle2D.Double(mx - COMPARTMENT_PADDING, y + COMPARTMENT_PADDING, width, height),
				new Rectangle2D.Double(x, y, width, height),
				new Rectangle2D.Double(x+ COMPARTMENT_PADDING, y + COMPARTMENT_PADDING, width, height),
				new Rectangle2D.Double(cx, my, width, height),
				new Rectangle2D.Double(cx - COMPARTMENT_PADDING, my - COMPARTMENT_PADDING, width, height),
				new Rectangle2D.Double(cx, y, width, height),
				new Rectangle2D.Double(cx - COMPARTMENT_PADDING, y + COMPARTMENT_PADDING, width, height),
				new Rectangle2D.Double(mx, cy, width, height),
				new Rectangle2D.Double(mx - COMPARTMENT_PADDING, cy, width, height),
				new Rectangle2D.Double(x, cy, width, height),
				new Rectangle2D.Double(x + COMPARTMENT_PADDING, cy, width, height),
				new Rectangle2D.Double(mx, cy, width, height),
				new Rectangle2D.Double(mx - COMPARTMENT_PADDING, cy, width, height));
		for (Rectangle2D position : positions) {
			if (!anyInside(diagram, position)) {
				return NodePropertiesFactory.get(position.getX(), position.getY(), width, height);
			}
		}
		// None of the positions is valid
		return null;
	}

	private static boolean anyInside(Diagram diagram, Rectangle2D position) {
		// -2 for rounding issues
		final double area = position.getHeight() * position.getWidth() - 2;
		// Nodes
		for (Node node : diagram.getNodes()) {
			if (position.intersects(toRectangle(node.getProp()))) return true;
			for (Connector connector : node.getConnectors()) {
				for (Segment segment : connector.getSegments()) {
					if (position.intersectsLine(toLine(segment))) return true;
				}
			}
		}
		for (Edge edge : diagram.getEdges()) {
			final double w = edge.getMaxX() - edge.getMinX();
			final double h = edge.getMaxY() - edge.getMinY();
			if (position.intersects(edge.getMinX(), edge.getMinY(), w, h)) return true;
		}
		for (Link link : diagram.getLinks()) {
			for (Segment segment : link.getSegments()) {
				final Line2D.Double line = toLine(segment);
				if (position.intersectsLine(line)) return true;
			}
		}
		// Compartments
		for (Compartment compartment : diagram.getCompartments()) {
			final Rectangle2D.Double outer = toRectangle(compartment.getProp());
			Rectangle2D intersection = position.createIntersection(outer);
			double intersectionArea = intersection.getWidth() * intersection.getHeight();
			if (!intersection.isEmpty() && intersectionArea > 0 && intersectionArea < area) return true;
			if (compartment.getInsets() != null) {
				final Rectangle2D.Double inner = toRectangle(compartment.getInsets());
				intersection = position.createIntersection(inner);
				intersectionArea = intersection.getWidth() * intersection.getHeight();
				if (!intersection.isEmpty() && intersectionArea > 0 && intersectionArea < area) return true;
			}
			// And we also check for text
			final double tw = FONT_METRICS.stringWidth(compartment.getDisplayName());
			final double th = FONT_METRICS.getHeight();
			final double tx = compartment.getTextPosition().getX() + RenderableCompartment.GWU_CORRECTION.getX();
			final double ty = compartment.getTextPosition().getY() + RenderableCompartment.GWU_CORRECTION.getY();
			if (position.intersects(tx, ty, tw, th)) return true;
		}
		return false;
	}

	private static Line2D.Double toLine(Segment segment) {
		return new Line2D.Double(segment.getFrom().getX(), segment.getFrom().getY(), segment.getTo().getX(), segment.getTo().getY());
	}

	private static Rectangle2D.Double toRectangle(NodeProperties properties) {
		return new Rectangle2D.Double(properties.getX(), properties.getY(), properties.getWidth(), properties.getHeight());
	}

	private static Rectangle2D.Double toRectangle(Bound bound) {
		return new Rectangle2D.Double(bound.getX(), bound.getY(), bound.getWidth(), bound.getHeight());
	}

	private static BufferedImage getLogo() {
		final String filename = "images/reactome_logo_100pxW_50T.png";
		final InputStream resource = Resources.class.getResourceAsStream(filename);
		try {
			return ImageIO.read(resource);
		} catch (IOException e) {
			LoggerFactory.getLogger("diagram-exporter").error("Logo not found in resources");
		}
		return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	}

}
