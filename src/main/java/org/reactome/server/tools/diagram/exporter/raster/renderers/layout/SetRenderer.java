package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SetRenderer extends NodeAbstractRenderer {

//	@Override
//	public void drawBorder(AdvancedGraphics2D graphics, DiagramObject item) {
//		final Node node = (Node) item;
//		graphics.drawRoundedRectangle(node.getProp(),
//				RendererProperties.ROUND_RECT_ARC_WIDTH,
//				RendererProperties.ROUND_RECT_ARC_WIDTH);
//
//		graphics.drawRoundedRectangle(
//				node.getProp().getX() + RendererProperties.SEPARATION,
//				node.getProp().getY() + RendererProperties.SEPARATION,
//				node.getProp().getWidth() - 2 * RendererProperties.SEPARATION,
//				node.getProp().getHeight() - 2 * RendererProperties.SEPARATION,
//				RendererProperties.ROUND_RECT_ARC_WIDTH,
//				RendererProperties.ROUND_RECT_ARC_WIDTH);
//	}
//
//	@Override
//	public void fill(AdvancedGraphics2D graphics, DiagramObject item) {
//		final Node node = (Node) item;
//		graphics.fillRoundedRectangle(node.getProp(),
//				RendererProperties.ROUND_RECT_ARC_WIDTH,
//				RendererProperties.ROUND_RECT_ARC_WIDTH);
//
//		graphics.fillRoundedRectangle(
//				node.getProp().getX() + RendererProperties.SEPARATION,
//				node.getProp().getY() + RendererProperties.SEPARATION,
//				node.getProp().getWidth() - 2 * RendererProperties.SEPARATION,
//				node.getProp().getHeight() - 2 * RendererProperties.SEPARATION,
//				RendererProperties.ROUND_RECT_ARC_WIDTH,
//				RendererProperties.ROUND_RECT_ARC_WIDTH);
//
//	}

	@Override
	public void draw(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items, Paint fillColor, Paint lineColor, Paint textColor, Stroke segmentStroke, Stroke borderStroke) {
		final Collection<Node> nodes = (Collection<Node>) items;
		final List<Shape> shapes = nodes.stream()
				.map(node -> shape(graphics, node))
				.collect(Collectors.toList());
		graphics.getGraphics().setPaint(fillColor);
		shapes.forEach(shape -> graphics.getGraphics().fill(shape));
		graphics.getGraphics().setPaint(lineColor);
		shapes.forEach(shape -> graphics.getGraphics().draw(shape));
		final List<Shape> inner = nodes.stream()
				.map(node -> innerShape(graphics, node))
				.collect(Collectors.toList());
		inner.forEach(shape -> graphics.getGraphics().draw(shape));
		graphics.getGraphics().setPaint(textColor);
		nodes.forEach(node -> graphics.drawText(node, RendererProperties.SEPARATION));

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

}
