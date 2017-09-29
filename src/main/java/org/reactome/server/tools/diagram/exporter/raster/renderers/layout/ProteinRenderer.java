package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;

public class ProteinRenderer extends NodeAbstractRenderer {
	@Override
	public void drawBorder(AdvancedGraphics2D graphics, DiagramObject item) {
		final Node node = (Node) item;
		if (node.getNeedDashedBorder() != null && node.getNeedDashedBorder()) {
			graphics.drawRoundedRectangle(node.getProp(),
					RendererProperties.ROUND_RECT_ARC_WIDTH,
					RendererProperties.ROUND_RECT_ARC_WIDTH);
		} else {
			// Call it by dashing
			// Stroke should be changed outside
			graphics.drawRoundedRectangle(node.getProp(),
					RendererProperties.ROUND_RECT_ARC_WIDTH,
					RendererProperties.ROUND_RECT_ARC_WIDTH);
		}
		if (node.getIsCrossed() != null)
			graphics.drawCross(node.getProp());
	}

	@Override
	public void fill(AdvancedGraphics2D graphics, DiagramObject item) {
		final Node node = (Node) item;
		graphics.fillRoundedRectangle(node.getProp(),
				RendererProperties.ROUND_RECT_ARC_WIDTH,
				RendererProperties.ROUND_RECT_ARC_WIDTH);
	}
}
