package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.awt.*;

/**
 * Proteins use a rounded rectangle.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ProteinRenderer extends NodeAbstractRenderer {
	@Override
	protected Shape shape(AdvancedGraphics2D graphics, Node node) {
		final ScaledNodeProperties prop = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		return ShapeFactory.roundedRectangle(prop.getX(),
				prop.getY(), prop.getWidth(), prop.getHeight());
	}
}
