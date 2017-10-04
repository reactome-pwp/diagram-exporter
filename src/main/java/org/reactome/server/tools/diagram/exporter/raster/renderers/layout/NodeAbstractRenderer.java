package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

public abstract class NodeAbstractRenderer extends AbstractRenderer {

	@Override
	public void draw(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items, Paint fillColor, Paint lineColor, Paint textColor, Stroke segmentStroke, Stroke borderStroke) {
		final Collection<Node> nodes = (Collection<Node>) items;
		if (fillColor != null) fill(graphics, fillColor, nodes);
		if (lineColor != null) border(graphics, lineColor, borderStroke, nodes);
		if (textColor != null) text(graphics, textColor, nodes);
//
// final List<Shape> shapes = nodes.stream()
//				.map(node -> shape(graphics, node))
//				.collect(Collectors.toList());
//		if (fillColor != null) fill(graphics, fillColor, shapes);
//		if (lineColor != null) border(graphics, lineColor, nodes, shapes);
//		if (textColor != null) text(graphics, textColor, nodes);
	}

	protected void fill(AdvancedGraphics2D graphics, Paint fillColor, Collection<Node> shapes) {
		graphics.getGraphics().setPaint(fillColor);
		shapes.stream()
				.map(node -> shape(graphics, node))
				.forEach(shape -> graphics.getGraphics().fill(shape));
	}

	protected void border(AdvancedGraphics2D graphics, Paint lineColor, Stroke stroke, Collection<Node> nodes) {
		graphics.getGraphics().setPaint(lineColor);
		graphics.getGraphics().setStroke(stroke);
		nodes.stream()
				.map(node -> shape(graphics, node))
				.forEach(shape -> graphics.getGraphics().draw(shape));
		nodes.stream()
				.filter(node -> node.getIsCrossed() != null)
				.forEach(node -> graphics.drawCross(node.getProp()));
	}

	protected void text(AdvancedGraphics2D graphics, Paint textColor, Collection<Node> nodes) {
		graphics.getGraphics().setPaint(textColor);
		nodes.forEach(graphics::drawText);
	}

	protected Shape shape(AdvancedGraphics2D graphics, Node node) {
		final NodeProperties properties = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		return new Rectangle2D.Double(properties.getX(), properties.getY(),
				properties.getWidth(), properties.getHeight());
	}
}
