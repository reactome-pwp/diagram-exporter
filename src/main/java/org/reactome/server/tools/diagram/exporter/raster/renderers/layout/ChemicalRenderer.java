package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Renderer of Chemicals, usually seen as ovals.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ChemicalRenderer extends NodeAbstractRenderer {

	protected Shape shape(AdvancedGraphics2D graphics, Node node) {
		final NodeProperties properties = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		return new Ellipse2D.Double(properties.getX(), properties.getY(), properties.getWidth(), properties.getHeight());
	}
}
