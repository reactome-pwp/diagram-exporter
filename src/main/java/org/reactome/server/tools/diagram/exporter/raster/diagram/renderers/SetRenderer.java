package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers;

import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.StrokeStyle;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.TextLayer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableNode;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;

/**
 * Sets add an inner border.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class SetRenderer extends NodeAbstractRenderer {

	private static double SET_PADDING = 4;

	@Override
	public void draw(RenderableNode renderableNode, DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		super.draw(renderableNode, canvas, colorProfiles, index, t);
		final Color border = getStrokeColor(renderableNode, colorProfiles, index);
		final Shape shape = ShapeFactory.roundedRectangle(renderableNode.getNode().getProp(), SET_PADDING);
		final Stroke stroke = StrokeStyle.BORDER.get(renderableNode.isDashed());
		final DrawLayer layer = renderableNode.isFadeOut()
				? canvas.getFadeOutNodeBorder()
				: canvas.getNodeBorder();
		layer.add(shape, border, stroke);
	}

	@Override
	public void text(RenderableNode renderableNode, DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, double textSplit) {
		final TextLayer layer = renderableNode.isFadeOut()
				? canvas.getFadeOutText()
				: canvas.getText();
		final Color color = getTextColor(renderableNode, colorProfiles, index);
		layer.add(renderableNode.getNode().getDisplayName(),
				color,
				renderableNode.getNode().getProp(),
				SET_PADDING,
				textSplit,
				FontProperties.DEFAULT_FONT);
	}
}
