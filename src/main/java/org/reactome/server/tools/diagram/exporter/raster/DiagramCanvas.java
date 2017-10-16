package org.reactome.server.tools.diagram.exporter.raster;

import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.*;

import java.awt.*;
import java.util.stream.Stream;

public class DiagramCanvas {

	private FillLayer compartmentFill = new FillLayerImpl();
	private DrawLayer compartmentBorder = new DrawLayerImpl();
	private TextLayer compartmentText = new TextLayerImpl();
	private DrawLayer fadeOutSegments = new DrawLayerImpl();
	private FillLayer fadeOutNodeBackground = new FillLayerImpl();
	private DrawLayer fadeOutNodeBorder = new DrawLayerImpl();
	private TextLayer fadeOutText = new TextLayerImpl();
	private DrawLayer segments = new DrawLayerImpl();
	private DrawLayer flags = new DrawLayerImpl();
	private DrawLayer halo = new DrawLayerImpl();
	private DrawLayer nodeBorder = new DrawLayerImpl();
	private TextLayer text = new TextLayerImpl();
	private TextLayer notes = new TextLayerImpl();
	private FillLayer nodeAnalysis = new FillLayerImpl();
	private DrawLayer cross = new DrawLayerImpl();
	private FillLayer nodeBackground = new FillLayerImpl();
	private FillLayer nodeForeground = new FillLayerImpl();
	private FillLayer fadeOutNodeForeground = new FillLayerImpl();
	private FillDrawLayer edgeShapes = new FillDrawLayerImpl();
	private FillDrawLayer fadeOutEdgeShapes = new FillDrawLayerImpl();

	public DiagramCanvas() {
	}

	public void render(Graphics2D graphics) {
		Stream.of(
				compartmentFill,
				compartmentBorder,
				compartmentText,

				fadeOutSegments,
				fadeOutNodeBackground,
				fadeOutNodeForeground,
				fadeOutNodeBorder,
				fadeOutEdgeShapes,
				fadeOutText,

				cross,
				flags,
				halo,

				segments,
				nodeBackground,
				nodeAnalysis,
				nodeForeground,
				nodeBorder,
				edgeShapes,
				text,

				notes
		).forEach(layer -> layer.render(graphics));
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
}
