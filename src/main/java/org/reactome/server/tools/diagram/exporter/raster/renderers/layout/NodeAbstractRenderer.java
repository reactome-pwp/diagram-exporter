package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

public abstract class NodeAbstractRenderer extends AbstractRenderer {


	@Override
	public void drawText(AdvancedGraphics2D graphics, DiagramObject item) {
		if (item.getDisplayName() == null || item.getDisplayName().isEmpty())
			return;
		final Node node = (Node) item;
		graphics.drawText(node);
	}

	@Override
	public void draw(AdvancedGraphics2D graphics, DiagramObject item) {
		final Node node = (Node) item;
		graphics.drawRectangle(node.getProp());
	}

	@Override
	public void fill(AdvancedGraphics2D graphics, DiagramObject item) {
		final Node node = (Node) item;
		graphics.fillRectangle(node.getProp());
	}
}
