package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import org.reactome.server.tools.diagram.data.layout.Shadow;
import org.reactome.server.tools.diagram.data.profile.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.pptx.model.Stylesheet;
import org.reactome.server.tools.diagram.exporter.raster.RenderType;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorProfile {

	public static Font SHADOWS_FONT;
	public static Font DEFAULT_FONT;
	public static Stroke HALO_STROKE;
	public static Stroke DEFAULT_LINE_STROKE;
	public static Stroke DEFAULT_BORDER_STROKE;
	public static Stroke SELECTION_STROKE;
	public static Stroke THICK_BORDER;

	/**
	 * rendering classes colors
	 */
	private static Map<String, Map<String, Stylesheet>> stylesheets = new HashMap<>();
	/**
	 * General colors
	 */
	private static Map<String, Map<String, Paint>> properties = new HashMap<>();

	static {
		setFactor(1.0);
	}

	public static void setFactor(double factor) {
		DEFAULT_LINE_STROKE = new BasicStroke((float) (1 * factor), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
		DEFAULT_BORDER_STROKE = new BasicStroke((float) (2 * factor), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
		SELECTION_STROKE = new BasicStroke((float) (3 * factor), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
		HALO_STROKE = new BasicStroke((float) (6 * factor), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
		THICK_BORDER = new BasicStroke((float) (3 * factor), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
		SHADOWS_FONT = new Font("arial", Font.BOLD, (int) (24 * factor));
		DEFAULT_FONT = new Font("arial", Font.BOLD, (int) (9 * factor));
	}

	public static Paint getLineColor(DiagramProfile profile, String renderingClass, RenderType renderType) {
		final Stylesheet styleSheet = getStyleSheet(profile, renderingClass);
		switch (renderType) {
			case FADE_OUT:
				return styleSheet.getFadeOutStroke();
			case NORMAL:
				return styleSheet.getLineColor();
			case DISEASE:
			case NOT_HIT_BY_ANALYSIS_DISEASE:
			case HIT_BY_ENRICHMENT_DISEASE:
			case HIT_BY_EXPRESSION_DISEASE:
				return styleSheet.getDiseaseColor();
			case HIT_INTERACTORS:
			case NOT_HIT_BY_ANALYSIS_NORMAL:
			case HIT_BY_ENRICHMENT_NORMAL:
			case HIT_BY_EXPRESSION_NORMAL:
				return styleSheet.getLighterStroke();
			default:
				return styleSheet.getLineColor();
		}
	}

	public static Paint getFillColor(DiagramProfile profile, String renderingClass, RenderType renderType) {
		final Stylesheet styleSheet = getStyleSheet(profile, renderingClass);
		switch (renderType) {
			case FADE_OUT:
				return styleSheet.getFadeOutFill();
			case NORMAL:
				return styleSheet.getFillColor();
			case HIT_BY_ENRICHMENT_NORMAL:
			case HIT_BY_ENRICHMENT_DISEASE:
				// TODO: AnalysisColours.get().PROFILE.getEnrichment().getGradient().getMax()
			case HIT_INTERACTORS:
				// TODO: AnalysisColours.get().PROFILE.getRibbon()
			case NOT_HIT_BY_ANALYSIS_DISEASE:
			case DISEASE:
			case NOT_HIT_BY_ANALYSIS_NORMAL:
			case HIT_BY_EXPRESSION_NORMAL:
			case HIT_BY_EXPRESSION_DISEASE:
//				return styleSheet.getLighterFill();
			default:
				return styleSheet.getFillColor();
		}
	}

	public static Stroke getStroke(DiagramProfile profile, String renderingClass, RenderType renderType) {
		switch (renderType) {
			case HIT_INTERACTORS:
				return THICK_BORDER;
			default:
				return DEFAULT_LINE_STROKE;
		}
	}

	public static Paint getTextColor(DiagramProfile profile, String renderingClass, RenderType renderType) {
		final Stylesheet styleSheet = getStyleSheet(profile, renderingClass);
		switch (renderType) {
			case FADE_OUT:
				return styleSheet.getFadeOutText();
			case NOT_HIT_BY_ANALYSIS_NORMAL:
				return styleSheet.getLighterText();
			default:
				return styleSheet.getTextColor();
		}
	}


	private static Stylesheet getStyleSheet(DiagramProfile profile, String type) {
		if (type.equals("Entity"))
			type = "OtherEntity";
		stylesheets.putIfAbsent(profile.getName(), new HashMap<>());
		final Map<String, Stylesheet> style = stylesheets.get(profile.getName());
		style.putIfAbsent(type, new Stylesheet(profile, type));
		return style.get(type);
	}

	private static void addProfile(DiagramProfile profile) {
		final Map<String, Paint> map = new HashMap<>();
		map.put("selection", parseColor(profile.getProperties().getSelection()));
		map.put("highlight", parseColor(profile.getProperties().getHighlight()));
		map.put("flag", parseColor(profile.getProperties().getFlag()));
		map.put("disease", parseColor(profile.getProperties().getDisease()));
		map.put("text", parseColor(profile.getProperties().getText()));
		map.put("halo", parseColor(profile.getProperties().getHalo()));
		properties.putIfAbsent(profile.getName(), map);
	}

	public static Paint getProfileColor(DiagramProfile profile, String type) {
		if (!properties.containsKey(profile.getName()))
			addProfile(profile);
		return properties.get(profile.getName()).get(type);
	}

	private static Color parseColor(String color) {
		if (color == null) return null;

		if (color.startsWith("#")) {
			return hexToColor(color);
		}
		return rgbaToColor(color);
	}

	private static Color hexToColor(String input) {
		int r = Integer.valueOf(input.substring(1, 3), 16);
		int g = Integer.valueOf(input.substring(3, 5), 16);
		int b = Integer.valueOf(input.substring(5, 7), 16);

		return new Color(r, g, b);
	}

	private static Color rgbaToColor(String input) {
		String rgbaRegex = "^rgba\\(\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])\\s*,\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])\\s*,\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])\\s*,\\s*((0.[1-9])|[01])\\s*\\)$";

		Pattern c = Pattern.compile(rgbaRegex);
		Matcher m = c.matcher(input);
		if (m.matches()) {
			return new Color(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), (int) (Float.parseFloat(m.group(4)) * 255f));
		}
		return null;
	}

	public static Paint getShadowFill(Shadow shadow) {
		final Color color = parseColor(shadow.getColour());
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), 50);
	}

	public static Paint getShadowLine(Shadow shadow) {
		return parseColor(shadow.getColour());
	}
}
