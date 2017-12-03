package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableNode;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.StrokeStyle;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers.TextLayer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ProcessNodeRenderer extends NodeAbstractRenderer {

	// When it comes to analysis, alpha must be 0.75
	private static final Color ANALYSIS_INNER_COLOR = new Color(254, 253, 255, 191);
	private static final Color INNER_COLOR = new Color(254, 253, 255);
	private static final double PROCESS_NODE_PADDING = 10;

	@Override
	public void background(RenderableNode renderableNode, DiagramCanvas canvas, DiagramIndex index, ColorProfiles colorProfiles) {
		super.background(renderableNode, canvas, index, colorProfiles);
		final Shape rectangle = ShapeFactory.rectangle(renderableNode.getNode().getProp(), PROCESS_NODE_PADDING);
		final Color fill = index.getAnalysis().getType() == AnalysisType.NONE
				? INNER_COLOR
				: ANALYSIS_INNER_COLOR;
		final Color border = getStrokeColor(renderableNode, colorProfiles, index);
		if (renderableNode.isFadeOut()) {
			canvas.getFadeOutNodeForeground().add(rectangle, fill);
			canvas.getFadeOutNodeBorder().add(rectangle, border, StrokeStyle.BORDER.get(renderableNode.isDashed()));
		} else {
			canvas.getNodeForeground().add(rectangle, fill);
			canvas.getNodeBorder().add(rectangle, border, StrokeStyle.BORDER.get(renderableNode.isDashed()));
		}

	}

	@Override
	public double expression(RenderableNode renderableNode, DiagramCanvas canvas, DiagramIndex index, ColorProfiles colorProfiles, int t) {
		final Double percentage = renderableNode.getEnrichment();
		if (percentage != null && percentage > 0) {
			final NodeProperties prop = renderableNode.getNode().getProp();
			final Color color = colorProfiles.getDiagramSheet().getProcessNode().getFill();
			final Area enrichment = new Area(renderableNode.getBackgroundShape());
			final Rectangle2D rectangle = new Rectangle2D.Double(
					prop.getX() + prop.getWidth() * percentage,
					prop.getY(),
					prop.getWidth(),
					prop.getHeight());
			enrichment.intersect(new Area(rectangle));
			renderableNode.getBackgroundArea().subtract(enrichment);
			canvas.getNodeAnalysis().add(enrichment, color);
		}
		// process node text is not split
		return 0;
	}

	@Override
	public void text(RenderableNode renderableNode, DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, double textSplit) {
		final TextLayer layer = renderableNode.isFadeOut()
				? canvas.getFadeOutText()
				: canvas.getText();
		final Color color = getTextColor(renderableNode, colorProfiles, index);
		// Shrink textSplit
		// textSplit / width = newTextSplit / nWidth
		final double width = renderableNode.getNode().getProp().getWidth();
		final double newWidth = width - 2 * PROCESS_NODE_PADDING;
		textSplit = textSplit * newWidth / width;
		layer.add(renderableNode.getNode().getDisplayName(),
				color,
				renderableNode.getNode().getProp(),
				PROCESS_NODE_PADDING,
				textSplit,
				FontProperties.DEFAULT_FONT);
	}
}
