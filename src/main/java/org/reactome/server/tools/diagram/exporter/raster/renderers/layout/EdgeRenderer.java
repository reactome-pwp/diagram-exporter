package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.data.profile.analysis.AnalysisProfile;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfileNode;
import org.reactome.server.tools.diagram.data.profile.interactors.InteractorProfile;
import org.reactome.server.tools.diagram.exporter.raster.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.StrokeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.DrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.FillDrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.TextLayer;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Renders edges, connectors and links.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class EdgeRenderer extends AbstractRenderer {

	/**
	 * Renders an edge and the connectors associated to it.
	 *
	 * @param canvas         where to render
	 * @param item           the edge to render
	 * @param diagramProfile color profile
	 * @param index          the diagram index
	 */
	@Override
	public void draw(DiagramCanvas canvas, DiagramObject item, DiagramProfile diagramProfile, AnalysisProfile analysisProfile, InteractorProfile interactorProfile, DiagramIndex index, AnalysisType analysisType) {
		final EdgeCommon edge = (EdgeCommon) item;
		final DiagramProfileNode profile = getDiagramProfileNode(edge.getRenderableClass(), diagramProfile);
		final List<Connector> connectors = index.getConnectors(edge.getId());

		final boolean isHalo = index.getHaloed().contains(edge.getId());
		final boolean selected = index.getSelected().contains(edge.getId());
		final boolean fadeout = edge.getIsFadeOut() != null && edge.getIsFadeOut();
		final boolean disease = edge.getIsDisease() != null && edge.getIsDisease();
		final boolean dashed = dashed(edge);

		final Stroke haloStroke = StrokeProperties.StrokeStyle.HALO.getStroke(false);
		final Stroke lineStroke;
		final Stroke segmentStroke;
		if (selected) {
			lineStroke = StrokeProperties.StrokeStyle.SELECTION.getStroke(false);
			segmentStroke = StrokeProperties.StrokeStyle.SELECTION.getStroke(dashed);
		} else {
			lineStroke = StrokeProperties.StrokeStyle.SEGMENT.getStroke(false);
			segmentStroke = StrokeProperties.StrokeStyle.SEGMENT.getStroke(dashed);
		}

		final String haloColor = diagramProfile.getProperties().getHalo();
		final String lineColor;
		final String fillColor;

		final TextLayer textLayer;
		final DrawLayer segmentsLayer;
		final FillDrawLayer shapeLayer;

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
			lineColor = selected
					? diagramProfile.getProperties().getSelection()
					: disease
					? diagramProfile.getProperties().getDisease()
					: profile.getStroke();
		}
		// 1 segments
		final List<java.awt.Shape> segments = new LinkedList<>();
		edge.getSegments().stream()
				.map(segment -> ShapeFactory.line(segment.getFrom(), segment.getTo()))
				.forEach(segments::add);
		connectors.stream()
				.map(Connector::getSegments)
				.flatMap(Collection::stream)
				.map(segment -> ShapeFactory.line(segment.getFrom(), segment.getTo()))
				.forEach(segments::add);

		// 2 shapes
		final List<Shape> rShapes = new LinkedList<>();
		renderableShapes(edge).stream()
				.filter(Objects::nonNull)
				.forEach(rShapes::add);
		connectors.stream()
				.map(Connector::getEndShape)
				.filter(Objects::nonNull)
				.forEach(rShapes::add);

		if (isHalo)
			segments.forEach(shape -> canvas.getHalo().add(haloColor, haloStroke, shape));
		segments.forEach(shape -> segmentsLayer.add(lineColor, segmentStroke, shape));

		rShapes.forEach(shape -> {
			final List<java.awt.Shape> javaShapes = ShapeFactory.createShape(shape);
			// 2.1 halo
			if (isHalo)
				javaShapes.forEach(sh -> canvas.getFlags().add(haloColor, haloStroke, sh));
			// 2.2 fill
			final String color = shape.getEmpty() != null && shape.getEmpty() ? fillColor : lineColor;
			javaShapes.forEach(sh -> shapeLayer.add(color, lineColor, lineStroke, sh));
			// 2.4 text
			if (shape.getS() != null && !shape.getS().isEmpty()) {
				final NodeProperties limits = NodePropertiesFactory.get(
						shape.getA().getX(), shape.getA().getY(),
						shape.getB().getX() - shape.getA().getX(),
						shape.getB().getY() - shape.getA().getY());
				textLayer.add(lineColor, shape.getS(), limits, 1);
			}
		});
		// stoichiometries
		connectors.stream()
				.map(Connector::getStoichiometry)
				.filter(Objects::nonNull)
				.filter(stoichiometry -> stoichiometry.getShape() != null)
				.forEach(stoichiometry -> {
					final org.reactome.server.tools.diagram.data.layout.Shape stShape = stoichiometry.getShape();
					final List<java.awt.Shape> shapes = ShapeFactory.createShape(stShape);
					if (isHalo)
						shapes.forEach(sh -> canvas.getHalo().add(haloColor, haloStroke, sh));
					final String fill = stShape.getEmpty() != null && stShape.getEmpty()
							? fillColor : lineColor;
					shapes.forEach(sh -> shapeLayer.add(fill, lineColor, lineStroke, sh));
					final String text = stoichiometry.getValue().toString();
					final NodeProperties limits =
							NodePropertiesFactory.get(
									stShape.getA().getX(), stShape.getA().getY(),
									stShape.getB().getX() - stShape.getA().getX(),
									stShape.getB().getY() - stShape.getA().getY());
					textLayer.add(lineColor, text, limits, 1);

				});
	}

	/**
	 * get a list of shapes that this edge needs to render: reaction and end for
	 * reactions, end for links
	 */
	protected List<Shape> renderableShapes(EdgeCommon edge) {
		return Arrays.asList(edge.getReactionShape(), edge.getEndShape());
	}

	/**
	 * links should override this if they want to be dashed
	 */
	protected boolean dashed(EdgeCommon edge) {
		return false;
	}


}
