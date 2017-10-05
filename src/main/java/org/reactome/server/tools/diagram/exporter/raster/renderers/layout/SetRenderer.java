package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sets add an inner border.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class SetRenderer extends NodeAbstractRenderer {

	@Override
	protected void border(AdvancedGraphics2D graphics, Paint lineColor, Stroke stroke, Collection<? extends DiagramObject> items) {
		super.border(graphics, lineColor, stroke, items);
		final List<Shape> inner = items.stream()
				.map(node -> innerShape(graphics, (Node) node))
				.collect(Collectors.toList());
		inner.forEach(shape -> graphics.getGraphics().draw(shape));
	}

	@Override
	protected Shape shape(AdvancedGraphics2D graphics, Node node) {
		final NodeProperties prop = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		return new RoundRectangle2D.Double(
				prop.getX(), prop.getY(), prop.getWidth(), prop.getHeight(),
				RendererProperties.ROUND_RECT_ARC_WIDTH,
				RendererProperties.ROUND_RECT_ARC_WIDTH);
	}

	private Shape innerShape(AdvancedGraphics2D graphics, Node node) {
		final NodeProperties prop = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		return new RoundRectangle2D.Double(
				prop.getX() + RendererProperties.SEPARATION,
				prop.getY() + RendererProperties.SEPARATION,
				prop.getWidth() - 2 * RendererProperties.SEPARATION,
				prop.getHeight() - 2 * RendererProperties.SEPARATION,
				RendererProperties.ROUND_RECT_ARC_WIDTH,
				RendererProperties.ROUND_RECT_ARC_WIDTH
		);
	}

	@Override
	protected void text(AdvancedGraphics2D graphics, Paint textColor, Collection<? extends DiagramObject> items) {
		graphics.getGraphics().setPaint(textColor);
		items.forEach(node -> graphics.drawText((NodeCommon) node, RendererProperties.SEPARATION));
	}
}
