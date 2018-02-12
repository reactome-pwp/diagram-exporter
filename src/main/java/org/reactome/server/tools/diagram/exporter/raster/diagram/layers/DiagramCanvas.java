package org.reactome.server.tools.diagram.exporter.raster.diagram.layers;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

/**
 * Just a few layers.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class DiagramCanvas {

	private FillLayer compartmentFill = new FillLayer();
	private DrawLayer compartmentBorder = new DrawLayer();
	private TextLayer compartmentText = new TextLayer();
	private DrawLayer fadeOutSegments = new DrawLayer();
	private FillLayer fadeOutNodeBackground = new FillLayer();
	private DrawLayer fadeOutNodeBorder = new DrawLayer();
	private TextLayer fadeOutText = new TextLayer();
	private DrawLayer segments = new DrawLayer();
	private DrawLayer flags = new DrawLayer();
	private DrawLayer halo = new DrawLayer();
	private DrawLayer nodeBorder = new DrawLayer();
	private TextLayer text = new TextLayer();
	private TextLayer notes = new TextLayer();
	private FillLayer nodeAnalysis = new FillLayer();
	private DrawLayer cross = new DrawLayer();
	private FillLayer nodeBackground = new FillLayer();
	private FillLayer nodeForeground = new FillLayer();
	private FillLayer fadeOutNodeForeground = new FillLayer();
	private FillDrawLayer edgeShapes = new FillDrawLayer();
	private FillDrawLayer fadeOutEdgeShapes = new FillDrawLayer();
	private FillDrawLayer attachments = new FillDrawLayer();
	private FillDrawLayer fadeOutAttachments = new FillDrawLayer();
	private FillDrawLayer legendBackground = new FillDrawLayer();
	private TextLayer legendText = new TextLayer();
	private FillLayer legendBar = new FillLayer();
	private DrawLayer legendTicks = new DrawLayer();
	private FillLayer legendTickArrows = new FillLayer();
	private TextLayer legendBottomText = new TextLayer();
	private ImageLayer logoLayer = new ImageLayer();

	private final List<Layer> layers = Arrays.asList(
			compartmentFill,
			compartmentBorder,
			compartmentText,

			fadeOutSegments,
			fadeOutEdgeShapes,
			fadeOutNodeBackground,
			fadeOutNodeForeground,
			fadeOutNodeBorder,
			fadeOutAttachments,
			fadeOutText,

			cross,
			flags,
			halo,

			segments,
			edgeShapes,
			nodeBackground,
			nodeAnalysis,
			nodeForeground,
			nodeBorder,
			attachments,
			text,

			notes,

			legendBackground,
			legendBar,
			legendTicks,
			legendTickArrows,
			legendText,
			legendBottomText,

			logoLayer
	);

	public DiagramCanvas() {
	}

	/**
	 * loops through layers in order, calling {@link Layer#render(Graphics2D)}
	 * for each. This should create an Image in graphics.
	 */
	public void render(Graphics2D graphics) {
		layers.forEach(layer -> layer.render(graphics));
	}

	public DrawLayer getCompartmentBorder() {
		return compartmentBorder;
	}

	public FillLayer getCompartmentFill() {
		return compartmentFill;
	}

	public TextLayer getCompartmentText() {
		return compartmentText;
	}

	public DrawLayer getFadeOutSegments() {
		return fadeOutSegments;
	}

	public DrawLayer getSegments() {
		return segments;
	}

	public FillLayer getFadeOutNodeBackground() {
		return fadeOutNodeBackground;
	}

	public DrawLayer getFadeOutNodeBorder() {
		return fadeOutNodeBorder;
	}

	public TextLayer getFadeOutText() {
		return fadeOutText;
	}

	public DrawLayer getFlags() {
		return flags;
	}

	public DrawLayer getHalo() {
		return halo;
	}

	public DrawLayer getNodeBorder() {
		return nodeBorder;
	}

	public TextLayer getText() {
		return text;
	}

	public TextLayer getNotes() {
		return notes;
	}

	public DrawLayer getCross() {
		return cross;
	}

	public FillLayer getNodeBackground() {
		return nodeBackground;
	}

	public FillLayer getNodeAnalysis() {
		return nodeAnalysis;
	}

	public FillLayer getNodeForeground() {
		return nodeForeground;
	}

	public FillLayer getFadeOutNodeForeground() {
		return fadeOutNodeForeground;
	}

	public FillDrawLayer getEdgeShapes() {
		return edgeShapes;
	}

	public FillDrawLayer getFadeOutEdgeShapes() {
		return fadeOutEdgeShapes;
	}

	public FillDrawLayer getAttachments() {
		return attachments;
	}

	public FillDrawLayer getFadeOutAttachments() {
		return fadeOutAttachments;
	}

	public FillDrawLayer getLegendBackground() {
		return legendBackground;
	}

	public FillLayer getLegendBar() {
		return legendBar;
	}

	public DrawLayer getLegendTicks() {
		return legendTicks;
	}

	public FillLayer getLegendTickArrows() {
		return legendTickArrows;
	}

	public TextLayer getLegendText() {
		return legendText;
	}

	public TextLayer getLegendBottomText() {
		return legendBottomText;
	}

	public ImageLayer getLogoLayer() {
		return logoLayer;
	}

	/**
	 * Calculate and get the bounds of the Canvas. Runs through layers, asking
	 * for each bounds. Don't use as a getter, since every call recomputes the
	 * bounds.
	 */
	public Rectangle2D getBounds() {
		double x = Double.MAX_VALUE, y = Double.MAX_VALUE, maxX = 0, maxY = 0;
		for (Layer layer : layers) {
			final Rectangle2D bounds = layer.getBounds();
			if (bounds == null) continue;
			if (bounds.getX() < x) x = bounds.getX();
			if (bounds.getY() < y) y = bounds.getY();
			if (bounds.getMaxX() > maxX) maxX = bounds.getMaxX();
			if (bounds.getMaxY() > maxY) maxY = bounds.getMaxY();
		}
		return new Rectangle2D.Double(x, y, maxX - x, maxY - y);
	}
}
