package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.awt.*;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class RnaRenderer extends NodeAbstractRenderer {

	@Override
	protected Shape shape(AdvancedGraphics2D graphics, Node node) {
		final NodeProperties properties = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		final double loopWidth = RendererProperties.RNA_LOOP_WIDTH;
		final double x = properties.getX();
		final double y = properties.getY();
		final double width = properties.getWidth();
		final double height = properties.getHeight();
		return ShapeFactory.getBoneShape(x, y, width, height, loopWidth);
	}

}
