package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;

public class GeneRenderer extends AbstractRenderer {

	@Override
	public void fill(AdvancedGraphics2D graphics, DiagramObject item) {
		graphics.fillGene((Node) item);
	}

	@Override
	public void drawBorder(AdvancedGraphics2D graphics, DiagramObject item) {
		graphics.drawGene((Node) item);
	}

	@Override
	public void drawText(AdvancedGraphics2D graphics, DiagramObject item) {
		if (item.getDisplayName() == null || item.getDisplayName().isEmpty())
			return;
		final Node node = (Node) item;
		final double x = node.getProp().getX();
		final double width = node.getProp().getWidth();
		final double yOffset = 0.5 * RendererProperties.GENE_SYMBOL_WIDTH / graphics.getFactor();
		final double y = node.getProp().getY() + yOffset;
		final double height = node.getProp().getHeight() - yOffset;
		graphics.drawText(node.getDisplayName(), x, y, width, height);
	}
}
