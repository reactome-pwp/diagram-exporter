package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.StrokeStyle;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.TextLayer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

public class RenderableProcessNode extends RenderableNode {


	// When it comes to analysis, alpha must be 0.75
	private static final Color ANALYSIS_INNER_COLOR = new Color(254, 253, 255, 191);
	private static final Color INNER_COLOR = new Color(254, 253, 255);
	static final double PROCESS_NODE_PADDING = 10;

	RenderableProcessNode(Node node) {
		super(node);
	}

	@Override
	Shape backgroundShape() {
		return ShapeFactory.rectangle(getNode().getProp());
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getProcessNode();
	}

	Shape innerShape() {
		return ShapeFactory.rectangle(getNode().getProp(), PROCESS_NODE_PADDING);
	}

	@Override
	public void draw(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		super.draw(canvas, colorProfiles, index, t);
		final Shape rectangle = innerShape();
		final Color fill = index.getAnalysis().getType() == null
				? INNER_COLOR
				: ANALYSIS_INNER_COLOR;
		final Color innerBorder = getInnerStrokeColor(colorProfiles, index.getAnalysis().getType());
		final Stroke stroke = StrokeStyle.BORDER.get(isDashed());
		if (isFadeOut()) {
			canvas.getFadeOutNodeForeground().add(rectangle, fill);
			canvas.getFadeOutNodeBorder().add(rectangle, innerBorder, stroke);
		} else {
			canvas.getNodeForeground().add(rectangle, fill);
			canvas.getNodeBorder().add(rectangle, innerBorder, stroke);
		}
	}

	/** Inner border is not selected */
	private Color getInnerStrokeColor(ColorProfiles colorProfiles, AnalysisType type) {
		if (isDisease())
			return colorProfiles.getDiagramSheet().getProperties().getDisease();
		if (isFadeOut())
			return getColorProfile(colorProfiles).getFadeOutStroke();
		if (type != null)
			return getColorProfile(colorProfiles).getLighterStroke();
		return getColorProfile(colorProfiles).getStroke();
	}

	@Override
	public double expression(DiagramCanvas canvas, DiagramIndex index, ColorProfiles colorProfiles, int t) {
		final Double percentage = getEnrichment();
		if (percentage != null && percentage > 0) {
			final NodeProperties prop = getNode().getProp();
			final Color color = colorProfiles.getDiagramSheet().getProcessNode().getFill();
			final Area enrichment = new Area(backgroundShape());
			final Rectangle2D rectangle = new Rectangle2D.Double(
					prop.getX() + prop.getWidth() * percentage,
					prop.getY(),
					prop.getWidth(),
					prop.getHeight());
			enrichment.intersect(new Area(rectangle));
			getBackgroundArea().subtract(enrichment);
			canvas.getNodeAnalysis().add(enrichment, color);
		}
		// process node text is not split
		return 0;
	}

	@Override
	public void text(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, double textSplit) {
		final TextLayer layer = isFadeOut()
				? canvas.getFadeOutText()
				: canvas.getText();
		final Color color = getTextColor(colorProfiles, index.getAnalysis().getType());
		// Shrink textSplit
		// textSplit / width = newTextSplit / nWidth
		final double width = getNode().getProp().getWidth();
		final double newWidth = width - 2 * PROCESS_NODE_PADDING;
		textSplit = textSplit * newWidth / width;
		layer.add(getNode().getDisplayName(),
				color,
				getNode().getProp(),
				PROCESS_NODE_PADDING,
				textSplit,
				FontProperties.DEFAULT_FONT);
	}
}
