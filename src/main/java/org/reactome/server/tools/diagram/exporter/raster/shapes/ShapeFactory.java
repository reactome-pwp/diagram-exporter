package org.reactome.server.tools.diagram.exporter.raster.shapes;

import org.reactome.server.tools.diagram.exporter.raster.renderers.common.IntNodeProperties;

import java.awt.*;

public class ShapeFactory {

	/**
	 * Creates an octagon simulating a rectangle with edged corners.
	 *
	 * @param prop
	 * @param cornerWidth
	 * @param cornerHeight
	 * @return
	 */
	public static Polygon cornedRectangle(IntNodeProperties prop, int cornerWidth, int cornerHeight) {
		final int x = prop.intX();
		final int y = prop.intY();
		final int width = prop.intWidth();
		final int height = prop.intHeight();
		return cornedRectangle(x, y, width, height, cornerWidth, cornerHeight);
	}

	public static Polygon cornedRectangle(int x, int y, int width, int height,
	                                      int cornerWidth, int cornerHeight) {
		final int[] xs = new int[]{
				x + cornerWidth,
				x + width - cornerWidth,
				x + width,
				x + width,
				x + width - cornerWidth,
				x + cornerWidth,
				x,
				x,
				x + cornerWidth
		};
		final int[] ys = new int[]{
				y,
				y,
				y + cornerHeight,
				y + height - cornerHeight,
				y + height,
				y + height,
				y + height - cornerHeight,
				y + cornerHeight,
				y
		};

		return new Polygon(xs, ys, xs.length);
	}
}
