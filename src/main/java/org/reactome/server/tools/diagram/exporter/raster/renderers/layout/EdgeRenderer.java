package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.EdgeCommon;
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
import java.util.Arrays;
import java.util.List;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class EdgeRenderer extends AbstractRenderer {

	protected List<Shape> renderableShapes(EdgeCommon edge) {
		return Arrays.asList(edge.getReactionShape(), edge.getEndShape());
	}

	private boolean isEmpty(org.reactome.server.tools.diagram.data.layout.Shape shape) {
		return shape.getEmpty() != null && shape.getEmpty();
	}

	public void draw(DiagramCanvas canvas, EdgeCommon edge, DiagramProfile diagramProfile, double factor, DiagramIndex index) {
		if (edge.getIsFadeOut() != null && edge.getIsFadeOut())
			asFadeOut(canvas, edge, diagramProfile, factor);
		else asNormal(canvas, edge, diagramProfile, index, factor);
	}

	private void asFadeOut(DiagramCanvas canvas, EdgeCommon edge, DiagramProfile diagramProfile, double factor) {
		final String line = diagramProfile.getReaction().getFadeOutStroke();
		final Stroke segmentStroke = StrokeProperties.SEGMENT_STROKE;
		segments(canvas.getFadeOutSegments(), edge, factor, line, segmentStroke);

		// Shapes and borders
		String fill = diagramProfile.getReaction().getFadeOutFill();
		String border = diagramProfile.getReaction().getFadeOutStroke();
		final Stroke borderStroke = StrokeProperties.SEGMENT_STROKE;
		final FillLayer fillLayer = canvas.getFadeOutFills();
		final LineLayer lineLayer = canvas.getFadeOutBorders();
		final TextLayer textLayer = canvas.getFadeOutText();
		draw(edge, factor, border, borderStroke, fillLayer, textLayer, lineLayer, null, fill, false, null);
	}

	private void asNormal(DiagramCanvas canvas, EdgeCommon edge, DiagramProfile diagramProfile, DiagramIndex index, double factor) {
		final String lineColor = getSegmentColor(edge, diagramProfile, index);
		final String haloColor = diagramProfile.getProperties().getHalo();
		final Stroke stroke = index.getSelected().contains(edge.getId())
				? StrokeProperties.SELECTION_STROKE
				: StrokeProperties.SEGMENT_STROKE;
		final boolean halo = index.getHaloed().contains(edge.getId());
		if (halo)
			segments(canvas.getHalos(), edge, factor, haloColor, StrokeProperties.HALO_STROKE);
		segments(canvas.getSegments(), edge, factor, lineColor, stroke);
		// Shapes and borders
		final FillLayer fillLayer = canvas.getFill();
		final TextLayer textLayer = canvas.getText();
		final LineLayer lineLayer = canvas.getBorder();
		String fill = diagramProfile.getReaction().getFill();
		draw(edge, factor, lineColor, stroke, fillLayer, textLayer, lineLayer, canvas.getHalos(), fill, halo, haloColor);
	}

	private void segments(LineLayer lineLayer, EdgeCommon edge, double factor, String line, Stroke segmentStroke) {
		// Segments
		edge.getSegments().stream()
				.map(segment -> ShapeFactory.line(factor, segment.getFrom(), segment.getTo()))
				.forEach(shape -> lineLayer.add(line, segmentStroke, shape));
	}

	private void draw(EdgeCommon edge, double factor, String lineColor, Stroke stroke, FillLayer fillLayer, TextLayer textLayer, LineLayer lineLayer, LineLayer haloLayer, String fill, boolean halo, String haloColor) {
		for (Shape shape : renderableShapes(edge)) {
			if (shape == null) continue;
			final String color = isEmpty(shape) ? fill : lineColor;
			final List<java.awt.Shape> shapes = ShapeFactory.createShape(shape, factor);
			shapes.forEach(s -> {
				fillLayer.add(color, s);
				lineLayer.add(lineColor, stroke, s);
				if (halo)
					haloLayer.add(haloColor, StrokeProperties.HALO_STROKE, s);
			});
			if (shape.getS() != null && !shape.getS().isEmpty()) {
				final NodeProperties limits = new ScaledNodeProperties(
						NodePropertiesFactory.get(
								shape.getA().getX(), shape.getA().getY(),
								shape.getB().getX() - shape.getA().getX(),
								shape.getB().getY() - shape.getA().getY()),
						factor);
				// Texts
				textLayer.add(lineColor, shape.getS(), limits, factor);
			}
		}
	}

	private String getSegmentColor(EdgeCommon edge, DiagramProfile diagramProfile, DiagramIndex index) {
		if (index.getSelected().contains(edge.getId()))
			return diagramProfile.getProperties().getSelection();
		if (edge.getIsDisease() != null && edge.getIsDisease())
			return diagramProfile.getProperties().getDisease();
		return diagramProfile.getReaction().getStroke();

	}
}
