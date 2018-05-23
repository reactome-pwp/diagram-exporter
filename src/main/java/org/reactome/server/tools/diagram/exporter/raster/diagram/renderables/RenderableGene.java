package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.StrokeStyle;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.TextLayer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class RenderableGene extends RenderableNode {

	private final Shape arrow;
	private final Shape lines;

	RenderableGene(Node node) {
		super(node);
		lines = ShapeFactory.getGeneLine(getNode().getProp());
		arrow = ShapeFactory.getGeneArrow(getNode().getProp());
	}

	@Override
	Shape backgroundShape() {
		return ShapeFactory.getGeneFillShape(getNode().getProp());
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getGene();
	}

	@Override
	public void background(DiagramCanvas canvas, DiagramIndex index, ColorProfiles colorProfiles) {
		final Color fill = getFillColor(colorProfiles, index.getAnalysis().getType());
		final Color border = getStrokeColor(colorProfiles, index.getAnalysis().getType());
		// report: genes are not dashed in PathwayBrowser, although json file says needDashedBorder
		final Stroke stroke = StrokeStyle.BORDER.get(false);
		if (isFadeOut()) {
			canvas.getFadeOutNodeBackground().add(getBackgroundArea(), fill);
			canvas.getFadeOutNodeBackground().add(arrow, fill);
			canvas.getFadeOutNodeBorder().add(lines, border, stroke);
			canvas.getFadeOutNodeBorder().add(arrow, border, stroke);
		} else {
			canvas.getNodeBackground().add(getBackgroundArea(), fill);
			canvas.getNodeBackground().add(arrow, fill);
			canvas.getNodeBorder().add(lines, border, stroke);
			canvas.getNodeBorder().add(arrow, border, stroke);
		}
	}

	@Override
	public void halo(DiagramCanvas canvas, ColorProfiles colorProfiles) {
		final Color color = colorProfiles.getDiagramSheet().getProperties().getHalo();
		final Stroke stroke = StrokeStyle.HALO.get(false);
		canvas.getHalo().add(arrow, color, stroke);
		canvas.getHalo().add(lines, color, stroke);
	}

	@Override
	public void flag(DiagramCanvas canvas, ColorProfiles colorProfiles) {
		final Color color = colorProfiles.getDiagramSheet().getProperties().getFlag();
		final Stroke stroke = StrokeStyle.FLAG.get(false);
		canvas.getFlags().add(arrow, color, stroke);
		canvas.getFlags().add(lines, color, stroke);
	}

	@Override
	public void text(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, double textSplit) {
		final TextLayer textLayer = isFadeOut()
				? canvas.getFadeOutText()
				: canvas.getText();
		Color color = getTextColor(colorProfiles, index.getAnalysis().getType());
		final Rectangle2D bounds = this.backgroundShape().getBounds2D();
		// report: some diagrams have too small gene shape. It seems to be a problem with the json file
		// in these cases we use node properties
		// when problem is solved, the if/else can be deleted
		if (bounds.getHeight() > FontProperties.DEFAULT_FONT.getSize()) {
			final NodeProperties limits = NodePropertiesFactory.get(
					bounds.getX(), bounds.getY(), bounds.getWidth(),
					bounds.getHeight());
			textLayer.add(getNode().getDisplayName(),
					color, limits, NODE_TEXT_PADDING,
					textSplit, FontProperties.DEFAULT_FONT);
		} else textLayer.add(getNode().getDisplayName(), color,
				getNode().getProp(),
				NODE_TEXT_PADDING,
				textSplit, FontProperties.DEFAULT_FONT);

	}
}
