package org.reactome.server.tools.diagram.exporter.raster.profiles;


import java.awt.*;

public class LegendSheet {
	private Color median;
	private Color hover;


	public Color getMedian() {
		return median;
	}

	public void setMedian(String color) {
		this.median = ColorFactory.parseColor(color);
	}

	public Color getHover() {
		return hover;
	}

	public void setHover(String color) {
		this.hover = ColorFactory.parseColor(color);
	}
}
