package org.reactome.server.tools.diagram.exporter.raster.profiles;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegulationSheet {

//	private final static Color DEFAULT_STOP_COLOUR = ColorFactory.parseColor("#999999");
	private GradientSheet gradient;
	private Map<Integer, Color> colorMap;
	private Color text;
	private static final Color DEFAULT_NOT_FOUND = new Color(153, 153, 153);

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
		colorMap.put(0, getNotFoundColor(ColorFactory.interpolate(gradient, 0.5)));
		colorMap.put(-1, ColorFactory.interpolate(gradient, 0.75));
		colorMap.put(-2, ColorFactory.interpolate(gradient, 1));
	}


	private Color getNotFoundColor(Color input) {
		Color rtn;
		try {
			Color c = new Color(input.getRGB());
			rtn = getGray(c.getRed(), c.getGreen(), c.getBlue());
		} catch (Exception e) {
			rtn = DEFAULT_NOT_FOUND;
		}
		return rtn;
	}

	/**
	 * Returns the converted grayscale Color based on this formula
	 * R' = G' = B'  = 0.299R + 0.587G + 0.114B
	 *
	 * @return
	 */
	private static Color getGray(int red, int green, int blue) {
		float g = (0.299f * red + 0.587f * green + 0.114f * blue);
		int v = (int) g;
		Color gray = null;
		try {
			gray = new Color(v, v, v);
		} catch (Exception e) {
			//Nothing here
		}
		return gray;
	}
}
