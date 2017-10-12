package org.reactome.server.tools.diagram.exporter.common;

import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.pptx.model.Stylesheet;
import org.reactome.server.tools.diagram.exporter.raster.RenderType;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ColorProfile {

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
	 * @param profile        the diagram to extract the color from
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
	 * @param profile        the diagram to extract the color from
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
		map.put("selection", ColorFactory.parseColor(profile.getProperties().getSelection()));
		map.put("highlight", ColorFactory.parseColor(profile.getProperties().getHighlight()));
		map.put("flag", ColorFactory.parseColor(profile.getProperties().getFlag()));
		map.put("disease", ColorFactory.parseColor(profile.getProperties().getDisease()));
		map.put("text", ColorFactory.parseColor(profile.getProperties().getText()));
		map.put("halo", ColorFactory.parseColor(profile.getProperties().getHalo()));
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

}
