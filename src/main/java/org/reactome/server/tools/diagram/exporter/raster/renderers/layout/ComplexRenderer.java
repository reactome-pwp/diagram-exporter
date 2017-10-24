package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.awt.*;

/**
 * Renderer for complexes, which are corned rectangles.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ComplexRenderer extends NodeAbstractRenderer {

	@Override
	public Shape backgroundShape(NodeCommon node) {
		final NodeProperties prop = node.getProp();
		final double x = prop.getX();
		final double y = prop.getY();
		final double width = prop.getWidth();
		final double height = prop.getHeight();
		return ShapeFactory.getCornedRectangle(x, y, width, height);
	}

}
