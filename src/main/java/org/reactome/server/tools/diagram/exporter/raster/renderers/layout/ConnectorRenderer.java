package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ConnectorRenderer extends EdgeRenderer {

	/**
	 * Renders a list of connectors in this order: segments, fills, borders and
	 * texts. For the fills, shapes are precomputed and separated into empty and
	 * non empty lists.
	 *  @param graphics
	 * @param items
	 * @param fillColor
	 * @param lineColor
	 * @param textColor
	 * @param lineStroke
	 */
	public void drawConnectors(AdvancedGraphics2D graphics, Collection<Connector> items, Paint fillColor, Paint lineColor, Paint textColor, Stroke lineStroke, Stroke borderStroke) {
		final Collection<Connector> connectors = items;
		connectorSegments(graphics, lineColor, lineStroke, connectors);
		// Fills
		// separate reactions and ends in black and white
		final List<java.awt.Shape> empty = new LinkedList<>();
		final List<java.awt.Shape> nonEmpty = new LinkedList<>();
		connectors.forEach(connector -> {
			if (connector.getEndShape() != null) {
				final List<java.awt.Shape> rendered = getScaledShapes(connector.getEndShape(), graphics.getFactor());
				if (connector.getEndShape().getEmpty() == null)
					nonEmpty.addAll(rendered);
				else
					empty.addAll(rendered);
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
		final Shape shape = connector.getEndShape();
		if (shape != null && shape.getS() != null)
			graphics.drawText(shape.getS(),
					shape.getA().getX(), shape.getA().getY(),
					shape.getB().getX() - shape.getA().getX(),
					shape.getB().getY() - shape.getA().getY(), 0.0);
	}

	private void fillConnectors(AdvancedGraphics2D graphics, Paint fillColor, Paint lineColor, List<java.awt.Shape> empty, List<java.awt.Shape> nonEmpty) {
		// white
		graphics.getGraphics().setPaint(fillColor);
		empty.forEach(shape -> graphics.getGraphics().fill(shape));
		// black
		graphics.getGraphics().setPaint(lineColor);
		nonEmpty.forEach(shape -> graphics.getGraphics().fill(shape));
	}

	private void connectorSegments(AdvancedGraphics2D graphics, Paint lineColor, Stroke stroke, Collection<Connector> connectors) {
		graphics.getGraphics().setStroke(stroke);
		graphics.getGraphics().setPaint(lineColor);
		connectors.stream()
				.map(Connector::getSegments)
				.flatMap(Collection::stream)
				.forEach(segment -> drawSegment(graphics, segment));
	}
}
