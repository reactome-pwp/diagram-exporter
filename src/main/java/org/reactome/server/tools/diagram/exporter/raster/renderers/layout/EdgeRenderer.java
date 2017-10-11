package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Edge;
import org.reactome.server.tools.diagram.data.layout.EdgeCommon;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class EdgeRenderer extends AbstractRenderer {

	@Override
	public void text(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		final Collection<Edge> edges = (Collection<Edge>) items;
		edges.forEach(edgeCommon -> drawReactionText(graphics, edgeCommon));
	}

	private void drawReactionText(AdvancedGraphics2D graphics, Edge edge) {
		if (edge.getReactionShape().getS() == null)
			return;
		final Shape shape = edge.getReactionShape();
		TextRenderer.drawText(graphics, shape.getS(),
				shape.getA().getX(), shape.getA().getY(),
				shape.getB().getX() - shape.getA().getX(),
				shape.getB().getY() - shape.getA().getY(),
				graphics.getFactor(), true);
	}

	@Override
	public void segments(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		final Collection<EdgeCommon> edges = (Collection<EdgeCommon>) items;
		edges.stream().map(EdgeCommon::getSegments)
				.flatMap(Collection::stream)
				.map(segment -> ShapeFactory.line(graphics, segment.getFrom(), segment.getTo()))
				.forEach(shape -> graphics.getGraphics().draw(shape));
	}

	@Override
	public void highlight(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		final Collection<Edge> reactions = (Collection<Edge>) items;
		reactions.stream()
				.map(this::renderableShapes)
				.flatMap(Collection::stream)
				.filter(Objects::nonNull)
				.map(shape -> ShapeFactory.createShape(shape, graphics.getFactor()))
				.flatMap(Collection::stream)
				.forEach(graphics.getGraphics()::draw);
	}

	public void draw(AdvancedGraphics2D graphics, Set<? extends EdgeCommon> edges, Paint fill, Paint border) {
		final Map<Boolean, Set<java.awt.Shape>> divided = divideShapes(edges, graphics);
		fillBlackAndWhite(graphics, border, fill, divided);
	}

	private Map<Boolean, Set<java.awt.Shape>> divideShapes(Set<? extends EdgeCommon> edges, AdvancedGraphics2D graphics) {
		return divide(graphics, edges.stream()
				.map(this::renderableShapes)
				.flatMap(Collection::stream));
	}

	protected List<Shape> renderableShapes(EdgeCommon edge) {
		return Arrays.asList(edge.getReactionShape(), edge.getEndShape());
	}

	private Map<Boolean, Set<java.awt.Shape>> divide(AdvancedGraphics2D graphics, Stream<Shape> shapeStream) {
		final Map<Boolean, Set<java.awt.Shape>> shapes = new HashMap<>();
		shapes.put(true, new HashSet<>());
		shapes.put(false, new HashSet<>());
		shapeStream
				.filter(Objects::nonNull)
				.forEach(shape -> {
					final java.util.List<java.awt.Shape> javaShapes = ShapeFactory.createShape(shape, graphics.getFactor());
					shapes.get(isEmpty(shape)).addAll(javaShapes);
				});
		return shapes;
	}

	private boolean isEmpty(Shape shape) {
		return shape.getEmpty() != null && shape.getEmpty();
	}

	private void fillBlackAndWhite(AdvancedGraphics2D graphics, Paint lineColor, Paint fillColor, Map<Boolean, Set<java.awt.Shape>> shapes) {
		graphics.getGraphics().setPaint(fillColor);
		shapes.get(true).forEach(graphics.getGraphics()::fill);
		graphics.getGraphics().setPaint(lineColor);
		shapes.get(false).forEach(graphics.getGraphics()::fill);
		shapes.values().stream()
				.flatMap(Collection::stream)
				.forEach(graphics.getGraphics()::draw);
	}

}
