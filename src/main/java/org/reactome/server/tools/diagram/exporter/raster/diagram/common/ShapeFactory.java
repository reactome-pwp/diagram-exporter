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
	private static final double GENE_SYMBOL_Y_OFFSET = 50;
	private static final double GENE_SYMBOL_PAD = 4;
	private static final double ARROW_LENGTH = 8;
	private static final double ENCAPSULATED_TANH = Math.tanh(Math.PI / 9);

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
		final double y1 = prop.getY() + 0.5 * GENE_SYMBOL_Y_OFFSET;
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
	 * Returns a path with three perpendicular lines.
	 */
	public static Shape getGeneLine(NodeProperties prop) {
		final double y1 = prop.getY() + 0.5 * GENE_SYMBOL_Y_OFFSET;
		final double maxX = prop.getX() + prop.getWidth();
		final Path2D path = new GeneralPath();
		// Horizontal line
		path.moveTo(prop.getX(), y1);
		path.lineTo(maxX, y1);
		// Vertical line
		final double x1 = maxX - GENE_SYMBOL_PAD;
		path.moveTo(x1, y1);
		path.lineTo(x1, prop.getY());
		// another very short horizontal line
		path.lineTo(maxX, prop.getY());
		return path;
	}

	/**
	 * Creates the arrow shape of a gene.
	 *
	 * @return the gene arrow
	 */
	public static Shape getGeneArrow(NodeProperties prop) {
		final double maxX = prop.getX() + prop.getWidth();
		final double arrowX = maxX + ARROW_LENGTH;
		final double ay = prop.getY() + 0.5 * ARROW_LENGTH;
		final double by = prop.getY() - 0.5 * ARROW_LENGTH;
		final Path2D triangle = new GeneralPath();
		triangle.moveTo(arrowX, prop.getY());
		triangle.lineTo(maxX, ay);
		triangle.lineTo(maxX, by);
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

	public static Shape line(Coordinate from, Coordinate to) {
		return new Line2D.Double(from.getX(), from.getY(), to.getX(), to.getY());
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
		final double xoffset = RNA_LOOP_WIDTH;
		final double yoffset = 0.5 * RNA_LOOP_WIDTH;
		double x = prop.getX();
		double maxX = prop.getX() + prop.getWidth();
		double x1 = x + xoffset;
		double x2 = maxX - xoffset;

		double y = prop.getY();
		double centerY = prop.getY() + 0.5 * prop.getHeight();
		double maxY = prop.getY() + prop.getHeight();
		double y1 = prop.getY() + yoffset;
		double y2 = maxY - yoffset;

		final Path2D path = new GeneralPath();
		path.moveTo(x1, y1);
		path.lineTo(x2, y1);
		path.quadTo(maxX, y, maxX, centerY);
		path.quadTo(maxX, maxY, x2, y2);
		path.lineTo(x1, y2);
		path.quadTo(x, maxY, x, centerY);
		path.quadTo(x, y, x1, y1);
		path.closePath();
		return path;
	}

	public static Shape hexagon(NodeProperties prop) {
		return hexagon(prop, 0);
	}

	public static Shape hexagon(NodeProperties prop, double padding) {
		final double x = prop.getX() + padding;
		final double y = prop.getY() + padding;
		final double maxX = x + prop.getWidth() - 2 * padding;
		final double maxY = y + prop.getHeight() - 2 * padding;
		final double height = maxY - y;

		final double corner = height * 0.5 * ENCAPSULATED_TANH;
		final double x1 = x + corner;
		final double x2 = maxX - corner;
		final double centerY = y + 0.5 * (maxY - y);

		final Path2D path2D = new GeneralPath();
		path2D.moveTo(x, centerY);
		path2D.lineTo(x1, y);
		path2D.lineTo(x2, y);
		path2D.lineTo(maxX, centerY);
		path2D.lineTo(x2, maxY);
		path2D.lineTo(x1, maxY);
		path2D.closePath();
		return path2D;
	}
}
