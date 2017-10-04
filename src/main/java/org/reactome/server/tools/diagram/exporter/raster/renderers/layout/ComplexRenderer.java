package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;

import java.awt.*;

public class ComplexRenderer extends NodeAbstractRenderer {

	@Override
	protected Shape shape(AdvancedGraphics2D graphics, Node node) {
		final NodeProperties prop = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		final double x = prop.getX();
		final double y = prop.getY();
		final double width = prop.getWidth();
		final double height = prop.getHeight();
		final int corner = (int) RendererProperties.COMPLEX_RECT_ARC_WIDTH;

		final int[] xs = new int[]{
				(int) (x + corner),
				(int) (x + width - corner),
				(int) (x + width),
				(int) (x + width),
				(int) (x + width - corner),
				(int) (x + corner),
				(int) x,
				(int) x,
				(int) (x + corner)
		};
		final int[] ys = new int[]{
				(int) y,
				(int) y,
				(int) (y + corner),
				(int) (y + height - corner),
				(int) (y + height),
				(int) (y + height),
				(int) (y + height - corner),
				(int) (y + corner),
				(int) y
		};
		return new Polygon(xs, ys, xs.length);
	}
}
