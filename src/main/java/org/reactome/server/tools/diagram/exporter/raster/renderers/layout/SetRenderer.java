package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.awt.*;
import java.util.Collection;

/**
 * Sets add an inner border.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class SetRenderer extends NodeAbstractRenderer {

	@Override
	public void border(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		super.border(graphics, items);
		final Collection<Node> nodes = (Collection<Node>) items;
		nodes.forEach(node -> {
			final Shape shape = innerShape(graphics, node);
			graphics.getGraphics().draw(shape);
		});
	}

	@Override
	protected Shape shape(AdvancedGraphics2D graphics, DiagramObject item) {
		final Node node = (Node) item;
		final NodeProperties prop = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		return ShapeFactory.roundedRectangle(
				prop.getX(), prop.getY(), prop.getWidth(), prop.getHeight());
	}

	private Shape innerShape(AdvancedGraphics2D graphics, Node node) {
		final NodeProperties prop = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		return ShapeFactory.roundedRectangle(
				prop.getX(),
				prop.getY(),
				prop.getWidth(),
				prop.getHeight(), RendererProperties.SEPARATION);
	}

	@Override
	public void text(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		final Collection<NodeCommon> nodes = (Collection<NodeCommon>) items;
		// Adds 1*factor padding to the inner rectangle
		final double padding = RendererProperties.SEPARATION + graphics.getFactor();
		nodes.forEach(node -> TextRenderer.drawText(graphics, node, padding));
	}
}
