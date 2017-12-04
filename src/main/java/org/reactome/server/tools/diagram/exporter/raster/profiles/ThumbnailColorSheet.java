package org.reactome.server.tools.diagram.exporter.raster.profiles;

import java.awt.*;

public class ThumbnailColorSheet {
	private Color node;
	private Color edge;
	private Color hovering;
	private Color highlight;
	private Color selection;

	
	public Color getNode() {
		return node;
	}

	public void setNode(String color) {
		this.node = ColorFactory.parseColor(color);
	}

	
	public Color getEdge() {
		return edge;
	}

	public void setEdge(String color) {
		this.edge = ColorFactory.parseColor(color);
	}

	
	public Color getHovering() {
		return hovering;
	}

	public void setHovering(String color) {
		this.hovering = ColorFactory.parseColor(color);
	}

	
	public Color getHighlight() {
		return highlight;
	}

	public void setHighlight(String color) {
		this.highlight = ColorFactory.parseColor(color);
	}

	
	public Color getSelection() {
		return selection;
	}

	public void setSelection(String color) {
		this.selection = ColorFactory.parseColor(color);
	}

}
