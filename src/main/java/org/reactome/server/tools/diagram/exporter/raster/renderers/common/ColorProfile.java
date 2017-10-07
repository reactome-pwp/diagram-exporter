package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import org.reactome.server.tools.diagram.data.layout.Shadow;
import org.reactome.server.tools.diagram.data.profile.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.pptx.model.Stylesheet;
import org.reactome.server.tools.diagram.exporter.raster.RenderType;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ColorProfile {

	private final static Pattern RGBA = Pattern.compile("^rgba\\(\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])\\s*,\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])\\s*,\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])\\s*,\\s*((0.[1-9])|[01])\\s*\\)$");
	private static final List<String> LINKS = Arrays.asList("Interaction", "EntitySetAndEntitySetLink", "EntitySetAndMemberLink");

	/**
	 * rendering classes colors
	 */
	private static Map<String, Map<String, Stylesheet>> stylesheets = new HashMap<>();
	/**
	 * General colors
	 */
	private static Map<String, Map<String, Paint>> properties = new HashMap<>();


	/**
	 * Computes the color of the "stroke" property for the given renderingClass
	 * based on the renderType
	 *
	 * @param profile        the profile to extract the color from
	 * @param renderingClass the class of the object to paint
	 * @param renderType     the render type to choose between normal, light,
	 *                       fadeout or disease
	 *
	 * @return a Paint with a proper color for drawing lines
	 */
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

	/**
	 * Computes the color of the "fill" property for the given renderingClass
	 * based on the renderType
	 *
	 * @param profile        the profile to extract the color from
	 * @param renderingClass the class of the object to paint
	 * @param renderType     the render type to choose between normal, light or
	 *                       fadeout
	 *
	 * @return a Paint with a proper color for drawing lines
	 */
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
		if (LINKS.contains(type))
			type = "link";
		return stylesheets
				.computeIfAbsent(profile.getName(), k -> new HashMap<>())
				.computeIfAbsent(type, k -> new Stylesheet(profile, k));
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

	/**
	 * Gets a color from the properties
	 *
	 * @param profile
	 * @param type
	 *
	 * @return
	 */
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
		final Matcher m = RGBA.matcher(input);
		if (m.matches()) {
			return new Color(Integer.parseInt(m.group(1)),
					Integer.parseInt(m.group(2)),
					Integer.parseInt(m.group(3)),
					(int) (Float.parseFloat(m.group(4)) * 255f));
		}
		return null;
	}

	public static Paint getShadowFill(Shadow shadow) {
		final Color color = parseColor(shadow.getColour());
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), 20);
	}

	public static Paint getShadowLine(Shadow shadow) {
		final Color color = parseColor(shadow.getColour());
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), 200);
	}
}
