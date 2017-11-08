package org.reactome.server.tools.diagram.exporter.raster.profiles;


import java.awt.*;

public class LegendSheetImpl implements LegendSheet {
	private Color median;
	private Color hover;

	@Override
	public Color getMedian() {
		return median;
	}

	@Override
	public Color getHover() {
		return hover;
	}

	public void setHover(String color) {
		this.hover = ColorFactory.parseColor(color);
	}

	public void setMedian(String color) {
		this.median = ColorFactory.parseColor(color);
	}
}
