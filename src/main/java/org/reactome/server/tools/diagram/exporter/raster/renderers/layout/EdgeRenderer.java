package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.EdgeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfileNode;
import org.reactome.server.tools.diagram.exporter.raster.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.StrokeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.FillLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.LineLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.TextLayer;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class EdgeRenderer extends AbstractRenderer {

	public void draw(DiagramCanvas canvas, EdgeCommon edge, DiagramProfile diagramProfile, double factor, DiagramIndex index) {
		final DiagramProfileNode profile = getDiagramProfileNode(edge.getRenderableClass(), diagramProfile);
		final boolean isHalo = index.getHaloed().contains(edge.getId());
		final boolean selected = index.getSelected().contains(edge.getId());
		final boolean fadeout = edge.getIsFadeOut() != null && edge.getIsFadeOut();
		final boolean disease = edge.getIsDisease() != null && edge.getIsDisease();
		final boolean dashed = dashed(edge);
		final Stroke haloStroke = StrokeProperties.HALO_STROKE;
		final Stroke lineStroke = selected
				? StrokeProperties.SELECTION_STROKE
				: StrokeProperties.SEGMENT_STROKE;
		final Stroke segmentStroke = dashed
				? selected
				? StrokeProperties.DASHED_SELECTION_STROKE
				: StrokeProperties.DASHED_SEGMENT_STROKE
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
		final String textColor = lineColor;

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
		final List<java.awt.Shape> segments = edge.getSegments().stream()
				.map(segment -> ShapeFactory.line(factor, segment.getFrom(), segment.getTo()))
				.collect(Collectors.toList());
		if (isHalo)
			segments.forEach(shape -> canvas.getHalos().add(haloColor, haloStroke, shape));
		segments.forEach(shape -> segmentsLayer.add(lineColor, segmentStroke, shape));

		// 2 shapes
		renderableShapes(edge).stream().filter(Objects::nonNull).forEach(shape -> {
			final List<java.awt.Shape> javaShapes = ShapeFactory.createShape(shape, factor);
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
				final NodeProperties limits = new ScaledNodeProperties(
						NodePropertiesFactory.get(
								shape.getA().getX(), shape.getA().getY(),
								shape.getB().getX() - shape.getA().getX(),
								shape.getB().getY() - shape.getA().getY()),
						factor);
				textLayer.add(textColor, shape.getS(), limits, factor);
			}
		});
	}

	private boolean isEmpty(org.reactome.server.tools.diagram.data.layout.Shape shape) {
		return shape.getEmpty() != null && shape.getEmpty();
	}

	protected List<Shape> renderableShapes(EdgeCommon edge) {
		return Arrays.asList(edge.getReactionShape(), edge.getEndShape());
	}

	protected boolean dashed(EdgeCommon edge) {
		return false;
	}
}
