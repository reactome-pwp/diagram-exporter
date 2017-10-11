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

	@Override
	public void text(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		final Collection<Shadow> shadows = (Collection<Shadow>) items;
		shadows.forEach(shadow -> TextRenderer.drawText(graphics, shadow.getDisplayName(), shadow.getProp()));
	}

	@Override
	protected Shape shape(AdvancedGraphics2D graphics, DiagramObject node) {
		final Shadow shadow = (Shadow) node;
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
