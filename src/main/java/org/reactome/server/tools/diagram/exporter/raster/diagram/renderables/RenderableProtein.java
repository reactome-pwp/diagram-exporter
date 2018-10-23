package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.*;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.FillDrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.TextLayer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.awt.*;

public class RenderableProtein extends RenderableNode {

	RenderableProtein(Node node) {
		super(node);
	}

	@Override
	Shape backgroundShape() {
		return ShapeFactory.roundedRectangle(getNode().getProp());
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getProtein();
	}

	@Override
	public void draw(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramData data, int t) {
		super.draw(canvas, colorProfiles, data, t);
		attachments(canvas, colorProfiles, data, t);
	}

	private void attachments(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		if (getNode().getNodeAttachments() == null
				|| getNode().getNodeAttachments().isEmpty())
			return;
		final Color fill = getFillColor(colorProfiles, index, t);
		final Color border = getStrokeColor(colorProfiles, index.getAnalysis().getType());
		final Color text = getTextColor(colorProfiles, index.getAnalysis().getType());
		final Stroke stroke = StrokeStyle.BORDER.get(isDashed());
		final FillDrawLayer fillDrawLayer = isFadeOut()
				? canvas.getFadeOutAttachments()
				: canvas.getAttachments();
		final TextLayer textLayer = isFadeOut()
				? canvas.getFadeOutText()
				: canvas.getText();
		getNode().getNodeAttachments().forEach(nodeAttachment -> {
			final org.reactome.server.tools.diagram.data.layout.Shape shape = nodeAttachment.getShape();
			final Shape awtShape = ShapeFactory.getShape(shape);
			fillDrawLayer.add(awtShape, fill, border, stroke);
			if (shape.getS() != null && !shape.getS().isEmpty()) {
				final NodeProperties limits = NodePropertiesFactory.get(
						shape.getA().getX(), shape.getA().getY(),
						shape.getB().getX() - shape.getA().getX(),
						shape.getB().getY() - shape.getA().getY());
				textLayer.add(shape.getS(), text, limits, 1, 0, FontProperties.DEFAULT_FONT);
			}
		});
	}

	private Color getFillColor(ColorProfiles colorProfiles, DiagramIndex index, int t) {
		if (isFadeOut())
			return getColorProfile(colorProfiles).getFadeOutFill();

		if (index.getAnalysis().getType() == null)
			return getColorProfile(colorProfiles).getFill();

		// enrichment
		if (index.getAnalysis().getType() == AnalysisType.SPECIES_COMPARISON
				|| index.getAnalysis().getType() == AnalysisType.OVERREPRESENTATION) {
			if (getEnrichment() != null && getEnrichment() > 0)
				return colorProfiles.getAnalysisSheet().getEnrichment().getGradient().getMax();
			else
				return getColorProfile(colorProfiles).getLighterFill();
		}
		// expression
		if (getHitExpressions() == null
				|| getHitExpressions().isEmpty()) {
			return getColorProfile(colorProfiles).getLighterFill();
		} else {
			final double exp = getHitExpressions().get(0).getExp().get(t);
			final double min = index.getAnalysis().getResult().getExpression().getMin();
			final double max = index.getAnalysis().getResult().getExpression().getMax();
			final double value = 1 - (exp - min) / (max - min);
			return ColorFactory.interpolate(colorProfiles.getAnalysisSheet().getExpression().getGradient(), value);
		}

	}
}
