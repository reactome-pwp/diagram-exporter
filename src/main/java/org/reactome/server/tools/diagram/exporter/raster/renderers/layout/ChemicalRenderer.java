package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

public class ChemicalRenderer extends NodeAbstractRenderer {

	@Override
	public void drawBorder(AdvancedGraphics2D graphics, DiagramObject item) {
		final Node node = (Node) item;
		graphics.drawOval(node.getProp());
		if (node.getIsCrossed() != null)
			graphics.drawCross(node.getProp());
	}

	@Override
	public void fill(AdvancedGraphics2D graphics, DiagramObject item) {
		final Node node = (Node) item;
		graphics.fillOval(node.getProp());
	}
}
