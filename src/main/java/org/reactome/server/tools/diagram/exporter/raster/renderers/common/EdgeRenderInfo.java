package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.EdgeCommon;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfileNode;
import org.reactome.server.tools.diagram.exporter.raster.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.DrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.FillDrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.TextLayer;

import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;

public class EdgeRenderInfo extends DiagramObjectInfo {

	private final DiagramProfileNode profile;

	private final boolean fadeout;
	private final boolean disease;
	private final boolean dashed;

	private final DrawLayer segmentsLayer;
	private final FillDrawLayer shapeLayer;
	private final TextLayer textLayer;

	private final Stroke haloStroke;
	private final Stroke lineStroke;
	private final Stroke segmentStroke;

	private final String fillColor;
	private final String lineColor;
	private final String haloColor;
	private final LinkedList<Shape> segments;
	private final DiagramIndex.EdgeDecorator decorator;

	public EdgeRenderInfo(EdgeCommon edge, boolean dashed, DiagramProfile diagramProfile, DiagramIndex index, DiagramCanvas canvas) {
		this.decorator = index.getEdgeDecorator(edge.getId());
		profile = getDiagramProfileNode(edge.getRenderableClass(), diagramProfile);

		fadeout = edge.getIsFadeOut() != null && edge.getIsFadeOut();
		disease = edge.getIsDisease() != null && edge.getIsDisease();
		this.dashed = dashed;

		haloStroke = StrokeProperties.StrokeStyle.HALO.getStroke(false);
		if (decorator.isSelected()) {
			lineStroke = StrokeProperties.StrokeStyle.SELECTION.getStroke(false);
			segmentStroke = StrokeProperties.StrokeStyle.SELECTION.getStroke(dashed);
		} else {
			lineStroke = StrokeProperties.StrokeStyle.SEGMENT.getStroke(false);
			segmentStroke = StrokeProperties.StrokeStyle.SEGMENT.getStroke(dashed);
		}

		haloColor = diagramProfile.getProperties().getHalo();

		if (fadeout) {
			segmentsLayer = canvas.getFadeOutSegments();
			shapeLayer = canvas.getFadeOutEdgeShapes();
			textLayer = canvas.getFadeOutText();
			fillColor = profile.getFadeOutFill();
			lineColor = profile.getFadeOutStroke();
		} else {
			segmentsLayer = canvas.getSegments();
			shapeLayer = canvas.getEdgeShapes();
			textLayer = canvas.getText();
			fillColor = profile.getFill();
			lineColor = decorator.isSelected()
					? diagramProfile.getProperties().getSelection()
					: disease
					? diagramProfile.getProperties().getDisease()
					: profile.getStroke();
		}
		segments = new LinkedList<>();
		edge.getSegments().stream()
				.map(segment -> ShapeFactory.line(segment.getFrom(), segment.getTo()))
				.forEach(segments::add);
		decorator.getConnectors().stream()
				.map(Connector::getSegments)
				.flatMap(Collection::stream)
				.map(segment -> ShapeFactory.line(segment.getFrom(), segment.getTo()))
				.forEach(segments::add);

	}

	public DiagramProfileNode getProfile() {
		return profile;
	}

	public DrawLayer getSegmentsLayer() {
		return segmentsLayer;
	}

	public FillDrawLayer getShapeLayer() {
		return shapeLayer;
	}

	public String getFillColor() {
		return fillColor;
	}

	public String getHaloColor() {
		return haloColor;
	}

	public String getLineColor() {
		return lineColor;
	}

	public Stroke getHaloStroke() {
		return haloStroke;
	}

	public Stroke getLineStroke() {
		return lineStroke;
	}

	public Stroke getSegmentStroke() {
		return segmentStroke;
	}

	public TextLayer getTextLayer() {
		return textLayer;
	}

	public LinkedList<Shape> getSegments() {
		return segments;
	}

	public DiagramIndex.EdgeDecorator getDecorator() {
		return decorator;
	}
}
