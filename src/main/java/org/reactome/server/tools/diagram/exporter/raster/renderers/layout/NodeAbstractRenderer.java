package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;

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

	/**
	 * This method is called from <code>draw()</code> if fillColor is not null.
	 * It calls <code>shape</code> for each node and fills those shapes with
	 * fillColor. You only have to override it if filling has a different
	 * behaviour.
	 *
	 * @param graphics where to render
	 * @param items    list of nodes to fill
	 */
	@Override
	public void fill(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		items.stream()
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
	 * @param graphics where to render
	 * @param items    list of nodes to draw
	 */
	@Override
	public void border(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		items.stream()
				.map(node -> shape(graphics, node))
				.forEach(graphics.getGraphics()::draw);
	}

	/**
	 * This method is called from <code>draw()</code> if textColor is not null.
	 * It sets the color to textColor and calls <code>graphics.drawText(node)</code>
	 * for each node. You only have to override it if drawing borders has a
	 * different behaviour.
	 *
	 * @param graphics where to render
	 * @param items    list of nodes to draw
	 */

	@Override
	public void text(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		final Collection<Node> nodes = (Collection<Node>) items;
		nodes.forEach(node -> TextRenderer.drawText(graphics, node));
	}

	@Override
	public void cross(AdvancedGraphics2D graphics, Collection<Node> nodes) {
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
	 *
	 * @return a Shape in the graphics scale
	 */
	protected Shape shape(AdvancedGraphics2D graphics, DiagramObject item) {
		final Node node = (Node) item;
		final NodeProperties properties = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		return new Rectangle2D.Double(properties.getX(), properties.getY(),
				properties.getWidth(), properties.getHeight());
	}
}
