package org.reactome.server.tools.diagram.exporter.raster.renderers.layers;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public abstract class CommonLayer implements Layer {

	private Double y;
	private Double x;
	private Double x1;
	private Double y1;

	@Override
	public Rectangle2D getBounds() {
		return x == null
				? null
				: new Rectangle2D.Double(x, y, x1 - x, y1 - y);
	}

	void addShape(Shape shape) {
		if (y == null) {
			x = shape.getBounds2D().getMinX();
			y = shape.getBounds2D().getMinY();
			x1 = shape.getBounds2D().getMaxX();
			y1 = shape.getBounds2D().getMaxY();
		} else {
			if (shape.getBounds2D().getMinX() < x)
				x = shape.getBounds2D().getMinX();
			if (shape.getBounds2D().getMinY() < y)
				y = shape.getBounds2D().getMinY();
			if (shape.getBounds2D().getMaxX() > x1)
				x1 = shape.getBounds2D().getMaxX();
			if (shape.getBounds2D().getMaxY() > y1)
				y1 = shape.getBounds2D().getMaxY();
		}
	}
}
