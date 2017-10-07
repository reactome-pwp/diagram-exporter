package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ColorProfile;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.StrokeProperties;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

/**
 * Basic node renderer. All Renderers that render nodes should override it. The
 * default behaviour consists on 3 steps: filling, drawing borders and drawing
 * texts.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public abstract class NodeAbstractRenderer extends AbstractRenderer {

	@Override
	public void draw(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items, Paint fillColor, Paint lineColor, Paint textColor, Stroke borderStroke) {
		if (fillColor != null) fill(graphics, fillColor, items);
		if (lineColor != null) border(graphics, lineColor, borderStroke, items);
		if (textColor != null) text(graphics, textColor, items);
	}

	/**
	 * This method is called from <code>draw()</code> if fillColor is not null.
	 * It calls <code>shape</code> for each node and fills those shapes with
	 * fillColor. You only have to override it if filling has a different
	 * behaviour.
	 *
	 * @param graphics  where to render
	 * @param fillColor color for all fillings
	 * @param items     list of nodes to fill
	 */
	protected void fill(AdvancedGraphics2D graphics, Paint fillColor, Collection<? extends DiagramObject> items) {
		final Collection<Node> nodes = (Collection<Node>) items;
		graphics.getGraphics().setPaint(fillColor);
		nodes.stream()
				.map(node -> shape(graphics, node))
				.forEach(shape -> graphics.getGraphics().fill(shape));
	}

	/**
	 * This method is called from <code>draw()</code> if lineColor is not null.
	 * It calls <code>shape</code> for each node and draws those shapes with
	 * lineColor. After that, if any of the nodes is crossed, draws the cross.
	 * You only have to override it if drawing borders has a different
	 * behaviour.
	 *
	 * @param graphics  where to render
	 * @param lineColor color for borders
	 * @param stroke    shape of border line
	 * @param items     list of nodes to draw
	 */
	protected void border(AdvancedGraphics2D graphics, Paint lineColor, Stroke stroke, Collection<? extends DiagramObject> items) {
		final Collection<Node> nodes = (Collection<Node>) items;
		graphics.getGraphics().setPaint(lineColor);
		graphics.getGraphics().setStroke(stroke);
		nodes.forEach(node -> {
			final Shape shape = shape(graphics, node);
			if (node.getNeedDashedBorder() != null) {
				graphics.getGraphics().setStroke(StrokeProperties.dash(stroke));
				graphics.getGraphics().draw(shape);
				graphics.getGraphics().setStroke(stroke);
			} else graphics.getGraphics().draw(shape);
		});
//		nodes.stream()
//				.filter(node -> node.getIsCrossed() != null)
//				.forEach(node -> graphics.drawCross(node.getProp()));
	}

	/**
	 * This method is called from <code>draw()</code> if textColor is not null.
	 * It sets the color to textColor and calls <code>graphics.drawText(node)</code>
	 * for each node. You only have to override it if drawing borders has a
	 * different behaviour.
	 *
	 * @param graphics  where to render
	 * @param textColor color for texts
	 * @param items     list of nodes to draw
	 */

	protected void text(AdvancedGraphics2D graphics, Paint textColor, Collection<? extends DiagramObject> items) {
		final Collection<Node> nodes = (Collection<Node>) items;
		graphics.getGraphics().setPaint(textColor);
		nodes.forEach(node -> TextRenderer.drawText(graphics, node));
	}

	@Override
	public void cross(AdvancedGraphics2D graphics, Collection<Node> nodes, Paint crossColor, Stroke stroke) {
		graphics.getGraphics().setPaint(crossColor);
		graphics.getGraphics().setStroke(stroke);
		nodes.stream()
				.filter(node -> node.getIsCrossed() != null)
				.filter(Node::getIsCrossed)
				.map(Node::getProp)
				.forEach(graphics::drawCross);
	}

	/**
	 * Returns the proper java shape for a Node. By default creates a rectangle.
	 * Override it when you have a different shape.
	 *
	 * @param graphics to take the factor
	 * @param node     node to extract shape
	 *
	 * @return a Shape in the graphics scale
	 */
	protected Shape shape(AdvancedGraphics2D graphics, Node node) {
		final NodeProperties properties = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		return new Rectangle2D.Double(properties.getX(), properties.getY(),
				properties.getWidth(), properties.getHeight());
	}
}
