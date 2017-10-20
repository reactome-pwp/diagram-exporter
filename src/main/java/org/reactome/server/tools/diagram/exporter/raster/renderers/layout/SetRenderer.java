package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.awt.*;

/**
 * Sets add an inner border.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class SetRenderer extends NodeAbstractRenderer {


	@Override
	protected Shape backgroundShape(NodeCommon node) {
		return ShapeFactory.roundedRectangle(node.getProp());
	}

	@Override
	protected Shape foregroundShape(NodeCommon node) {
		return ShapeFactory.roundedRectangle(node.getProp(), RendererProperties.SET_PADDING);
	}

	@Override
	protected Color getFgFill(ColorProfiles colorProfiles, DiagramIndex index) {
		return new Color(0, 0, 0, 0);
	}
}
