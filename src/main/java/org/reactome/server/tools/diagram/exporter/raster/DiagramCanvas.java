package org.reactome.server.tools.diagram.exporter.raster;

import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.*;

import java.awt.*;
import java.util.stream.Stream;

public class DiagramCanvas {
	private FillLayer compartmentFill = new FillLayerImpl();
	private LineLayer compartmentBorder = new LineLayerImpl();
	private TextLayer compartmentText = new TextLayerImpl();
	private LineLayer fadeOutSegments = new LineLayerImpl();
	private FillLayer fadeOutNodeBackground = new FillLayerImpl();
	private LineLayer fadeOutNodeBorder = new LineLayerImpl();
	private TextLayer fadeOutText = new TextLayerImpl();
	private LineLayer segments = new LineLayerImpl();
	private LineLayer flags = new LineLayerImpl();
	private LineLayer halos = new LineLayerImpl();
	private FillLayer edgeFill = new FillLayerImpl();
	private LineLayer nodeBorder = new LineLayerImpl();
	private TextLayer text = new TextLayerImpl();
	private TextLayer notes = new TextLayerImpl();
	private FillLayer analysis = new FillLayerImpl();
	private LineLayer cross = new LineLayerImpl();
	private FillLayer nodeBackground = new FillLayerImpl();
	private FillLayer nodeForeground = new FillLayerImpl();
	private LineLayer edgeBorder = new LineLayerImpl();
	private FillLayer fadeOutNodeForeground = new FillLayerImpl();
	private FillLayer fadeoutEdgeFill = new FillLayerImpl();
	private LineLayer fadeoutEdgeBorder = new LineLayerImpl();

	public DiagramCanvas() {
	}

	public void render(Graphics2D graphics) {
		Stream.of(
				// Compartments
				compartmentFill,
				compartmentBorder,
				compartmentText,

				fadeOutSegments,
				fadeOutNodeBackground,
				fadeOutNodeForeground,
				fadeOutNodeBorder,
				fadeoutEdgeFill,
				fadeoutEdgeBorder,
				fadeOutText,
				cross,

				flags,
				halos,

				segments,
				nodeBackground,
				analysis,
				nodeForeground,
				nodeBorder,
				edgeFill,
				edgeBorder,
				text,

				notes
		).forEach(layer -> layer.render(graphics));
	}

	public LineLayer getCompartmentBorder() {
		return compartmentBorder;
	}

	public FillLayer getCompartmentFill() {
		return compartmentFill;
	}

	public TextLayer getCompartmentText() {
		return compartmentText;
	}

	public LineLayer getFadeOutSegments() {
		return fadeOutSegments;
	}

	public LineLayer getSegments() {
		return segments;
	}

	public FillLayer getFadeOutNodeBackground() {
		return fadeOutNodeBackground;
	}

	public LineLayer getFadeOutNodeBorder() {
		return fadeOutNodeBorder;
	}

	public TextLayer getFadeOutText() {
		return fadeOutText;
	}

	public LineLayer getFlags() {
		return flags;
	}

	public LineLayer getHalos() {
		return halos;
	}

	public FillLayer getEdgeFill() {
		return edgeFill;
	}

	public LineLayer getNodeBorder() {
		return nodeBorder;
	}

	public TextLayer getText() {
		return text;
	}

	public TextLayer getNotes() {
		return notes;
	}

	public LineLayer getCross() {
		return cross;
	}

	public FillLayer getNodeBackground() {
		return nodeBackground;
	}

	public FillLayer getAnalysis() {
		return analysis;
	}

	public FillLayer getNodeForeground() {
		return nodeForeground;
	}

	public LineLayer getEdgeBorder() {
		return edgeBorder;
	}

	public FillLayer getFadeOutNodeForeground() {
		return fadeOutNodeForeground;
	}

	public FillLayer getFadeoutEdgeFill() {
		return fadeoutEdgeFill;
	}

	public LineLayer getFadeoutEdgeBorder() {
		return fadeoutEdgeBorder;
	}

}
