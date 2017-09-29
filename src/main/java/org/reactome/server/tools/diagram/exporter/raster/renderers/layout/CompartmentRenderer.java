package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Compartment;
import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;

public class CompartmentRenderer extends AbstractRenderer {


	@Override
	public void fill(AdvancedGraphics2D graphics, DiagramObject item) {
		final Compartment compartment = (Compartment) item;
		graphics.fillRoundedRectangle(compartment.getProp(),
				RendererProperties.ROUND_RECT_ARC_WIDTH,
				RendererProperties.ROUND_RECT_ARC_WIDTH);
		if (compartment.getInsets() != null) {
			graphics.fillRoundedRectangle(compartment.getInsets(),
					RendererProperties.ROUND_RECT_ARC_WIDTH,
					RendererProperties.ROUND_RECT_ARC_WIDTH);
		}
	}

	@Override
	public void drawBorder(AdvancedGraphics2D graphics, DiagramObject item) {
		final Compartment compartment = (Compartment) item;
		graphics.drawRoundedRectangle(compartment.getProp(),
				RendererProperties.ROUND_RECT_ARC_WIDTH,
				RendererProperties.ROUND_RECT_ARC_WIDTH);
		if (compartment.getInsets() != null)
			graphics.drawRoundedRectangle(compartment.getInsets(),
					RendererProperties.ROUND_RECT_ARC_WIDTH,
					RendererProperties.ROUND_RECT_ARC_WIDTH);
	}


	@Override
	public void drawText(AdvancedGraphics2D graphics, DiagramObject item) {
		if (item.getDisplayName() == null || item.getDisplayName().isEmpty())
			return;
		final Compartment compartment = (Compartment) item;
		graphics.drawTextSingleLine(item.getDisplayName(), compartment.getTextPosition());
	}

}
