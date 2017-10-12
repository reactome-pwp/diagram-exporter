package org.reactome.server.tools.diagram.exporter.raster;

import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.*;

import java.awt.*;
import java.util.stream.Stream;

public class DiagramCanvas {
	private FillLayer compartmentFill = new FillLayerImpl();
	private LineLayer compartmentBorder = new LineLayerImpl();
	private TextLayer compartmentText = new TextLayerImpl();
	private LineLayer fadeOutSegments = new LineLayerImpl();
	private FillLayer fadeOutFills = new FillLayerImpl();
	private LineLayer fadeOutBorders = new LineLayerImpl();
	private TextLayer fadeOutText = new TextLayerImpl();
	private LineLayer segments = new LineLayerImpl();
	private LineLayer flags = new LineLayerImpl();
	private LineLayer halos = new LineLayerImpl();
	private FillLayer fill = new FillLayerImpl();
	private LineLayer border = new LineLayerImpl();
	private TextLayer text = new TextLayerImpl();
	private TextLayer notes = new TextLayerImpl();
	private FillLayer analysis = new FillLayerImpl();
	private LineLayer cross = new LineLayerImpl();

	public DiagramCanvas() {
	}

	public void render(Graphics2D graphics) {
		Stream.of(
				compartmentFill,
				compartmentBorder,
				compartmentText,
				fadeOutSegments,
				fadeOutFills,
				fadeOutBorders,
				fadeOutText,
				flags,
				halos,
				segments,
				fill,
				analysis,
				border,
				cross,
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

	public FillLayer getFadeOutFills() {
		return fadeOutFills;
	}

	public LineLayer getFadeOutBorders() {
		return fadeOutBorders;
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

	public FillLayer getFill() {
		return fill;
	}

	public LineLayer getBorder() {
		return border;
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
}
