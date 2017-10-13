package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfileNode;
import org.reactome.server.tools.diagram.exporter.raster.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.StrokeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.FillLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.LineLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.TextLayer;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Connectors have a similar behaviour than reactions: segments, shapes and
 * texts, but they do NOT share interface, so its rendering is made with a
 * different renderer. Connectors do not have reaction shape, instead they have
 * stoichiometry box when its value is more than 1.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ConnectorRenderer {

	public void draw(DiagramCanvas canvas, Connector connector, DiagramProfile diagramProfile, DiagramIndex index) {
		final DiagramProfileNode profile = diagramProfile.getReaction();
		final boolean isHalo = index.getHaloed().contains(connector.getEdgeId());
		final boolean selected = index.getSelected().contains(connector.getEdgeId());
		final boolean fadeout = connector.getIsFadeOut() != null && connector.getIsFadeOut();
		final boolean disease = connector.getIsDisease() != null && connector.getIsDisease();
		final Stroke haloStroke = StrokeProperties.HALO_STROKE;
		final Stroke lineStroke = selected
				? StrokeProperties.SELECTION_STROKE
				: StrokeProperties.SEGMENT_STROKE;
		final String haloColor = diagramProfile.getProperties().getHalo();
		final String lineColor = selected
				? diagramProfile.getProperties().getSelection()
				: disease
				? diagramProfile.getProperties().getDisease()
				: fadeout
				? profile.getFadeOutStroke()
				: profile.getStroke();
		final String fillColor = fadeout
				? profile.getFadeOutFill()
				: profile.getFill();

		final FillLayer fillLayer;
		final LineLayer borderLayer;
		final TextLayer textLayer;
		final LineLayer segmentsLayer;
		if (fadeout) {
			segmentsLayer = canvas.getFadeOutSegments();
			fillLayer = canvas.getFadeoutEdgeFill();
			borderLayer = canvas.getFadeoutEdgeBorder();
			textLayer = canvas.getFadeOutText();
		} else {
			segmentsLayer = canvas.getSegments();
			fillLayer = canvas.getEdgeFill();
			borderLayer = canvas.getEdgeBorder();
			textLayer = canvas.getText();
		}
		// 1 segments
		final List<java.awt.Shape> segments = connector.getSegments().stream()
				.map(segment -> ShapeFactory.line(segment.getFrom(), segment.getTo()))
				.collect(Collectors.toList());
		if (isHalo)
			segments.forEach(shape -> canvas.getHalos().add(haloColor, haloStroke, shape));
		segments.forEach(shape -> segmentsLayer.add(lineColor, lineStroke, shape));

		// 2 shapes
		final Shape shape = connector.getEndShape();
		if (shape != null) {
			final List<java.awt.Shape> javaShapes = ShapeFactory.createShape(shape);
			// 2.1 halo
			if (isHalo)
				javaShapes.forEach(sh -> canvas.getFlags().add(haloColor, haloStroke, sh));
			// 2.2 fill
			final String color = isEmpty(shape) ? fillColor : lineColor;
			javaShapes.forEach(sh -> fillLayer.add(color, sh));
			// 2.3 border
			javaShapes.forEach(sh -> borderLayer.add(lineColor, lineStroke, sh));
			// 2.4 text
			if (shape.getS() != null && !shape.getS().isEmpty()) {
				final NodeProperties limits = NodePropertiesFactory.get(
						shape.getA().getX(), shape.getA().getY(),
						shape.getB().getX() - shape.getA().getX(),
						shape.getB().getY() - shape.getA().getY());
				textLayer.add(lineColor, shape.getS(), limits, 1);
			}
		}
		if (connector.getStoichiometry() != null && connector.getStoichiometry().getShape() != null) {
			final Shape stShape = connector.getStoichiometry().getShape();
			final List<java.awt.Shape> shapes = ShapeFactory.createShape(stShape);
			final boolean empty = isEmpty(stShape);
			shapes.forEach(sh -> {
				fillLayer.add(empty ? fillColor : lineColor, sh);
				borderLayer.add(lineColor, StrokeProperties.SEGMENT_STROKE, sh);
			});
			final String text = connector.getStoichiometry().getValue().toString();
			final NodeProperties limits =
					NodePropertiesFactory.get(
							stShape.getA().getX(), stShape.getA().getY(),
							stShape.getB().getX() - stShape.getA().getX(),
							stShape.getB().getY() - stShape.getA().getY());
			textLayer.add(lineColor, text, limits, 1);
		}
	}


	private void stoichiometry(Connector connector, String color, String fill, FillLayer fillLayer, TextLayer textLayer, LineLayer lineLayer) {
		if (connector.getStoichiometry() != null && connector.getStoichiometry().getShape() != null) {
			final Shape stShape = connector.getStoichiometry().getShape();
			final List<java.awt.Shape> shapes = ShapeFactory.createShape(stShape);
			final boolean empty = isEmpty(stShape);
			shapes.forEach(sh -> {
				fillLayer.add(empty ? fill : color, sh);
				lineLayer.add(color, StrokeProperties.SEGMENT_STROKE, sh);
			});
			final String text = connector.getStoichiometry().getValue().toString();
			final NodeProperties limits = NodePropertiesFactory.get(
					stShape.getA().getX(), stShape.getA().getY(),
					stShape.getB().getX() - stShape.getA().getX(),
					stShape.getB().getY() - stShape.getA().getY());
			textLayer.add(color, text, limits, 1);
		}
	}

	private boolean isEmpty(Shape shape) {
		return shape.getEmpty() != null && shape.getEmpty();
	}
}
