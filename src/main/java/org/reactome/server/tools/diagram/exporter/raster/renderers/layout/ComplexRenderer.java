package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;

public class ComplexRenderer extends NodeAbstractRenderer {

	@Override
	public void fill(AdvancedGraphics2D graphics, DiagramObject item) {
		final Node node = (Node) item;
		graphics.fillCornedRectangle(node.getProp(),
				RendererProperties.COMPLEX_RECT_ARC_WIDTH,
				RendererProperties.COMPLEX_RECT_ARC_WIDTH);
	}

	@Override
	public void drawBorder(AdvancedGraphics2D graphics, DiagramObject item) {
		final Node node = (Node) item;
		graphics.drawCornedRectangle(node.getProp(),
				RendererProperties.COMPLEX_RECT_ARC_WIDTH,
				RendererProperties.COMPLEX_RECT_ARC_WIDTH);
		if (node.getIsCrossed() != null) {
			graphics.drawCross(node.getProp());
		}
	}

}
