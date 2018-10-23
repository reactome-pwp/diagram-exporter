package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramData;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.StrokeStyle;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.TextLayer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.awt.*;

public class RenderableEntitySet extends RenderableNode {


	private static double SET_PADDING = 4;

	RenderableEntitySet(Node node) {
		super(node);
	}

	@Override
	Shape backgroundShape() {
		return ShapeFactory.roundedRectangle(getNode().getProp());
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getEntitySet();
	}

	@Override
	public void draw(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramData data, int t) {
		super.draw(canvas, colorProfiles, data, t);
		// Inner shape
		final Color border = getStrokeColor(colorProfiles, data.getAnalysis().getType());
		final Shape shape = ShapeFactory.roundedRectangle(getNode().getProp(), SET_PADDING);
		final Stroke stroke = StrokeStyle.BORDER.get(isDashed());
		final DrawLayer layer = isFadeOut()
				? canvas.getFadeOutNodeBorder()
				: canvas.getNodeBorder();
		layer.add(shape, border, stroke);
	}

	@Override
	protected void text(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramData data, double textSplit) {
		final TextLayer layer = isFadeOut()
				? canvas.getFadeOutText()
				: canvas.getText();
		final Color color = getTextColor(colorProfiles, data.getAnalysis().getType());
		layer.add(getNode().getDisplayName(),
				color,
				getNode().getProp(),
				SET_PADDING,
				textSplit,
				FontProperties.DEFAULT_FONT);
	}
}
