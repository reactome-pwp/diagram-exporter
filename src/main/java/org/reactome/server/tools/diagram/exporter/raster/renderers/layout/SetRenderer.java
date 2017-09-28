package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;

public class SetRenderer extends NodeAbstractRenderer {

	@Override
	public void draw(AdvancedGraphics2D graphics, DiagramObject item) {
		final Node node = (Node) item;
		graphics.drawRoundedRectangle(node.getProp(),
				RendererProperties.ROUND_RECT_ARC_WIDTH,
				RendererProperties.ROUND_RECT_ARC_WIDTH);

		graphics.drawRoundedRectangle(
				node.getProp().getX() + RendererProperties.SEPARATION,
				node.getProp().getY() + RendererProperties.SEPARATION,
				node.getProp().getWidth() - 2 * RendererProperties.SEPARATION,
				node.getProp().getHeight() - 2 * RendererProperties.SEPARATION,
				RendererProperties.ROUND_RECT_ARC_WIDTH,
				RendererProperties.ROUND_RECT_ARC_WIDTH);
	}

	@Override
	public void fill(AdvancedGraphics2D graphics, DiagramObject item) {
		final Node node = (Node) item;
		graphics.fillRoundedRectangle(node.getProp(),
				RendererProperties.ROUND_RECT_ARC_WIDTH,
				RendererProperties.ROUND_RECT_ARC_WIDTH);

		graphics.fillRoundedRectangle(
				node.getProp().getX() + RendererProperties.SEPARATION,
				node.getProp().getY() + RendererProperties.SEPARATION,
				node.getProp().getWidth() - 2 * RendererProperties.SEPARATION,
				node.getProp().getHeight() - 2 * RendererProperties.SEPARATION,
				RendererProperties.ROUND_RECT_ARC_WIDTH,
				RendererProperties.ROUND_RECT_ARC_WIDTH);

	}
}
