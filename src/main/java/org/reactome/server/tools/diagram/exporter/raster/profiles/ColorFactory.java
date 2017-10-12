package org.reactome.server.tools.diagram.exporter.raster.profiles;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse colors in hex RGB (#FF0000) and rgba(255,255,0, 0.5)
 */
public class ColorFactory {
	private final static Pattern RGBA = Pattern.compile("^rgba\\(\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])\\s*,\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])\\s*,\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])\\s*,\\s*((0.[1-9])|[01])\\s*\\)$");

	public static Color parseColor(String color) {
		if (color == null) return null;
		return color.startsWith("#")
				? hexToColor(color)
				: rgbaToColor(color);
	}

	private static Color hexToColor(String input) {
		int r = Integer.valueOf(input.substring(1, 3), 16);
		int g = Integer.valueOf(input.substring(3, 5), 16);
		int b = Integer.valueOf(input.substring(5, 7), 16);

		return new Color(r, g, b);
	}

	private static Color rgbaToColor(String input) {
		final Matcher m = RGBA.matcher(input);
		if (m.matches()) {
			return new Color(Integer.parseInt(m.group(1)),
					Integer.parseInt(m.group(2)),
					Integer.parseInt(m.group(3)),
					(int) (Float.parseFloat(m.group(4)) * 255f));
		}
		return null;
	}
}
