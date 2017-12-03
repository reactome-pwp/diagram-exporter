package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableGene;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableNode;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.StrokeStyle;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers.TextLayer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Renderer for genes. These ones are a little bit more complex than the rest.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class GeneRenderer extends NodeAbstractRenderer {
	@Override
	public void background(RenderableNode renderableNode, DiagramCanvas canvas, DiagramIndex index, ColorProfiles colorProfiles) {
		RenderableGene renderableGene = (RenderableGene) renderableNode;
		final Color fill = getFillColor(renderableNode, colorProfiles, index);
		final Color border = getStrokeColor(renderableNode, colorProfiles, index);
		if (renderableNode.isFadeOut()) {
			canvas.getFadeOutNodeBackground().add(renderableNode.getBackgroundArea(), fill);
			canvas.getFadeOutNodeBackground().add(renderableGene.getArrow(), fill);
			canvas.getFadeOutNodeBorder().add(renderableGene.getLine(), border, StrokeStyle.BORDER.get(renderableGene.isDashed()));
			canvas.getFadeOutNodeBorder().add(renderableGene.getArrow(), border, StrokeStyle.BORDER.get(renderableGene.isDashed()));
		} else {
			canvas.getNodeForeground().add(renderableNode.getBackgroundArea(), fill);
			canvas.getNodeForeground().add(renderableGene.getArrow(), fill);
			canvas.getNodeBorder().add(renderableGene.getLine(), border, StrokeStyle.BORDER.get(renderableGene.isDashed()));
			canvas.getNodeBorder().add(renderableGene.getArrow(), border, StrokeStyle.BORDER.get(renderableGene.isDashed()));
		}
	}

	@Override
	public void halo(RenderableNode renderableNode, DiagramCanvas canvas, ColorProfiles colorProfiles) {
		RenderableGene renderableGene = (RenderableGene) renderableNode;
		canvas.getHalo().add(renderableGene.getArrow(),
				colorProfiles.getDiagramSheet().getProperties().getHalo(),
				StrokeStyle.HALO.get(renderableGene.isDashed()));
		canvas.getHalo().add(renderableGene.getLine(),
				colorProfiles.getDiagramSheet().getProperties().getHalo(),
				StrokeStyle.HALO.get(renderableGene.isDashed()));
	}

	@Override
	public void flag(RenderableNode renderableNode, DiagramCanvas canvas, ColorProfiles colorProfiles) {
		RenderableGene renderableGene = (RenderableGene) renderableNode;
		canvas.getFlags().add(renderableGene.getArrow(),
				colorProfiles.getDiagramSheet().getProperties().getHalo(),
				StrokeStyle.FLAG.get(renderableGene.isDashed()));
		canvas.getFlags().add(renderableGene.getLine(),
				colorProfiles.getDiagramSheet().getProperties().getHalo(),
				StrokeStyle.FLAG.get(renderableGene.isDashed()));
	}

	@Override
	public void text(RenderableNode renderableNode, DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, double textSplit) {
		final TextLayer textLayer = renderableNode.isFadeOut()
				? canvas.getFadeOutText()
				: canvas.getText();
		Color textColor = getTextColor(renderableNode, colorProfiles, index);
		final Rectangle2D bounds = renderableNode.getBackgroundShape().getBounds2D();
		if (bounds.getHeight() > FontProperties.DEFAULT_FONT.getSize()) {
			final NodeProperties limits = NodePropertiesFactory.get(
					bounds.getX(), bounds.getY(), bounds.getWidth(),
					bounds.getHeight());
			textLayer.add(renderableNode.getNode().getDisplayName(),
					textColor, limits, RendererProperties.NODE_TEXT_PADDING,
					textSplit, FontProperties.DEFAULT_FONT);
		} else
			textLayer.add(renderableNode.getNode().getDisplayName(), textColor,
					renderableNode.getNode().getProp(), RendererProperties.NODE_TEXT_PADDING,
					textSplit, FontProperties.DEFAULT_FONT);

	}

}
