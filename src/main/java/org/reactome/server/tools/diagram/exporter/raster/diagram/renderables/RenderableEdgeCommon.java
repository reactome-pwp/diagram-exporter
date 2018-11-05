package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramData;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.StrokeStyle;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.FillDrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.TextLayer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.Color;
import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains extra rendering information for an edge: decorators plus
 * connectors.
 */
public abstract class RenderableEdgeCommon<T extends EdgeCommon> extends RenderableDiagramObject<T> {

	private List<Connector> connectors = new LinkedList<>();
	// Lazy loading, as connectors are populated after creation
	private List<Shape> shapes;
	private Collection<java.awt.Shape> segments;

	RenderableEdgeCommon(T edge) {
		super(edge);
	}

	public List<Connector> getConnectors() {
		return connectors;
	}

	public T getEdge() {
		return getDiagramObject();
	}

	public boolean isDashed() {
		return false;
	}

	@Override
	public void draw(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramData data, int t) {
		final Color linesColor = getStrokeColor(colorProfiles, data.getAnalysis().getType());
		final Color fillColor = getFillColor(colorProfiles, data.getAnalysis().getType());
		segments(linesColor, canvas, colorProfiles);
		shapes(linesColor, fillColor, canvas, colorProfiles);
	}

	@Override
	protected Color getFillColor(ColorProfiles colorProfiles, AnalysisType type) {
		//fadeout -> analysis -> normal
		if (isFadeOut())
			return getColorProfile(colorProfiles).getFadeOutFill();
		if (type != null) {
			// report: lighterfill of reaction is grey (modern profile) as text
			return getColorProfile(colorProfiles).getFill();
		}
		return getColorProfile(colorProfiles).getFill();
	}

	private void segments(Color linesColor, DiagramCanvas canvas, ColorProfiles colorProfiles) {
		if (isHalo())
			getSegments().forEach(shape -> canvas.getHalo().add(shape,
					colorProfiles.getDiagramSheet().getProperties().getHalo(),
					StrokeStyle.HALO.get(isDashed())
			));
		final DrawLayer layer = isFadeOut()
				? canvas.getFadeOutSegments()
				: canvas.getSegments();
		final Stroke stroke = isSelected()
				? StrokeStyle.SELECTION.get(isDashed())
				: StrokeStyle.SEGMENT.get(isDashed());
		getSegments().forEach(shape -> layer.add(shape, linesColor, stroke));
	}

	private void shapes(Color linesColor, Color fillColor, DiagramCanvas canvas, ColorProfiles colorProfiles) {
		final FillDrawLayer layer = isFadeOut()
				? canvas.getFadeOutEdgeShapes()
				: canvas.getEdgeShapes();
		final TextLayer textLayer = isFadeOut()
				? canvas.getFadeOutText()
				: canvas.getText();
		final Stroke stroke = StrokeStyle.SEGMENT.get(isDashed());
		getShapes().forEach(shape -> {
			final java.awt.Shape awtShape = ShapeFactory.getShape(shape);
			if (isFlag()) flag(canvas, colorProfiles, awtShape);
			if (isHalo()) halo(canvas, colorProfiles, awtShape);
			final Color color = shape.getEmpty() != null && shape.getEmpty()
					? Color.WHITE
					: fillColor;
			layer.add(awtShape, color, linesColor, stroke);
			if (shape.getType().equals("DOUBLE_CIRCLE"))
				layer.add(ShapeFactory.innerCircle(shape), color, linesColor, stroke);
			if (shape.getS() != null && !shape.getS().isEmpty()) {
				final NodeProperties limits = NodePropertiesFactory.get(
						shape.getA().getX(), shape.getA().getY(),
						shape.getB().getX() - shape.getA().getX(),
						shape.getB().getY() - shape.getA().getY());
				textLayer.add(shape.getS(), linesColor, limits, 1, 0, FontProperties.DEFAULT_FONT);
			}
		});
	}

	private void flag(DiagramCanvas canvas, ColorProfiles colorProfiles, java.awt.Shape awtShape) {
		canvas.getFlags().add(awtShape,
				colorProfiles.getDiagramSheet().getProperties().getFlag(),
				StrokeStyle.FLAG.get(isDashed()));
	}

	private void halo(DiagramCanvas canvas, ColorProfiles colorProfiles, java.awt.Shape awtShape) {
		canvas.getHalo().add(awtShape,
				colorProfiles.getDiagramSheet().getProperties().getHalo(),
				StrokeStyle.HALO.get(isDashed()));
	}

	public Collection<java.awt.Shape> getSegments() {
		if (segments == null) createSegments();
		return segments;
	}

	private void createSegments() {
		segments = new LinkedList<>();
		for (Segment segment : getEdge().getSegments())
			segments.add(ShapeFactory.line(segment.getFrom(), segment.getTo()));
		for (Connector connector : connectors)
			for (Segment segment : connector.getSegments())
				segments.add(ShapeFactory.line(segment.getFrom(), segment.getTo()));
	}

	public List<Shape> getShapes() {
		if (shapes == null) createShapes();
		return shapes;
	}

	private void createShapes() {
		shapes = new LinkedList<>();
		for (Shape shape : getRenderableShapes())
			if (shape != null) shapes.add(shape);
		for (Connector connector : connectors)
			if (connector.getEndShape() != null)
				shapes.add(connector.getEndShape());
		for (Connector connector : connectors)
			if (connector.getStoichiometry().getShape() != null)
				shapes.add(connector.getStoichiometry().getShape());
	}

	protected List<Shape> getRenderableShapes() {
		return Arrays.asList(getEdge().getReactionShape(), getEdge().getEndShape());
	}
}
