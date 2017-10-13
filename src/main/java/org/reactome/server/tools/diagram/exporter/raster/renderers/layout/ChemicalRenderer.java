package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
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
	protected Shape backgroundShape(DiagramObject item) {
		final Node node = (Node) item;
		final NodeProperties properties = node.getProp();
		return new Ellipse2D.Double(properties.getX(), properties.getY(),
				properties.getWidth(), properties.getHeight());
	}
}
