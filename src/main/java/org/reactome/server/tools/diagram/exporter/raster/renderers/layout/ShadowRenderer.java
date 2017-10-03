package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Shadow;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

public class ShadowRenderer extends NodeAbstractRenderer {

	@Override
	public void drawText(AdvancedGraphics2D graphics, DiagramObject item) {
		final Shadow shadow = (Shadow) item;
		final double x = shadow.getPoints().get(0).getX();
		final double y = shadow.getPoints().get(0).getY();
		final double w = shadow.getPoints().get(2).getX() - x;
		final double h = shadow.getPoints().get(2).getY() - y;

		graphics.drawText(shadow.getDisplayName(), x, y, w, h);
	}

	@Override
	public void drawBorder(AdvancedGraphics2D graphics, DiagramObject item) {
		final Shadow shadow = (Shadow) item;
		final double x = shadow.getPoints().get(0).getX();
		final double y = shadow.getPoints().get(0).getY();
		final double w = shadow.getPoints().get(2).getX() - x;
		final double h = shadow.getPoints().get(2).getY() - y;
		graphics.drawRectangle(x, y, w, h);
	}

	@Override
	public void fill(AdvancedGraphics2D graphics, DiagramObject item) {
		final Shadow shadow = (Shadow) item;
		final double x = shadow.getPoints().get(0).getX();
		final double y = shadow.getPoints().get(0).getY();
		final double w = shadow.getPoints().get(2).getX() - x;
		final double h = shadow.getPoints().get(2).getY() - y;
		graphics.fillRect(x, y, w, h);
	}
}
