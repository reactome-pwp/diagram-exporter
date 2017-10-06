package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.*;
import java.util.List;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class EdgeRenderer extends AbstractRenderer {

	/**
	 * Returns a list of java.awt.shapes that make up the reactome Shape.
	 * Although most of the shapes are unique, the double circle returns two
	 * circles.
	 *
	 * @param shape reactome shape
	 * @param scale AdvancedGraphics2D factor
	 *
	 * @return a list of java shapes
	 */
	// TODO: Is it ok to return a list of shapes just because of the inner circle?
	List<java.awt.Shape> getScaledShapes(Shape shape, double scale) {
		switch (shape.getType()) {
			case "ARROW":
				return Collections.singletonList(arrow(shape, scale));
			case "BOX":
				return Collections.singletonList(box(shape, scale));
			case "CIRCLE":
				return Collections.singletonList(circle(shape, scale));
			case "DOUBLE_CIRCLE":
				return Arrays.asList(circle(shape, scale), innerCircle(shape, scale));
			case "STOP":
				return Collections.singletonList(stop(shape, scale));
			default:
				throw new RuntimeException("Do not know shape " + shape.getType());
		}
	}

	private void drawReactionText(AdvancedGraphics2D graphics, EdgeCommon edge) {
		if (edge.getReactionShape().getS() == null)
			return;
		final Shape shape = edge.getReactionShape();
		TextRenderer.drawText(graphics, shape.getS(),
				shape.getA().getX(), shape.getA().getY(),
				shape.getB().getX() - shape.getA().getX(),
				shape.getB().getY() - shape.getA().getY(),
				graphics.getFactor(), true);
	}

	private java.awt.Shape arrow(Shape shape, double factor) {
		final int[] xs = new int[]{
				(int) (factor * shape.getA().getX()),
				(int) (factor * shape.getB().getX()),
				(int) (factor * shape.getC().getX())
		};
		final int[] ys = new int[]{
				(int) (factor * shape.getA().getY()),
				(int) (factor * shape.getB().getY()),
				(int) (factor * shape.getC().getY())
		};
		return new Polygon(xs, ys, xs.length);
	}

	protected java.awt.Shape box(Shape shape, double factor) {
		return new Rectangle(
				(int) (factor * shape.getA().getX()),
				(int) (factor * shape.getA().getY()),
				(int) (factor * (shape.getB().getX() - shape.getA().getX())),
				(int) (factor * (shape.getB().getY() - shape.getA().getY())));
	}

	private java.awt.Shape circle(Shape shape, double factor) {
		final double x = shape.getC().getX() - shape.getR();
		final double y = shape.getC().getY() - shape.getR();
		return new Ellipse2D.Double(
				factor * x,
				factor * y,
				factor * 2 * shape.getR(),
				factor * 2 * shape.getR());
	}

	private java.awt.Shape innerCircle(Shape shape, double factor) {
		final double x = shape.getC().getX() - shape.getR1();
		final double y = shape.getC().getY() - shape.getR1();
		return new Ellipse2D.Double(
				factor * x,
				factor * y,
				factor * 2 * shape.getR1(),
				factor * 2 * shape.getR1()
		);
//		graphics.drawOval(x, y, shape.getR1() * 2, shape.getR1() * 2);
	}

	private java.awt.Shape stop(Shape shape, double factor) {
		return new Line2D.Double(
				factor * shape.getA().getX(),
				factor * shape.getA().getY(),
				factor * shape.getB().getX(),
				factor * shape.getB().getY()
		);
	}

	/**
	 * Renders a complete reaction in this order: segments, fills, borders and
	 * text. For the fill, shapes are computed and separated into empty and
	 * nonEmpty lists.
	 *
	 * @param graphics      where to render
	 * @param items         list of edges
	 * @param fillColor     color for filling
	 * @param lineColor     color for segments and borders
	 * @param textColor     color for text
	 */
	@Override
	public void draw(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items, Paint fillColor, Paint lineColor, Paint textColor, Stroke borderStroke) {
		final Collection<EdgeCommon> edges = (Collection<EdgeCommon>) items;

		// separate reactions and ends in black and white
		final List<java.awt.Shape> empty = new LinkedList<>();
		final List<java.awt.Shape> nonEmpty = new LinkedList<>();
		edges.forEach(edge -> {
			//noinspection Duplicates (some as Connector, but no common interface)
			if (edge.getEndShape() != null) {
				final List<java.awt.Shape> rendered = getScaledShapes(edge.getEndShape(), graphics.getFactor());
				if (edge.getEndShape().getEmpty() == null)
					nonEmpty.addAll(rendered);
				else
					empty.addAll(rendered);
			}
			if (edge.getReactionShape() != null) {
				final List<java.awt.Shape> rendered = getScaledShapes(edge.getReactionShape(), graphics.getFactor());
				if (edge.getReactionShape().getEmpty() == null)
					nonEmpty.addAll(rendered);
				else
					empty.addAll(rendered);
			}
		});
		fill(graphics, fillColor, lineColor, empty, nonEmpty);
		empty.addAll(nonEmpty);
		border(graphics, lineColor, borderStroke, empty);
		text(graphics, lineColor, edges);
	}

	/**
	 * Renders the segments of each edges using lineColor and Stroke to model
	 * the lines.
	 *
	 * @param graphics  where to render
	 * @param lineColor color for segments
	 * @param stroke    line style
	 * @param edges     list of edges to render
	 */
	public void segments(AdvancedGraphics2D graphics, Paint lineColor, Stroke stroke, Collection<? extends EdgeCommon> edges) {
		graphics.getGraphics().setStroke(stroke);
		graphics.getGraphics().setPaint(lineColor);
		edges.stream()
				.map(EdgeCommon::getSegments)
				.flatMap(Collection::stream)
				.forEach(segment -> drawSegment(graphics, segment));
	}

	/**
	 * Renders the fill of the reaction shapes, separated into empty or non
	 * empty lists.
	 *
	 * @param graphics      where to render
	 * @param emptyColor    color for empty
	 * @param nonEmptyColor color for non empty
	 * @param empties       shapes to be filled as empty
	 * @param filled        shapes to be filled as non empty
	 */
	public void fill(AdvancedGraphics2D graphics, Paint emptyColor, Paint nonEmptyColor, List<java.awt.Shape> empties, List<java.awt.Shape> filled) {
		graphics.getGraphics().setPaint(emptyColor);
		empties.forEach(shape -> graphics.getGraphics().fill(shape));
		graphics.getGraphics().setPaint(nonEmptyColor);
		filled.forEach(shape -> graphics.getGraphics().fill(shape));
	}

	/**
	 * Renders the reactions' shapes' borders.
	 *
	 * @param graphics     where to render
	 * @param lineColor    color for borders
	 * @param borderStroke stroke for borders
	 * @param shapes       list of precomputed shapes
	 */
	public void border(AdvancedGraphics2D graphics, Paint lineColor, Stroke borderStroke, List<java.awt.Shape> shapes) {
		graphics.getGraphics().setPaint(lineColor);
		graphics.getGraphics().setStroke(borderStroke);
		shapes.forEach(shape -> graphics.getGraphics().draw(shape));
	}

	/**
	 * Renders the edges' texts
	 *
	 * @param graphics  where to render
	 * @param textColor color for texts
	 * @param edges     list of edges
	 */
	public void text(AdvancedGraphics2D graphics, Paint textColor, Collection<? extends EdgeCommon> edges) {
		graphics.getGraphics().setPaint(textColor);
		edges.forEach(edge -> drawReactionText(graphics, edge));
	}

	/**
	 * Renders a segment by drawing a line from segment.getFrom() to
	 * segment.getTo()
	 *
	 * @param graphics where to render
	 * @param segment  segment to render
	 */
	protected void drawSegment(AdvancedGraphics2D graphics, Segment segment) {
		final double x = segment.getFrom().getX() * graphics.getFactor();
		final double y = segment.getFrom().getY() * graphics.getFactor();
		final double x1 = segment.getTo().getX() * graphics.getFactor();
		final double y1 = segment.getTo().getY() * graphics.getFactor();
		graphics.getGraphics().drawLine((int) x, (int) y, (int) x1, (int) y1);
	}
}
