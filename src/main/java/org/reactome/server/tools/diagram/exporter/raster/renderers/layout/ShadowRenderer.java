package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Shadow;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.util.Collection;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ShadowRenderer extends NodeAbstractRenderer {

	private void drawText(AdvancedGraphics2D graphics, Shadow item) {
		final double x = item.getPoints().get(0).getX();
		final double y = item.getPoints().get(0).getY();
		final double w = item.getPoints().get(2).getX() - x;
		final double h = item.getPoints().get(2).getY() - y;

		TextRenderer.drawText(graphics, item.getDisplayName(), x, y, w, h);
	}


	@Override
	public void draw(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items, Paint fillColor, Paint lineColor, Paint textColor, Stroke borderStroke) {
		final Collection<Shadow> shadows = (Collection<Shadow>) items;
		graphics.getGraphics().setPaint(fillColor);
		shadows.forEach(shadow -> graphics.getGraphics().fill(shape(graphics, shadow)));

		graphics.getGraphics().setPaint(lineColor);
		graphics.getGraphics().setStroke(borderStroke);
		shadows.forEach(shadow -> graphics.getGraphics().draw(shape(graphics, shadow)));

		graphics.getGraphics().setPaint(textColor);
		shadows.forEach(node -> drawText(graphics, node));

	}

	private Shape shape(AdvancedGraphics2D graphics, Shadow shadow) {
		final Path2D path = new GeneralPath();
		path.moveTo(
				shadow.getPoints().get(0).getX() * graphics.getFactor(),
				shadow.getPoints().get(0).getY() * graphics.getFactor());
		for (int i = 1; i < shadow.getPoints().size(); i++) {
			path.lineTo(
					shadow.getPoints().get(i).getX() * graphics.getFactor(),
					shadow.getPoints().get(i).getY() * graphics.getFactor());
		}
		path.closePath();
		return path;
	}
}
