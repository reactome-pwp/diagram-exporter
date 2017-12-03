package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableNode;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.StrokeStyle;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers.FillDrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers.TextLayer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;

/**
 * Proteins use a rounded rectangle.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ProteinRenderer extends NodeAbstractRenderer {

	@Override
	public void draw(RenderableNode renderableNode, DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		super.draw(renderableNode, canvas, colorProfiles, index, t);
		attachments(renderableNode, canvas, colorProfiles, index, t);
	}

	private void attachments(RenderableNode renderableNode, DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		if (renderableNode.getNode().getNodeAttachments() == null
				|| renderableNode.getNode().getNodeAttachments().isEmpty())
			return;
		final Color fill = getFillColor(renderableNode, colorProfiles, index, t);
		final Color border = getStrokeColor(renderableNode, colorProfiles, index);
		final Color text = getTextColor(renderableNode, colorProfiles, index);
		final Stroke stroke = StrokeStyle.BORDER.get(renderableNode.isDashed());
		final FillDrawLayer fillDrawLayer = renderableNode.isFadeOut()
				? canvas.getFadeOutAttachments()
				: canvas.getAttachments();
		final TextLayer textLayer = renderableNode.isFadeOut()
				? canvas.getFadeOutText()
				: canvas.getText();
		renderableNode.getNode().getNodeAttachments().forEach(nodeAttachment -> {
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

	private Color getFillColor(RenderableNode renderableNode, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		if (renderableNode.isFadeOut())
			return renderableNode.getColorProfile(colorProfiles).getFadeOutFill();

		if (index.getAnalysis().getType() == AnalysisType.NONE)
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
}
