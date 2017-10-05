package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;

/**
 * Convenient place to find no so common shapes.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ShapeFactory {

	/**
	 * Creates a rectangle with edged corners (an octagon)
	 *
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param corner
	 *
	 * @return
	 */
	public static Shape getCornedRectangle(double x, double y, double width, double height, int corner) {
		final int[] xs = new int[]{
				(int) (x + corner),
				(int) (x + width - corner),
				(int) (x + width),
				(int) (x + width),
				(int) (x + width - corner),
				(int) (x + corner),
				(int) x,
				(int) x,
				(int) (x + corner)
		};
		final int[] ys = new int[]{
				(int) y,
				(int) y,
				(int) (y + corner),
				(int) (y + height - corner),
				(int) (y + height),
				(int) (y + height),
				(int) (y + height - corner),
				(int) (y + corner),
				(int) y
		};
		return new Polygon(xs, ys, xs.length);
	}

	/**
	 * Creates the shape of the gene fill, a bottom rounded rectangle.
	 *
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 *
	 * @return
	 */
	public static Shape getGeneFillShape(double x, double y, double width, double height) {
		final GeneralPath path = new GeneralPath();
		final double y1 = y + 0.5 * RendererProperties.GENE_SYMBOL_WIDTH;
		final double bottom = y + height;
		final double arcWidth = RendererProperties.ROUND_RECT_ARC_WIDTH;
		final double right = x + width;
		path.moveTo(x, y1);
		path.lineTo(right, y1);
		path.lineTo(right, bottom - arcWidth);
		path.quadTo(right, bottom, right - arcWidth, bottom);
		path.lineTo(x + arcWidth, bottom);
		path.quadTo(x, bottom, x, bottom - arcWidth);
		path.closePath();
		return path;
	}

	/**
	 * Returns a path with two perpendicular lines.
	 *
	 * @param x
	 * @param y
	 * @param width
	 *
	 * @return
	 */
	public static Shape getGeneLine(double x, double y, double width) {
		// Horizontal line
		final double y1 = y + 0.5 * RendererProperties.GENE_SYMBOL_WIDTH;
		final double right = x + width;
		final Path2D path = new GeneralPath();
		path.moveTo(x, y1);
		path.lineTo(right, y1);
		// Vertical line
		final double x1 = right - RendererProperties.GENE_SYMBOL_PAD;
		final double y2 = y1 - 0.5 * RendererProperties.GENE_SYMBOL_WIDTH;
		path.moveTo(x1, y1);
		path.lineTo(x1, y2);
		// another very short horizontal line
		path.lineTo(right, y2);
		return path;
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 *
	 * @return
	 */
	public static Shape getGeneArrow(double x, double y, double width) {
		final double right = x + width;
		final double toX = right + RendererProperties.ARROW_LENGTH;
		final double y1 = y + 0.5 * RendererProperties.GENE_SYMBOL_WIDTH;
		final double y2 = y1 - 0.5 * RendererProperties.GENE_SYMBOL_WIDTH;
		final Path2D triangle = new GeneralPath();
		triangle.moveTo(toX, y2);
		final double ay = y2 + 0.5 * RendererProperties.ARROW_LENGTH;
		final double by = y2 - 0.5 * RendererProperties.ARROW_LENGTH;
		triangle.lineTo(right, ay);
		triangle.lineTo(right, by);
		triangle.closePath();
		return triangle;
	}

	public static Shape getBoneShape(double x, double y, double width, double height, double loopWidth) {
		double right = x + width;
		double bottom = y + height;
		final Path2D path = new GeneralPath();

		double xAux = x + loopWidth;
		double yAux = y + loopWidth / 2;
		path.moveTo(xAux, yAux);
		xAux = right - loopWidth;
		path.lineTo(xAux, yAux);
		yAux = y + height / 2;
		path.quadTo(right, y, right, yAux);

		xAux = right - loopWidth;
		yAux = bottom - loopWidth / 2;
		path.quadTo(right, bottom, xAux, yAux);

		xAux = x + loopWidth;
		path.lineTo(xAux, yAux);
		yAux = y + height / 2;
		path.quadTo(x, bottom, x, yAux);

		xAux = x + loopWidth;
		yAux = y + loopWidth / 2;
		path.quadTo(x, y, xAux, yAux);
		path.closePath();
		return path;
	}
}
