package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ProcessNodeRenderer extends NodeAbstractRenderer {

	private static final Paint INNER_COLOR = new Color(254, 253, 255);

	@Override
	public void fill(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		final Collection<Node> nodes = (Collection<Node>) items;
		final List<Shape> shapes = nodes.stream()
				.map(node -> shape(graphics, node))
				.collect(Collectors.toList());
		final List<Shape> inner = nodes.stream()
				.map(node -> inner(graphics, node))
				.collect(Collectors.toList());
		shapes.forEach(shape -> graphics.getGraphics().fill(shape));
		graphics.getGraphics().setPaint(INNER_COLOR);
		inner.forEach(shape -> graphics.getGraphics().fill(shape));
	}

	@Override
	public void border(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		final Collection<Node> nodes = (Collection<Node>) items;
		nodes.stream()
				.map(node -> shape(graphics, node))
				.forEach(shape -> graphics.getGraphics().draw(shape));
		nodes.stream()
				.map(node -> inner(graphics, node))
				.forEach(shape -> graphics.getGraphics().draw(shape));
	}

	@Override
	public void text(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		final Collection<Node> nodes = (Collection<Node>) items;
		nodes.forEach(node -> TextRenderer.drawText(graphics, node, RendererProperties.PROCESS_NODE_INSET_WIDTH));
	}

	private Shape inner(AdvancedGraphics2D graphics, Node node) {
		final NodeProperties properties = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		return new Rectangle2D.Double(properties.getX() + RendererProperties.PROCESS_NODE_INSET_WIDTH,
				properties.getY() + RendererProperties.PROCESS_NODE_INSET_WIDTH,
				properties.getWidth() - 2 * RendererProperties.PROCESS_NODE_INSET_WIDTH,
				properties.getHeight() - 2 * RendererProperties.PROCESS_NODE_INSET_WIDTH);
	}
}
