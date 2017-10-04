package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessNodeRenderer extends NodeAbstractRenderer {

	private static final double PADDING = 10;

	private static final Paint INNER_COLOR = new Color(254, 253, 255);

	@Override
	public void draw(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items, Paint fillColor, Paint lineColor, Paint textColor, Stroke segmentStroke, Stroke borderStroke) {
		final Collection<Node> nodes = (Collection<Node>) items;
		final List<Shape> shapes = nodes.stream()
				.map(node -> shape(graphics, node))
				.collect(Collectors.toList());
		final List<Shape> inner = nodes.stream()
				.map(node -> inner(graphics, node))
				.collect(Collectors.toList());
		if (fillColor != null) fill(graphics, fillColor, shapes, inner);
		if (lineColor != null) border(graphics, lineColor, shapes, inner);
		if (textColor != null) text(graphics, textColor, nodes);
	}

	private void fill(AdvancedGraphics2D graphics, Paint fillColor, List<Shape> shapes, List<Shape> inner) {
		graphics.getGraphics().setPaint(fillColor);
		shapes.forEach(shape -> graphics.getGraphics().fill(shape));
		graphics.getGraphics().setPaint(INNER_COLOR);
		inner.forEach(shape -> graphics.getGraphics().fill(shape));
	}

	private void border(AdvancedGraphics2D graphics, Paint lineColor, List<Shape> shapes, List<Shape> inner) {
		graphics.getGraphics().setPaint(lineColor);
		shapes.forEach(shape -> graphics.getGraphics().draw(shape));
		inner.forEach(shape -> graphics.getGraphics().draw(shape));
	}

	@Override
	protected void text(AdvancedGraphics2D graphics, Paint textColor, Collection<Node> nodes) {
		graphics.getGraphics().setPaint(textColor);
		nodes.forEach(node -> graphics.drawText(node, PADDING));
	}

	private Shape inner(AdvancedGraphics2D graphics, Node node) {
		final NodeProperties properties = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		return new Rectangle2D.Double(properties.getX() + PADDING,
				properties.getY() + PADDING,
				properties.getWidth() - 2 * PADDING,
				properties.getHeight() - 2 * PADDING);
	}
}
