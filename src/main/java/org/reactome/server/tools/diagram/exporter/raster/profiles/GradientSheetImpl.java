package org.reactome.server.tools.diagram.exporter.raster.profiles;


import java.awt.*;

public class GradientSheetImpl implements GradientSheet {
	private Color min;
	private Color stop;
	private Color max;

	@Override
	public Color getMin() {
		return min;
	}

	public void setMin(String color) {
		this.min = ColorFactory.parseColor(color);
	}

	@Override
	public Color getStop() {
		return stop;
	}

	public void setStop(String color) {
		this.stop = ColorFactory.parseColor(color);
	}

	@Override
	public Color getMax() {
		return max;
	}

	public void setMax(String color) {
		this.max = ColorFactory.parseColor(color);
	}

}
