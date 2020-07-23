package org.reactome.server.tools.diagram.exporter.raster.profiles;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegulationSheet {

//	private final static Color DEFAULT_STOP_COLOUR = ColorFactory.parseColor("#999999");
	private GradientSheet gradient;
	private Map<Integer, Color> colorMap;
	private Color text;

	public RegulationSheet(GradientSheet gradient) {
		this.gradient = gradient;
		init();
	}

	public Color getText() {
		return text;
	}

	public GradientSheet getGradient() {
		return gradient;
	}

	public void setText(String color) {
		this.text = ColorFactory.parseColor(color);
	}

	public Map<Integer, Color> getColorMap() {
		return colorMap;
	}

	private void init() {
		colorMap = new LinkedHashMap<>(5);
		colorMap.put(2, ColorFactory.interpolate(gradient, 0));
		colorMap.put(1, ColorFactory.interpolate(gradient, 0.25));
		colorMap.put(0, ColorFactory.interpolate(gradient, 0.5));
		colorMap.put(-1, ColorFactory.interpolate(gradient, 0.75));
		colorMap.put(-2, ColorFactory.interpolate(gradient, 1));
	}
}
