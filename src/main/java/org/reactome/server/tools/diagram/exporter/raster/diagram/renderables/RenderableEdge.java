package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.Edge;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.Stoichiometry;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.StrokeStyle;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.FillDrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.TextLayer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;
import java.util.Objects;

public abstract class RenderableEdge extends RenderableEdgeCommon<Edge> {

	RenderableEdge(Edge edge) {
		super(edge);
	}

	@Override
	public void draw(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		final Color linesColor = getStrokeColor(colorProfiles, index.getAnalysis().getType());
		final Color fillColor = getFillColor(colorProfiles, index.getAnalysis().getType());
		segments(linesColor, canvas, colorProfiles);
		shapes(linesColor, fillColor, canvas, colorProfiles);
		// stoichiometry text is not in stoichiometry.getShape().getS()
		// but in stoichiometry.getValue()
		stoichiometryText(linesColor, canvas);
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
					? fillColor
					: linesColor;
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

	private void flag( DiagramCanvas canvas, ColorProfiles colorProfiles, java.awt.Shape awtShape) {
		canvas.getFlags().add(awtShape,
				colorProfiles.getDiagramSheet().getProperties().getFlag(),
				StrokeStyle.FLAG.get(isDashed()));
	}

	private void halo(DiagramCanvas canvas, ColorProfiles colorProfiles, java.awt.Shape awtShape) {
		canvas.getHalo().add(awtShape,
				colorProfiles.getDiagramSheet().getProperties().getHalo(),
				StrokeStyle.HALO.get(isDashed()));
	}

	private void stoichiometryText(Color linesColor, DiagramCanvas canvas) {
		getConnectors().stream()
				.map(Connector::getStoichiometry)
				.filter(Objects::nonNull)
				.filter(stoichiometry -> stoichiometry.getShape() != null)
				.forEach(stoichiometry -> stoichiometryText(stoichiometry, linesColor, canvas));
	}

	private void stoichiometryText(Stoichiometry stoichiometry, Color linesColor, DiagramCanvas canvas) {
		final org.reactome.server.tools.diagram.data.layout.Shape stShape = stoichiometry.getShape();
		final String text = stoichiometry.getValue().toString();
		final NodeProperties limits =
				NodePropertiesFactory.get(
						stShape.getA().getX(), stShape.getA().getY(),
						stShape.getB().getX() - stShape.getA().getX(),
						stShape.getB().getY() - stShape.getA().getY());
		canvas.getText().add(text, linesColor, limits, 1, 0, FontProperties.DEFAULT_FONT);
	}
}
