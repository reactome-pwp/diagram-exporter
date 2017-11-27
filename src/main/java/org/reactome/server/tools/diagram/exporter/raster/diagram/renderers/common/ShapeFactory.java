package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common;

import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;

import java.awt.*;
import java.awt.geom.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Convenient place to find no so common shapes.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ShapeFactory {

	/**
	 * Creates a rectangle with edged corners (an octagon)
	 *
	 * @param x      top left x coordinate
	 * @param y      top left y coordinate
	 * @param width  width
	 * @param height height
	 *
	 * @return an edged rectangle
	 */
	public static Shape getCornedRectangle(double x, double y, double width, double height) {
		final double corner = RendererProperties.COMPLEX_RECT_ARC_WIDTH;
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
	 * @param x      top left x coordinate
	 * @param y      top left y coordinate
	 * @param width  width
	 * @param height height
	 *
	 * @return the gene fill shape
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
	 * @param x     top left x coordinate
	 * @param y     top left y coordinate
	 * @param width width
	 *
	 * @return a path of two perpendicular lines
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
	 * @param x     top left x coordinate
	 * @param y     top left y coordinate
	 * @param width width
	 *
	 * @return the gene arrow
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

	public static Shape getRnaShape(double x, double y, double width, double height) {
		final double loopWidth = RendererProperties.RNA_LOOP_WIDTH;
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

	public static Shape roundedRectangle(NodeProperties properties) {
		return roundedRectangle(properties.getX(), properties.getY(),
				properties.getWidth(), properties.getHeight());
	}

	public static Shape roundedRectangle(double x, double y, double width, double height) {
		return new RoundRectangle2D.Double(
				x,
				y,
				width,
				height,
				RendererProperties.ROUND_RECT_ARC_WIDTH,
				RendererProperties.ROUND_RECT_ARC_WIDTH);
	}

	public static Shape roundedRectangle(NodeProperties prop, double padding) {
		return roundedRectangle(prop.getX(), prop.getY(),
				prop.getWidth(), prop.getHeight(), padding);
	}

	private static Shape roundedRectangle(double x, double y, double width, double height, double padding) {
		return new RoundRectangle2D.Double(
				x + padding,
				y + padding,
				width - 2 * padding,
				height - 2 * padding,
				RendererProperties.ROUND_RECT_ARC_WIDTH,
				RendererProperties.ROUND_RECT_ARC_WIDTH);
	}

	private static Shape arrow(org.reactome.server.tools.diagram.data.layout.Shape shape) {
		final int[] xs = new int[]{
				shape.getA().getX().intValue(),
				shape.getB().getX().intValue(),
				shape.getC().getX().intValue()
		};
		final int[] ys = new int[]{
				shape.getA().getY().intValue(),
				shape.getB().getY().intValue(),
				shape.getC().getY().intValue()
		};
		return new Polygon(xs, ys, xs.length);
	}

	private static Shape box(org.reactome.server.tools.diagram.data.layout.Shape shape) {
		return new Rectangle2D.Double(
				shape.getA().getX(),
				shape.getA().getY(),
				shape.getB().getX() - shape.getA().getX(),
				shape.getB().getY() - shape.getA().getY());
	}

	private static Shape circle(org.reactome.server.tools.diagram.data.layout.Shape shape) {
		final double x = shape.getC().getX() - shape.getR();
		final double y = shape.getC().getY() - shape.getR();
		return new Ellipse2D.Double(
				x,
				y,
				2 * shape.getR(),
				2 * shape.getR());
	}

	private static Shape innerCircle(org.reactome.server.tools.diagram.data.layout.Shape shape) {
		final double x = shape.getC().getX() - shape.getR1();
		final double y = shape.getC().getY() - shape.getR1();
		return new Ellipse2D.Double(
				x,
				y,
				2 * shape.getR1(),
				2 * shape.getR1()
		);
	}

	private static Shape stop(org.reactome.server.tools.diagram.data.layout.Shape shape) {
		return new Line2D.Double(
				shape.getA().getX(),
				shape.getA().getY(),
				shape.getB().getX(),
				shape.getB().getY()
		);
	}

	/**
	 * Returns a list of java.awt.shapes that make up the reactome Shape.
	 * Although most of the shapes are unique, the double circle returns two
	 * circles.
	 *
	 * @param shape reactome shape
	 *
	 * @return a list of java shapes
	 */
	// TODO: Is it ok to return a list of shapes just because of the inner circle?
	public static List<Shape> getShapes(org.reactome.server.tools.diagram.data.layout.Shape shape) {
		switch (shape.getType()) {
			case "ARROW":
				return Collections.singletonList(arrow(shape));
			case "BOX":
				return Collections.singletonList(box(shape));
			case "CIRCLE":
				return Collections.singletonList(circle(shape));
			case "DOUBLE_CIRCLE":
				return Arrays.asList(circle(shape), innerCircle(shape));
			case "STOP":
				return Collections.singletonList(stop(shape));
			default:
				throw new RuntimeException("Do not know shape " + shape.getType());
		}
	}

	public static Shape line(Coordinate from, Coordinate to) {
		return new Line2D.Double(from.getX(),
				from.getY(), to.getX(), to.getY());
	}

	public static List<Shape> cross(NodeProperties properties) {
		return Arrays.asList(
				new Line2D.Double(properties.getX(), properties.getY(),
						properties.getX() + properties.getWidth(),
						properties.getY() + properties.getHeight()),
				new Line2D.Double(properties.getX(), properties.getY() + properties.getHeight(),
						properties.getX() + properties.getWidth(), properties.getY())
		);
	}

	public static Shape rectangle(NodeProperties prop, double padding) {
		return new Rectangle2D.Double(prop.getX() + padding,
				prop.getY() + padding,
				prop.getWidth() - 2 * padding,
				prop.getHeight() - 2 * padding);

	}

	public static Shape rectangle(NodeProperties prop) {
		return new Rectangle2D.Double(prop.getX(), prop.getY(), prop.getWidth(), prop.getHeight());
	}
}
