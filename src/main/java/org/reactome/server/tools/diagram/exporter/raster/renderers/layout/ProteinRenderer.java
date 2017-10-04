package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ProteinRenderer extends NodeAbstractRenderer {
	@Override
	protected Shape shape(AdvancedGraphics2D graphics, Node node) {
		final ScaledNodeProperties prop = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		return new RoundRectangle2D.Double(prop.getX(),
				prop.getY(), prop.getWidth(), prop.getHeight(),
				RendererProperties.ROUND_RECT_ARC_WIDTH,
				RendererProperties.ROUND_RECT_ARC_WIDTH);
	}
}
