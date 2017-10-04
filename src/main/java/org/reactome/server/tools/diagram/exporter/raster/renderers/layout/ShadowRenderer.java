package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Shadow;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

import java.awt.*;
import java.util.Collection;

public class ShadowRenderer extends NodeAbstractRenderer {

	private void drawText(AdvancedGraphics2D graphics, DiagramObject item) {
		final Shadow shadow = (Shadow) item;
		final double x = shadow.getPoints().get(0).getX();
		final double y = shadow.getPoints().get(0).getY();
		final double w = shadow.getPoints().get(2).getX() - x;
		final double h = shadow.getPoints().get(2).getY() - y;

		graphics.drawText(shadow.getDisplayName(), x, y, w, h);
	}

	private void drawBorder(AdvancedGraphics2D graphics, DiagramObject item) {
		final Shadow shadow = (Shadow) item;
		final double x = shadow.getPoints().get(0).getX();
		final double y = shadow.getPoints().get(0).getY();
		final double w = shadow.getPoints().get(2).getX() - x;
		final double h = shadow.getPoints().get(2).getY() - y;
		graphics.drawRectangle(x, y, w, h);
	}

	private void fill(AdvancedGraphics2D graphics, DiagramObject item) {
		final Shadow shadow = (Shadow) item;
		final double x = shadow.getPoints().get(0).getX();
		final double y = shadow.getPoints().get(0).getY();
		final double w = shadow.getPoints().get(2).getX() - x;
		final double h = shadow.getPoints().get(2).getY() - y;
		graphics.fillRect(x, y, w, h);
	}

	@Override
	public void draw(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items, Paint fillColor, Paint lineColor, Paint textColor, Stroke segmentStroke, Stroke borderStroke) {
		final Collection<Shadow> shadows = (Collection<Shadow>) items;
		graphics.getGraphics().setPaint(fillColor);
		shadows.forEach(node -> fill(graphics, node));

		graphics.getGraphics().setPaint(lineColor);
		shadows.forEach(node -> drawBorder(graphics, node));

		graphics.getGraphics().setPaint(textColor);
		shadows.forEach(node -> drawText(graphics, node));

	}
}
