package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.*;

import java.awt.*;
import java.util.Collection;

/**
 * Sets add an inner border.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class SetRenderer extends NodeAbstractRenderer {

	@Override
	protected void border(AdvancedGraphics2D graphics, Paint lineColor, Stroke stroke, Collection<? extends DiagramObject> items) {
		super.border(graphics, lineColor, stroke, items);
		final Collection<Node> nodes = (Collection<Node>) items;
		nodes.forEach(node -> {
			final Shape shape = innerShape(graphics, node);
			if (node.getNeedDashedBorder() != null && node.getNeedDashedBorder()) {
				graphics.getGraphics().setStroke(ColorProfile.DASHED_BORDER_STROKE);
				graphics.getGraphics().draw(shape);
				graphics.getGraphics().setStroke(ColorProfile.BORDER_STROKE);
			} else graphics.getGraphics().draw(shape);
		});
	}

	@Override
	protected Shape shape(AdvancedGraphics2D graphics, Node node) {
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
	protected void text(AdvancedGraphics2D graphics, Paint textColor, Collection<? extends DiagramObject> items) {
		graphics.getGraphics().setPaint(textColor);
		items.forEach(node -> TextRenderer.drawText(graphics, (NodeCommon) node, RendererProperties.SEPARATION));
	}
}
