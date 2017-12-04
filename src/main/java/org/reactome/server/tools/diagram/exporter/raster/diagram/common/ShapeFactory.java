package org.reactome.server.tools.diagram.exporter.raster.diagram.common;

import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;

import java.awt.*;
import java.awt.geom.*;
import java.util.Arrays;
import java.util.List;

/**
 * Convenient place to find no so common shapes.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ShapeFactory {

	private static final double COMPLEX_RECT_ARC_WIDTH = 6;
	private static final double ROUND_RECT_ARC_WIDTH = 8;
	private static final double RNA_LOOP_WIDTH = 16;
	private static final double GENE_SYMBOL_WIDTH = 50;
	private static final double GENE_SYMBOL_PAD = 4;
	private static final double ARROW_LENGTH = 8;

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
		final double corner = COMPLEX_RECT_ARC_WIDTH;
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
	 * @return the gene fill shape
	 */
	public static Shape getGeneFillShape(NodeProperties prop) {
		final GeneralPath path = new GeneralPath();
		final double y1 = prop.getY() + 0.5 * GENE_SYMBOL_WIDTH;
		final double bottom = (double) prop.getY() + prop.getHeight();
		final double arcWidth = ROUND_RECT_ARC_WIDTH;
		final double right = (double) prop.getX() + prop.getWidth();
		path.moveTo(prop.getX(), y1);
		path.lineTo(right, y1);
		path.lineTo(right, bottom - arcWidth);
		path.quadTo(right, bottom, right - arcWidth, bottom);
		path.lineTo(prop.getX() + arcWidth, bottom);
		path.quadTo(prop.getX(), bottom, prop.getX(), bottom - arcWidth);
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
		final double y1 = y + 0.5 * GENE_SYMBOL_WIDTH;
		final double right = x + width;
		final Path2D path = new GeneralPath();
		path.moveTo(x, y1);
		path.lineTo(right, y1);
		// Vertical line
		final double x1 = right - GENE_SYMBOL_PAD;
		final double y2 = y1 - 0.5 * GENE_SYMBOL_WIDTH;
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
		final double toX = right + ARROW_LENGTH;
		final double y1 = y + 0.5 * GENE_SYMBOL_WIDTH;
		final double y2 = y1 - 0.5 * GENE_SYMBOL_WIDTH;
		final Path2D triangle = new GeneralPath();
		triangle.moveTo(toX, y2);
		final double ay = y2 + 0.5 * ARROW_LENGTH;
		final double by = y2 - 0.5 * ARROW_LENGTH;
		triangle.lineTo(right, ay);
		triangle.lineTo(right, by);
		triangle.closePath();
		return triangle;
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
				ROUND_RECT_ARC_WIDTH,
				ROUND_RECT_ARC_WIDTH);
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
				ROUND_RECT_ARC_WIDTH,
				ROUND_RECT_ARC_WIDTH);
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

	public static Shape innerCircle(org.reactome.server.tools.diagram.data.layout.Shape shape) {
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
	public static Shape getShape(org.reactome.server.tools.diagram.data.layout.Shape shape) {
		switch (shape.getType()) {
			case "ARROW":
				return (arrow(shape));
			case "BOX":
				return (box(shape));
			case "CIRCLE":
			case "DOUBLE_CIRCLE":
				return (circle(shape));
			case "STOP":
				return (stop(shape));
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
				new Line2D.Double(
						properties.getX(),
						properties.getY(),
						properties.getX() + properties.getWidth(),
						properties.getY() + properties.getHeight()),
				new Line2D.Double(
						properties.getX(),
						properties.getY() + properties.getHeight(),
						properties.getX() + properties.getWidth(),
						properties.getY())
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

	public static Shape getRnaShape(NodeProperties prop) {
		double right = (double) prop.getX() + prop.getWidth();
		double bottom = prop.getY() + (double) prop.getHeight();
		final Path2D path = new GeneralPath();

		double xAux = prop.getX() + RNA_LOOP_WIDTH;
		double yAux = prop.getY() + RNA_LOOP_WIDTH / 2;
		path.moveTo(xAux, yAux);
		xAux = right - RNA_LOOP_WIDTH;
		path.lineTo(xAux, yAux);
		yAux = prop.getY() + prop.getHeight() / 2;
		path.quadTo(right, prop.getY(), right, yAux);

		xAux = right - RNA_LOOP_WIDTH;
		yAux = bottom - RNA_LOOP_WIDTH / 2;
		path.quadTo(right, bottom, xAux, yAux);

		xAux = prop.getX() + RNA_LOOP_WIDTH;
		path.lineTo(xAux, yAux);
		yAux = prop.getY() + prop.getHeight() / 2;
		path.quadTo(prop.getX(), bottom, prop.getX(), yAux);

		xAux = prop.getX() + RNA_LOOP_WIDTH;
		yAux = prop.getY() + RNA_LOOP_WIDTH / 2;
		path.quadTo(prop.getX(), prop.getY(), xAux, yAux);
		path.closePath();
		return path;
	}
}
