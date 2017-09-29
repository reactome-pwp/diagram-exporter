package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

public class EdgeAbstractRenderer extends AbstractRenderer {

	@Override
	public void drawText(AdvancedGraphics2D graphics, DiagramObject item) {
		final EdgeCommon edge = (Edge) item;
		drawReactionText(graphics, edge);
	}

	protected void draw(AdvancedGraphics2D graphics, Segment segment) {
		graphics.drawLine(segment.getFrom(), segment.getTo());
	}

	@Override
	public void drawBorder(AdvancedGraphics2D graphics, DiagramObject item) {
		final Edge edge = (Edge) item;
		drawShape(graphics, edge.getEndShape());
		drawShape(graphics, edge.getReactionShape());
	}

	public void drawSegments(AdvancedGraphics2D graphics, DiagramObject item){
		final Edge edge = (Edge) item;
		edge.getSegments().forEach(segment -> draw(graphics, segment));
	}

	@Override
	public void fill(AdvancedGraphics2D graphics, DiagramObject item) {
		final Edge edge = (Edge) item;
		fillShape(graphics, edge.getEndShape());
		fillShape(graphics, edge.getReactionShape());

	}

	public void fillShape(AdvancedGraphics2D graphics, Shape shape) {
		if (shape == null)
			return;
		final java.awt.Shape awtShape = getScaledShape(shape, graphics.getFactor());
		graphics.getGraphics().fill(awtShape);
	}

	private java.awt.Shape getScaledShape(Shape shape, double scale) {
		switch (shape.getType()) {
			case "ARROW":
				return arrow(shape, scale);
			case "BOX":
				return box(shape, scale);
			case "CIRCLE":
			case "DOUBLE_CIRCLE":
				return circle(shape, scale);
			case "STOP":
				return stop(shape, scale);
			default:
				throw new RuntimeException("Do not know shape " + shape.getType());
		}
	}


	private void drawReactionText(AdvancedGraphics2D graphics, EdgeCommon edge) {
		if (edge.getReactionShape().getS() == null)
			return;
		final Shape shape = edge.getReactionShape();
		graphics.drawText(shape.getS(),
				shape.getA().getX(), shape.getA().getY(),
				shape.getB().getX() - shape.getA().getX(),
				shape.getB().getY() - shape.getA().getY(), 0.0);
	}

	public void drawShape(AdvancedGraphics2D graphics, Shape shape) {
		if (shape == null)
			return;
		final java.awt.Shape scaledShape = getScaledShape(shape, graphics.getFactor());
		graphics.getGraphics().draw(scaledShape);
		if (shape.getType().equals("DOUBLE_CIRCLE")) {
			final java.awt.Shape innerCircle = innerCircle(shape, graphics.getFactor());
			graphics.getGraphics().draw(innerCircle);
		}
	}

	private java.awt.Shape arrow(Shape shape, double scale) {
		final int[] xs = new int[]{
				(int) (scale * shape.getA().getX()),
				(int) (scale * shape.getB().getX()),
				(int) (scale * shape.getC().getX())
		};
		final int[] ys = new int[]{
				(int) (scale * shape.getA().getY()),
				(int) (scale * shape.getB().getY()),
				(int) (scale * shape.getC().getY())
		};
		return new Polygon(xs, ys, xs.length);

	}

	private java.awt.Shape box(Shape shape, double scale) {
		return new Rectangle(
				(int) (scale * shape.getA().getX()),
				(int) (scale * shape.getA().getY()),
				(int) (scale * (shape.getB().getX() - shape.getA().getX())),
				(int) (scale * (shape.getB().getY() - shape.getA().getY())));
	}


	private java.awt.Shape circle(Shape shape, double scale) {
		final double x = shape.getC().getX() - shape.getR();
		final double y = shape.getC().getY() - shape.getR();
		return new Ellipse2D.Double(
				scale * x,
				scale * y,
				scale * 2 * shape.getR(),
				scale * 2 * shape.getR()
		);
	}

	private java.awt.Shape innerCircle(Shape shape, double scale) {
		final double x = shape.getC().getX() - shape.getR1();
		final double y = shape.getC().getY() - shape.getR1();
		return new Ellipse2D.Double(
				scale * x,
				scale * y,
				scale * 2 * shape.getR1(),
				scale * 2 * shape.getR1()
		);
//		graphics.drawOval(x, y, shape.getR1() * 2, shape.getR1() * 2);
	}

	private java.awt.Shape stop(Shape shape, double scale) {
		return new Line2D.Double(
				scale * shape.getA().getX(),
				scale * shape.getA().getY(),
				scale * shape.getB().getX(),
				scale * shape.getB().getY()
		);
	}


}
