package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers;

import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.StrokeStyle;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.FillDrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.TextLayer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableNode;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableObject;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ChemicalDrugRenderer extends ChemicalRenderer {

	@Override
	public void draw(RenderableNode renderableNode, DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		super.draw(renderableNode, canvas, colorProfiles, index, t);
		chemicalBox(renderableNode, canvas, colorProfiles, index, t);
	}

	private void chemicalBox(RenderableNode renderableNode, DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		final Color fill = getFillColor(renderableNode, colorProfiles, index, t);
		final Color border = getAttachmentStrokeColor(renderableNode, colorProfiles, index);
		final Color text = Color.RED;
		final Stroke stroke = StrokeStyle.SEGMENT.get(renderableNode.isDashed());
		final FillDrawLayer fillDrawLayer = renderableNode.isFadeOut()
				? canvas.getFadeOutAttachments()
				: canvas.getAttachments();
		final TextLayer textLayer = renderableNode.isFadeOut()
				? canvas.getFadeOutText()
				: canvas.getText();
		final NodeProperties prop = renderableNode.getNode().getProp();
		final double x = prop.getX() + prop.getWidth() - 10;
		final double y = prop.getY() + prop.getHeight() - 10;
		final NodeProperties attachment = NodePropertiesFactory.get(x, y, 10, 10);
		final Shape shape = ShapeFactory.rectangle(attachment);
		fillDrawLayer.add(shape, fill, border, stroke);
		textLayer.add("Rx", text, attachment, 1, 0, FontProperties.DEFAULT_FONT);
		if (renderableNode.isFlag())
			canvas.getFlags().add(shape, colorProfiles.getDiagramSheet().getProperties().getFlag(), StrokeStyle.FLAG.get(renderableNode.isDashed()));
		if (renderableNode.isHalo())
			canvas.getHalo().add(shape, colorProfiles.getDiagramSheet().getProperties().getHalo(), StrokeStyle.HALO.get(renderableNode.isDashed()));
		if (renderableNode.isSelected())
			canvas.getAttachmentSelection().add(shape, colorProfiles.getDiagramSheet().getProperties().getSelection(), StrokeStyle.SELECTION.get(renderableNode.isDashed()));
	}

	private Color getFillColor(RenderableNode renderableNode, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		if (renderableNode.isFadeOut())
			return renderableNode.getColorProfile(colorProfiles).getFadeOutFill();

		if (index.getAnalysis().getType() == null)
			return renderableNode.getColorProfile(colorProfiles).getFill();

		// enrichment
		if (index.getAnalysis().getType() == AnalysisType.SPECIES_COMPARISON
				|| index.getAnalysis().getType() == AnalysisType.OVERREPRESENTATION) {
			if (renderableNode.getEnrichment() != null && renderableNode.getEnrichment() > 0)
				return colorProfiles.getAnalysisSheet().getEnrichment().getGradient().getMax();
			else
				return renderableNode.getColorProfile(colorProfiles).getLighterFill();
		}
		// expression
		if (renderableNode.getHitExpressions() == null
				|| renderableNode.getHitExpressions().isEmpty()) {
			return renderableNode.getColorProfile(colorProfiles).getLighterFill();
		} else {
			final double exp = renderableNode.getHitExpressions().get(0).getExp().get(t);
			final double min = index.getAnalysis().getResult().getExpression().getMin();
			final double max = index.getAnalysis().getResult().getExpression().getMax();
			final double value = 1 - (exp - min) / (max - min);
			return ColorFactory.interpolate(colorProfiles.getAnalysisSheet().getExpression().getGradient(), value);
		}

	}

	protected Color getAttachmentStrokeColor(RenderableObject renderableNode, ColorProfiles colorProfiles, DiagramIndex index) {
		// disease -> fadeout -> analysis -> normal
		if (renderableNode.isDisease())
			return colorProfiles.getDiagramSheet().getProperties().getDisease();
		if (renderableNode.isFadeOut())
			return renderableNode.getColorProfile(colorProfiles).getFadeOutStroke();
		if (index.getAnalysis().getType() != null)
			return renderableNode.getColorProfile(colorProfiles).getLighterStroke();
		return renderableNode.getColorProfile(colorProfiles).getStroke();
	}



}
