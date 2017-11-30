package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public abstract class CommonLayer implements Layer {

	private Double minY;
	private Double minX;
	private Double maxX;
	private Double maxY;

	@Override
	public Rectangle2D getBounds() {
		return minX == null
				? null
				: new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
	}

	void addShape(Shape shape) {
		if (minY == null) {
			minX = shape.getBounds2D().getMinX();
			minY = shape.getBounds2D().getMinY();
			maxX = shape.getBounds2D().getMaxX();
			maxY = shape.getBounds2D().getMaxY();
		} else {
			if (shape.getBounds2D().getMinX() < minX)
				minX = shape.getBounds2D().getMinX();
			if (shape.getBounds2D().getMinY() < minY)
				minY = shape.getBounds2D().getMinY();
			if (shape.getBounds2D().getMaxX() > maxX)
				maxX = shape.getBounds2D().getMaxX();
			if (shape.getBounds2D().getMaxY() > maxY)
				maxY = shape.getBounds2D().getMaxY();
		}
	}

	@Override
	public void clear() {
		minX = minY = maxY = maxX = null;
	}
}
