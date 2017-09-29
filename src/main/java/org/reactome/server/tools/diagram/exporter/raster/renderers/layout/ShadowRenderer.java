package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Shadow;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

public class ShadowRenderer extends NodeAbstractRenderer {

	@Override
	public void drawText(AdvancedGraphics2D graphics, DiagramObject item) {
		final Shadow shadow = (Shadow) item;
		graphics.drawText(shadow.getDisplayName(), shadow.getProp().getX(),
				shadow.getProp().getY(),
				shadow.getProp().getWidth(),
				shadow.getProp().getHeight());
	}

	@Override
	public void drawBorder(AdvancedGraphics2D graphics, DiagramObject item) {
		final Shadow shadow = (Shadow) item;
		graphics.drawRectangle(shadow.getProp());
	}

	@Override
	public void fill(AdvancedGraphics2D graphics, DiagramObject item) {
		final Shadow shadow = (Shadow) item;
		graphics.fillRectangle(shadow.getProp());
	}
}
