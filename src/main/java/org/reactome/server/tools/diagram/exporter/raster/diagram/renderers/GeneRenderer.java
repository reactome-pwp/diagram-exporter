package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers;

import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.StrokeStyle;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.TextLayer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableGene;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableNode;
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
		final RenderableGene renderableGene = (RenderableGene) renderableNode;
		final Color fill = getFillColor(renderableNode, colorProfiles, index);
		final Color border = getStrokeColor(renderableNode, colorProfiles, index);
		// report: genes are not dashed in PathwayBrowser, although json file says needDashedBorder
		final Stroke stroke = StrokeStyle.BORDER.get(false);
		if (renderableNode.isFadeOut()) {
			canvas.getFadeOutNodeBackground().add(renderableNode.getBackgroundArea(), fill);
			canvas.getFadeOutNodeBackground().add(renderableGene.getArrow(), fill);
			canvas.getFadeOutNodeBorder().add(renderableGene.getLines(), border, stroke);
			canvas.getFadeOutNodeBorder().add(renderableGene.getArrow(), border, stroke);
		} else {
			canvas.getNodeBackground().add(renderableNode.getBackgroundArea(), fill);
			canvas.getNodeBackground().add(renderableGene.getArrow(), fill);
			canvas.getNodeBorder().add(renderableGene.getLines(), border, stroke);
			canvas.getNodeBorder().add(renderableGene.getArrow(), border, stroke);
		}
	}

	@Override
	public void halo(RenderableNode renderableNode, DiagramCanvas canvas, ColorProfiles colorProfiles) {
		final RenderableGene renderableGene = (RenderableGene) renderableNode;
		final Color color = colorProfiles.getDiagramSheet().getProperties().getHalo();
		final Stroke stroke = StrokeStyle.HALO.get(false);
		canvas.getHalo().add(renderableGene.getArrow(), color, stroke);
		canvas.getHalo().add(renderableGene.getLines(), color, stroke);
	}

	@Override
	public void flag(RenderableNode renderableNode, DiagramCanvas canvas, ColorProfiles colorProfiles) {
		final RenderableGene renderableGene = (RenderableGene) renderableNode;
		final Color color = colorProfiles.getDiagramSheet().getProperties().getHalo();
		final Stroke stroke = StrokeStyle.FLAG.get(false);
		canvas.getFlags().add(renderableGene.getArrow(), color, stroke);
		canvas.getFlags().add(renderableGene.getLines(), color, stroke);
	}

	@Override
	public void text(RenderableNode renderableNode, DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, double textSplit) {
		final TextLayer textLayer = renderableNode.isFadeOut()
				? canvas.getFadeOutText()
				: canvas.getText();
		Color color = getTextColor(renderableNode, colorProfiles, index);
		final Rectangle2D bounds = renderableNode.getBackgroundShape().getBounds2D();
		// report: some diagrams have too small gene shape. It seems to be a problem with the json file
		// in these cases we use node properties
		// when problem is solved, the if/else can be deleted
		if (bounds.getHeight() > FontProperties.DEFAULT_FONT.getSize()) {
			final NodeProperties limits = NodePropertiesFactory.get(
					bounds.getX(), bounds.getY(), bounds.getWidth(),
					bounds.getHeight());
			textLayer.add(renderableNode.getNode().getDisplayName(),
					color, limits, NodeAbstractRenderer.NODE_TEXT_PADDING,
					textSplit, FontProperties.DEFAULT_FONT);
		} else textLayer.add(renderableNode.getNode().getDisplayName(), color,
				renderableNode.getNode().getProp(),
				NodeAbstractRenderer.NODE_TEXT_PADDING,
				textSplit, FontProperties.DEFAULT_FONT);

	}

}
