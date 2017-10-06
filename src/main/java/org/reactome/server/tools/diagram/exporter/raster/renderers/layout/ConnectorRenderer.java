package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Connectors have a similar behaviour than reactions: segments, shapes and
 * texts, but they do NOT share interface, so its rendering is made with a
 * different renderer. Connectors do not have reaction shape, instead they have
 * stoichiometry box when its value is more than 1.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ConnectorRenderer extends EdgeRenderer {

	/**
	 * Renders a list of connectors in this order: segments, fills, borders and
	 * texts. For the fills, shapes are precomputed and separated into empty and
	 * non empty lists.
	 *
	 * @param graphics   where to render
	 * @param connectors list of connectors
	 * @param fillColor  filling color for empty shapes
	 * @param lineColor  color for borders, non empty shapes and texts
	 */
	public void drawConnectors(AdvancedGraphics2D graphics, Collection<Connector> connectors, Paint fillColor, Paint lineColor, Stroke borderStroke) {
		// Fills
		// separate reactions and ends in black and white
		final List<java.awt.Shape> empty = new LinkedList<>();
		final List<java.awt.Shape> nonEmpty = new LinkedList<>();
		connectors.forEach(connector -> {
			//noinspection Duplicates (similar to edges, but don't share interface)
			if (connector.getEndShape() != null) {
				final List<java.awt.Shape> rendered = getScaledShapes(connector.getEndShape(), graphics.getFactor());
				if (connector.getEndShape().getEmpty() == null)
					nonEmpty.addAll(rendered);
				else
					empty.addAll(rendered);
			}
			if (connector.getStoichiometry() != null && connector.getStoichiometry().getShape() != null) {
				final java.awt.Shape box = box(connector.getStoichiometry().getShape(), graphics.getFactor());
				empty.add(box);
			}
		});
		fillConnectors(graphics, fillColor, lineColor, empty, nonEmpty);

		// Borders
		empty.addAll(nonEmpty);
		graphics.getGraphics().setPaint(lineColor);
		graphics.getGraphics().setStroke(borderStroke);
		empty.forEach(shape -> graphics.getGraphics().draw(shape));

		// Text
		// They have same color as border
		connectors.forEach(connector -> text(graphics, connector));
	}

	private void text(AdvancedGraphics2D graphics, Connector connector) {
		endShapeText(graphics, connector);
		stoichiometryText(graphics, connector);
	}

	private void endShapeText(AdvancedGraphics2D graphics, Connector connector) {
		final Shape shape = connector.getEndShape();
		if (shape != null && shape.getS() != null)
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

	private void fillConnectors(AdvancedGraphics2D graphics, Paint fillColor, Paint lineColor, List<java.awt.Shape> empty, List<java.awt.Shape> nonEmpty) {
		// white
		graphics.getGraphics().setPaint(fillColor);
		empty.forEach(shape -> graphics.getGraphics().fill(shape));
		// black
		graphics.getGraphics().setPaint(lineColor);
		nonEmpty.forEach(shape -> graphics.getGraphics().fill(shape));
	}

	public void connectorSegments(AdvancedGraphics2D graphics, Paint lineColor, Stroke stroke, Collection<Connector> connectors) {
		graphics.getGraphics().setStroke(stroke);
		graphics.getGraphics().setPaint(lineColor);
		connectors.stream()
				.map(Connector::getSegments)
				.flatMap(Collection::stream)
				.forEach(segment -> drawSegment(graphics, segment));
	}
}
