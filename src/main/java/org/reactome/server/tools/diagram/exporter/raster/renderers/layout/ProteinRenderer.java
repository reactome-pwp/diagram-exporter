package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.awt.*;

/**
 * Proteins use a rounded rectangle.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ProteinRenderer extends NodeAbstractRenderer {

	@Override
	protected Shape backgroundShape(NodeCommon node) {
		return ShapeFactory.roundedRectangle(node.getProp());
	}
}
