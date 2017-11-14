package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.awt.*;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class RnaRenderer extends NodeAbstractRenderer {

	@Override
	public Shape backgroundShape(NodeCommon node) {
		final NodeProperties properties = node.getProp();
		final double x = properties.getX();
		final double y = properties.getY();
		final double width = properties.getWidth();
		final double height = properties.getHeight();
		return ShapeFactory.getRnaShape(x, y, width, height);
	}

}
