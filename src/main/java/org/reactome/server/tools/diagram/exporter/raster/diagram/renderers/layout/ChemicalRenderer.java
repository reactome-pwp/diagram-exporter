package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Renderer of Chemicals, usually seen as ovals.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ChemicalRenderer extends NodeAbstractRenderer {

	@Override
	public Shape backgroundShape(NodeCommon node) {
		final NodeProperties properties = node.getProp();
		return new Ellipse2D.Double(properties.getX(), properties.getY(),
				properties.getWidth(), properties.getHeight());
	}
}
