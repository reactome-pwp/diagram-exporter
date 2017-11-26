package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;

/**
 * Sets add an inner border.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class SetRenderer extends NodeAbstractRenderer {


	@Override
	public Shape backgroundShape(NodeCommon node) {
		return ShapeFactory.roundedRectangle(node.getProp());
	}

	@Override
	public Shape foregroundShape(NodeCommon node) {
		return ShapeFactory.roundedRectangle(node.getProp(), RendererProperties.SET_PADDING);
	}

	@Override
	public Color getForegroundFill(ColorProfiles colors, DiagramIndex index) {
		return new Color(0, 0, 0, 0);  // Transparent
	}
}
