package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.awt.*;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class RnaRenderer extends NodeAbstractRenderer {

	@Override
	protected Shape backgroundShape(double factor, DiagramObject item) {
		final Node node = (Node) item;
		final NodeProperties properties = new ScaledNodeProperties(node.getProp(), factor);
		final double loopWidth = RendererProperties.RNA_LOOP_WIDTH;
		final double x = properties.getX();
		final double y = properties.getY();
		final double width = properties.getWidth();
		final double height = properties.getHeight();
		return ShapeFactory.getBoneShape(x, y, width, height, loopWidth);
	}

}
