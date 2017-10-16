package org.reactome.server.tools.diagram.exporter.raster.profiles;

import org.reactome.server.tools.diagram.data.profile.analysis.ProfileGradient;

import java.awt.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse colors in hex RGB (#FF0000) and rgba(255,255,0, 0.5)
 */
public class ColorFactory {
	private final static Pattern RGBA = Pattern.compile("^rgba\\(\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])\\s*,\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])\\s*,\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])\\s*,\\s*((0.[0-9]+)|[01]|1.0*)\\s*\\)$");

	// speed up with a color cache
	// of course, this shouldn't be necessary if the Profiles already had the
	// colors parsed
	private static final Map<String, Color> cache = new HashMap<>();

	public static Color parseColor(String color) {
		if (color == null) return null;
		return cache.computeIfAbsent(color, ColorFactory::strToColor);
	}

	private static Color strToColor(String color) {
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

	public static String asRgba(Color color) {
		return String.format(Locale.US, "rgba(%d, %d, %d, %.2f)",
				color.getRed(), color.getGreen(), color.getBlue(),
				color.getAlpha() / 255.0);
	}

	public static Color blend(Color back, Color front) {
		double b = back.getAlpha() / 255.0;
		double f = front.getAlpha() / 255.0;
		double alpha = b * f + (1 - b);
		int red = (int) (back.getRed() * b + front.getRed() * f);
		int green = (int) (back.getGreen() * b + front.getGreen() * f);
		int blue = (int) (back.getBlue() * b + front.getBlue() * f);
//		int red = back.getRed() * back.getAlpha() + front.getRed() * front.getAlpha();
//		int green = back.getGreen() + (front.getGreen() - back.getGreen()) / 2;
//		int blue = back.getBlue() + (front.getBlue() - back.getBlue()) / 2;
		return new Color(
				Math.min(255, red),
				Math.min(255, green),
				Math.min(255, blue),
				Math.min(255, (int) (alpha * 255)));
	}

	public static String interpolate(ProfileGradient gradient, double scale) {
		final Color min = parseColor(gradient.getMin());
		final Color max = parseColor(gradient.getMax());
		final Color interpolate = interpolate(max, min, scale);
		return asRgba(interpolate);
	}

	private static Color interpolate(Color a, Color b, double t) {
		if (t <= 0.0) return a;
		if (t >= 1.0) return b;
		float scale = (float) t;
		return new Color(
				(int) (a.getRed() + (b.getRed() - a.getRed()) * scale),
				(int) (a.getGreen() + (b.getGreen() - a.getGreen()) * scale),
				(int) (a.getBlue() + (b.getBlue() - a.getBlue()) * scale),
				(int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * scale));
	}
}
