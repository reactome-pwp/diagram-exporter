package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.StrokeStyle;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.FillDrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.TextLayer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.awt.*;

public class RenderableChemicalDrug extends RenderableChemical {

	RenderableChemicalDrug(Node node) {
		super(node);
	}

	@Override
	public void draw(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		super.draw(canvas, colorProfiles, index, t);
		chemicalBox(canvas, colorProfiles, index, t);
	}

	private void chemicalBox(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		final Color fill = getFillColor(colorProfiles, index, t);
		final Color border = getAttachmentStrokeColor(colorProfiles, index.getAnalysis().getType());
		final Color text = getTextColor(colorProfiles, index.getAnalysis().getType());
		final Stroke stroke = StrokeStyle.SEGMENT.get(isDashed());
		final FillDrawLayer fillDrawLayer = isFadeOut()
				? canvas.getFadeOutAttachments()
				: canvas.getAttachments();
		final TextLayer textLayer = isFadeOut()
				? canvas.getFadeOutText()
				: canvas.getText();
		final NodeProperties prop = getNode().getProp();
		final double x = prop.getX() + prop.getWidth() - 14;
		final double y = prop.getY() + prop.getHeight() - 7;
		final NodeProperties attachment = NodePropertiesFactory.get(x, y, 14, 7);
		final Shape shape = ShapeFactory.rectangle(attachment);
		fillDrawLayer.add(shape, fill, border, stroke);
		textLayer.add("Rx", text, attachment, 1, 0, FontProperties.DEFAULT_FONT);
		if (isFlag())
			canvas.getFlags().add(shape, colorProfiles.getDiagramSheet().getProperties().getFlag(), StrokeStyle.FLAG.get(isDashed()));
		if (isHalo())
			canvas.getHalo().add(shape, colorProfiles.getDiagramSheet().getProperties().getHalo(), StrokeStyle.HALO.get(isDashed()));
		if (isSelected())
			canvas.getAttachmentSelection().add(shape, colorProfiles.getDiagramSheet().getProperties().getSelection(), StrokeStyle.SELECTION.get(isDashed()));
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

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getChemicalDrug();
	}

	private Color getAttachmentStrokeColor(ColorProfiles colorProfiles, AnalysisType type) {
		// disease -> fadeout -> analysis -> normal
		if (isDisease())
			return colorProfiles.getDiagramSheet().getProperties().getDisease();
		if (isFadeOut())
			return getColorProfile(colorProfiles).getFadeOutStroke();
		if (type != null)
			return getColorProfile(colorProfiles).getLighterStroke();
		return getColorProfile(colorProfiles).getStroke();
	}

}
