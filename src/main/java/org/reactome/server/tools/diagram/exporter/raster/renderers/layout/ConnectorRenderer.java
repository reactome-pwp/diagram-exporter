package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.raster.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.StrokeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.FillLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.LineLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.TextLayer;

import java.awt.*;
import java.util.List;

/**
 * Connectors have a similar behaviour than reactions: segments, shapes and
 * texts, but they do NOT share interface, so its rendering is made with a
 * different renderer. Connectors do not have reaction shape, instead they have
 * stoichiometry box when its value is more than 1.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ConnectorRenderer {

	public void draw(DiagramCanvas canvas, Connector connector, DiagramProfile diagramProfile, double factor, DiagramIndex index) {
		if (connector.getIsFadeOut() != null && connector.getIsFadeOut())
			asFadeOut(canvas, connector, diagramProfile, factor);
		else asNormal(canvas, connector, diagramProfile, index, factor);
	}

	private void asFadeOut(DiagramCanvas canvas, Connector connector, DiagramProfile diagramProfile, double factor) {
		final LineLayer segments = canvas.getFadeOutSegments();
		final FillLayer fillLayer = canvas.getFadeOutFills();
		final LineLayer lineLayer = canvas.getFadeOutBorders();
		final TextLayer textLayer = canvas.getFadeOutText();

		final Stroke stroke = StrokeProperties.SEGMENT_STROKE;
		final String border = diagramProfile.getReaction().getFadeOutStroke();
		final String fill = diagramProfile.getReaction().getFadeOutFill();
		final Shape shape = connector.getEndShape();

		connector.getSegments().stream()
				.map(segment -> ShapeFactory.line(factor, segment.getFrom(), segment.getTo()))
				.forEach(sh -> segments.add(border, stroke, sh));
		// Shapes and borders
		drawEndShape(factor, fill, border, stroke, shape, textLayer, lineLayer, fillLayer);
		stoichiometry(connector, factor, border, fill, fillLayer, textLayer, lineLayer);
	}

	private void asNormal(DiagramCanvas canvas, Connector connector, DiagramProfile diagramProfile, DiagramIndex index, double factor) {
		final LineLayer segments = canvas.getSegments();
		final FillLayer fillLayer = canvas.getFill();
		final LineLayer lineLayer = canvas.getBorder();
		final TextLayer textLayer = canvas.getText();

		final Stroke stroke = StrokeProperties.SEGMENT_STROKE;
		final String border = getSegmentColor(connector, diagramProfile, index);
		final String fill = diagramProfile.getReaction().getFill();
		final String haloColor = diagramProfile.getProperties().getHalo();
		final Shape shape = connector.getEndShape();

		connector.getSegments().stream()
				.map(segment -> ShapeFactory.line(factor, segment.getFrom(), segment.getTo()))
				.forEach(line -> segments.add(border, stroke, line));
		// Shapes and borders
		drawEndShape(factor, fill, border, stroke, shape, textLayer, lineLayer, fillLayer);
		stoichiometry(connector, factor, border, fill, fillLayer, textLayer, lineLayer);

	}

	private void drawEndShape(double factor, String fill, String border, Stroke borderStroke, Shape shape, TextLayer textLayer, LineLayer linel, FillLayer fillLayer) {
		if (shape != null) {
			final String color = isEmpty(shape) ? fill : border;
			final List<java.awt.Shape> shapes = ShapeFactory.createShape(shape, factor);
			shapes.forEach(s -> {
				fillLayer.add(color, s);
				linel.add(border, borderStroke, s);
			});
			if (shape.getS() != null && !shape.getS().isEmpty()) {
				final NodeProperties limits = new ScaledNodeProperties(
						NodePropertiesFactory.get(
								shape.getA().getX(), shape.getA().getY(),
								shape.getB().getX() - shape.getA().getX(),
								shape.getB().getY() - shape.getA().getY()),
						factor);
				// Texts
				textLayer.add(border, shape.getS(), limits, factor);
			}
		}
	}

	private void stoichiometry(Connector connector, double factor, String color, String fill, FillLayer fillLayer, TextLayer textLayer, LineLayer lineLayer) {
		if (connector.getStoichiometry() != null && connector.getStoichiometry().getShape() != null) {
			final Shape stShape = connector.getStoichiometry().getShape();
			final List<java.awt.Shape> shapes = ShapeFactory.createShape(stShape, factor);
			final boolean empty = isEmpty(stShape);
			shapes.forEach(sh -> {
				fillLayer.add(empty ? fill : color, sh);
				lineLayer.add(color, StrokeProperties.SEGMENT_STROKE, sh);
			});
			final String text = connector.getStoichiometry().getValue().toString();
			final NodeProperties limits = new ScaledNodeProperties(
					NodePropertiesFactory.get(
							stShape.getA().getX(), stShape.getA().getY(),
							stShape.getB().getX() - stShape.getA().getX(),
							stShape.getB().getY() - stShape.getA().getY()), factor);
			textLayer.add(color, text, limits, factor);
		}
	}

	private String getSegmentColor(Connector connector, DiagramProfile diagramProfile, DiagramIndex index) {
		if (index.getSelected().contains(connector.getEdgeId()))
			return diagramProfile.getProperties().getSelection();
		if (connector.getIsDisease() != null && connector.getIsDisease())
			return diagramProfile.getProperties().getDisease();
		return diagramProfile.getReaction().getStroke();

	}

	private boolean isEmpty(Shape shape) {
		return shape.getEmpty() != null && shape.getEmpty();
	}
}
