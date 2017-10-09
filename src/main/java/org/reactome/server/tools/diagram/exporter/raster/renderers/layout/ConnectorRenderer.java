package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.util.Collection;
import java.util.Set;

/**
 * Connectors have a similar behaviour than reactions: segments, shapes and
 * texts, but they do NOT share interface, so its rendering is made with a
 * different renderer. Connectors do not have reaction shape, instead they have
 * stoichiometry box when its value is more than 1.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ConnectorRenderer {

//	/**
//	 * Renders a list of connectors in this order: segments, fills, borders and
//	 * texts. For the fills, shapes are precomputed and separated into empty and
//	 * non empty lists.
//	 *
//	 * @param graphics   where to render
//	 * @param connectors list of connectors
//	 * @param fillColor  filling color for empty shapes
//	 * @param lineColor  color for borders, non empty shapes and texts
//	 */
//	public void drawConnectors(AdvancedGraphics2D graphics, Collection<Connector> connectors, Paint fillColor, Paint lineColor, Stroke borderStroke) {
//		// Fills
//		// separate stoichiometries and end shapes in black and white
//		final List<java.awt.Shape> empty = new LinkedList<>();
//		final List<java.awt.Shape> nonEmpty = new LinkedList<>();
//		connectors.forEach(connector -> {
//			//noinspection Duplicates (similar to edges, but don't share interface)
//			if (connector.getEndShape() != null) {
//				final List<java.awt.Shape> rendered = ShapeFactory.createShape(connector.getEndShape(), graphics.getFactor());
//				if (connector.getEndShape().getEmpty() == null)
//					nonEmpty.addAll(rendered);
//				else
//					empty.addAll(rendered);
//			}
//			if (connector.getStoichiometry() != null && connector.getStoichiometry().getShape() != null) {
//				final List<java.awt.Shape> box = ShapeFactory.createShape(connector.getStoichiometry().getShape(), graphics.getFactor());
//				empty.addAll(box);
//			}
//		});
//		fill(graphics, fillColor, lineColor, empty, nonEmpty);
//
//		// Borders
//		empty.addAll(nonEmpty);
//		graphics.getGraphics().setPaint(lineColor);
//		graphics.getGraphics().setStroke(borderStroke);
//		empty.forEach(shape -> graphics.getGraphics().draw(shape));
//
//		// Text
//		// They have same color as border
//		connectors.forEach(connector -> text(graphics, connector));
//	}

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

//	private void fill(AdvancedGraphics2D graphics, Paint fillColor, Paint lineColor, List<java.awt.Shape> empty, List<java.awt.Shape> nonEmpty) {
//		// white
//		graphics.getGraphics().setPaint(fillColor);
//		empty.forEach(shape -> graphics.getGraphics().fill(shape));
//		// black
//		graphics.getGraphics().setPaint(lineColor);
//		nonEmpty.forEach(shape -> graphics.getGraphics().fill(shape));
//	}

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

	public void border(AdvancedGraphics2D graphics, Collection<Connector> connectors) {
		connectors.stream()
				.filter(connector -> connector.getEndShape() != null)
				.map(connector -> ShapeFactory.createShape(connector.getEndShape(), graphics.getFactor()))
				.flatMap(Collection::stream)
				.forEach(graphics.getGraphics()::draw);
		connectors.stream()
				.filter(connector -> connector.getStoichiometry().getShape() != null)
				.map(connector -> ShapeFactory.createShape(connector.getStoichiometry().getShape(), graphics.getFactor()))
				.flatMap(Collection::stream)
				.forEach(graphics.getGraphics()::draw);
	}

	public void text(AdvancedGraphics2D graphics, Collection<Connector> connectors) {
		connectors.forEach(connector -> text(graphics, connector));
	}
}
