package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.util.Collection;

public class GeneRenderer extends NodeAbstractRenderer {

	@Override
	protected void fill(AdvancedGraphics2D graphics, Paint fillColor, Collection<Node> nodes) {
		graphics.getGraphics().setPaint(fillColor);
		nodes.stream()
				.map(node -> shape(graphics, node))
				.forEach(shape -> graphics.getGraphics().fill(shape));
	}

	@Override
	protected void border(AdvancedGraphics2D graphics, Paint lineColor, Stroke stroke, Collection<Node> nodes) {
		graphics.getGraphics().setPaint(lineColor);
		nodes.stream()
				.map(node -> line(graphics, node))
				.forEach(shape -> graphics.getGraphics().draw(shape));
		nodes.stream()
				.map(node -> arrow(graphics, node))
				.forEach(shape -> graphics.getGraphics().fill(shape));
	}

	@Override
	protected void text(AdvancedGraphics2D graphics, Paint textColor, Collection<Node> nodes) {
		graphics.getGraphics().setPaint(textColor);
		nodes.forEach(node -> {
			final double x = node.getProp().getX();
			final double width = node.getProp().getWidth();
			final double yOffset = 0.5 * RendererProperties.GENE_SYMBOL_WIDTH / graphics.getFactor();
			final double y = node.getProp().getY() + yOffset;
			final double height = node.getProp().getHeight() - yOffset;
			graphics.drawText(node.getDisplayName(), x, y, width, height);
		});
	}

	@Override
	protected Shape shape(AdvancedGraphics2D graphics, Node node) {
		final NodeProperties prop = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		final GeneralPath path = new GeneralPath();
		final double y1 = prop.getY() + 0.5 * RendererProperties.GENE_SYMBOL_WIDTH;
		final double right = prop.getX() + prop.getWidth();
		final double bottom = prop.getY() + prop.getHeight();
		final double arcWidth = RendererProperties.ROUND_RECT_ARC_WIDTH;

		path.moveTo(prop.getX(), y1);
		path.lineTo(right, y1);
		path.lineTo(right, bottom - arcWidth);
		path.quadTo(right, bottom, right - arcWidth, bottom);
		path.lineTo(prop.getX() + arcWidth, bottom);
		path.quadTo(prop.getX(), bottom, prop.getX(), bottom - arcWidth);
		path.closePath();
		return path;
	}

	private Shape line(AdvancedGraphics2D graphics, Node node) {
		final NodeProperties prop = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		// Horizontal line
		final double x = prop.getX();
		final double y1 = prop.getY() + 0.5 * RendererProperties.GENE_SYMBOL_WIDTH;
		final double right = prop.getX() + prop.getWidth();
		final Path2D path = new GeneralPath();
		path.moveTo(x, y1);
		path.lineTo(right, y1);
		// Vertical line
		final double x1 = right - RendererProperties.GENE_SYMBOL_PAD;
		final double y2 = y1 - 0.5 * RendererProperties.GENE_SYMBOL_WIDTH;
		path.moveTo(x1, y1);
		path.lineTo(x1, y2);
		// another very short horizontal line
		path.lineTo(right, y2);
		return path;
	}

	private Shape arrow(AdvancedGraphics2D graphics, Node node) {
		final NodeProperties prop = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
//		drawArrow(x1, y2, x1 + arrowLength, y2, arrowLength, arrowAngle);
		// Get the angle of the line segment
		final double right = prop.getX() + prop.getWidth();
		final double toX = right + RendererProperties.ARROW_LENGTH;
		final double y1 = prop.getY() + 0.5 * RendererProperties.GENE_SYMBOL_WIDTH;
		final double y2 = y1 - 0.5 * RendererProperties.GENE_SYMBOL_WIDTH;
		final Path2D triangle = new GeneralPath();
		triangle.moveTo(toX, y2);
		final double ay = y2 + 0.5 * RendererProperties.ARROW_LENGTH;
		final double by = y2 - 0.5 * RendererProperties.ARROW_LENGTH;
		triangle.lineTo(right, ay);
		triangle.lineTo(right, by);
		triangle.closePath();
		return triangle;
	}
}
