package org.reactome.server.tools.diagram.exporter.raster.profiles;


import java.awt.*;

public class GradientSheet {
	private Color min;
	private Color stop;
	private Color max;


	public Color getMin() {
		return min;
	}

	public void setMin(String color) {
		this.min = ColorFactory.parseColor(color);
	}


	public Color getStop() {
		return stop;
	}

	public void setStop(String color) {
		this.stop = ColorFactory.parseColor(color);
	}


	public Color getMax() {
		return max;
	}

	public void setMax(String color) {
		this.max = ColorFactory.parseColor(color);
	}

}
