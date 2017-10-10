package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.awt.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Connectors have a similar behaviour than reactions: segments, shapes and
 * texts, but they do NOT share interface, so its rendering is made with a
 * different renderer. Connectors do not have reaction shape, instead they have
 * stoichiometry box when its value is more than 1.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ConnectorRenderer {

	private void text(AdvancedGraphics2D graphics, Connector connector) {
		endShapeText(graphics, connector);
		stoichiometryText(graphics, connector);
	}

	private void endShapeText(AdvancedGraphics2D graphics, Connector connector) {
		final Shape shape = connector.getEndShape();
		if (shape == null || shape.getS() == null)
			return;
		TextRenderer.drawText(graphics, shape.getS(),
				shape.getA().getX(), shape.getA().getY(),
				shape.getB().getX() - shape.getA().getX(),
				shape.getB().getY() - shape.getA().getY(),
				graphics.getFactor(), true);
	}

	private void stoichiometryText(AdvancedGraphics2D graphics, Connector connector) {
		if (connector.getStoichiometry() == null || connector.getStoichiometry().getShape() == null)
			return;
		final Shape stShape = connector.getStoichiometry().getShape();
		TextRenderer.drawText(graphics,
				connector.getStoichiometry().getValue().toString(),
				stShape.getA().getX(), stShape.getA().getY(),
				stShape.getB().getX() - stShape.getA().getX(),
				stShape.getB().getY() - stShape.getA().getY(),
				graphics.getFactor(), true);
	}

	public void segments(AdvancedGraphics2D graphics, Set<Connector> connectors) {
		connectors.stream()
				.map(Connector::getSegments)
				.flatMap(Collection::stream)
				.map(segment -> ShapeFactory.line(graphics, segment.getFrom(), segment.getTo()))
				.forEach(line -> graphics.getGraphics().draw(line));
	}

	public void fill(AdvancedGraphics2D graphics, Collection<Connector> connectors) {
		connectors.stream()
				.filter(connector -> connector.getEndShape() != null)
				.map(connector -> ShapeFactory.createShape(connector.getEndShape(), graphics.getFactor()))
				.flatMap(Collection::stream)
				.forEach(graphics.getGraphics()::fill);
		connectors.stream()
				.filter(connector -> connector.getStoichiometry().getShape() != null)
				.map(connector -> ShapeFactory.createShape(connector.getStoichiometry().getShape(), graphics.getFactor()))
				.flatMap(Collection::stream)
				.forEach(graphics.getGraphics()::fill);
	}

//	public void border(AdvancedGraphics2D graphics, Collection<Connector> connectors) {
//		connectors.stream()
//				.filter(connector -> connector.getEndShape() != null)
//				.map(connector -> ShapeFactory.createShape(connector.getEndShape(), graphics.getFactor()))
//				.flatMap(Collection::stream)
//				.forEach(graphics.getGraphics()::draw);
//		connectors.stream()
//				.filter(connector -> connector.getStoichiometry().getShape() != null)
//				.map(connector -> ShapeFactory.createShape(connector.getStoichiometry().getShape(), graphics.getFactor()))
//				.flatMap(Collection::stream)
//				.forEach(graphics.getGraphics()::draw);
//	}

	public void text(AdvancedGraphics2D graphics, Collection<Connector> connectors) {
		connectors.forEach(connector -> text(graphics, connector));
	}

	public void draw(AdvancedGraphics2D graphics, Set<Connector> connectors, Paint fill, Paint border) {
		final Map<Boolean, Set<java.awt.Shape>> divided = divideConnectorShapes(connectors, graphics);
		fillBlackAndWhite(graphics, border, fill, divided);
	}

	private Map<Boolean, Set<java.awt.Shape>> divideConnectorShapes(Collection<Connector> connectors, AdvancedGraphics2D graphics) {
		return divide(graphics, connectors.stream()
				.flatMap(connector -> Stream.of(connector.getEndShape(), connector.getStoichiometry().getShape())));
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
