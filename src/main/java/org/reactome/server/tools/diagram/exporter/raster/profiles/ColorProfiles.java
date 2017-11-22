package org.reactome.server.tools.diagram.exporter.raster.profiles;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public class ColorProfiles {
	private static final Map<String, AnalysisSheet> ANALYSIS_SHEET_MAP = new TreeMap<>();
	private static final Map<String, DiagramSheet> DIAGRAM_SHEET_MAP = new TreeMap<>();
	private static final Map<String, InteractorsSheet> INTERACTORS_SHEET_MAP = new TreeMap<>();

	private static final String DEFAULT_DIAGRAM_PROFILE = "modern";
	private static final String DEFAULT_ANALYSIS_PROFILE = "standard";
	private static final String DEFAULT_INTERACTORS_PROFILE = "cyan";
	private InteractorsSheet interactorsSheet;
	private DiagramSheet diagramSheet;
	private AnalysisSheet analysisSheet;

	static {
		Stream.of("modern", "standard")
				.forEach(profile -> DIAGRAM_SHEET_MAP.put(profile, getSheet(DiagramSheetImpl.class, "diagram", profile)));
		Stream.of("standard", "copper plus", "strosobar")
				.forEach(profile -> ANALYSIS_SHEET_MAP.put(profile, getSheet(AnalysisSheetImpl.class, "analysis", profile)));
		Stream.of("cyan", "teal")
				.forEach(profile -> INTERACTORS_SHEET_MAP.put(profile, getSheet(InteractorsSheetImpl.class, "interactors", profile)));
	}

	public ColorProfiles(String diagram, String analysis, String interactors) {
//		diagramSheet = getSheet(DiagramSheetImpl.class, DEFAULT_DIAGRAM_PROFILE, "diagram", diagram);
//		analysisSheet = getSheet(AnalysisSheetImpl.class, DEFAULT_ANALYSIS_PROFILE, "analysis", analysis);
//		interactorsSheet = getSheet(InteractorsSheetImpl.class, DEFAULT_INTERACTORS_PROFILE, "interactors", interactors);
		diagramSheet = getDiagramSheet(diagram);
		analysisSheet = getAnalysisSheet(analysis);
		interactorsSheet = getInteractorsSheet(interactors);
	}

	private InteractorsSheet getInteractorsSheet(String interactors) {
		return interactors != null && INTERACTORS_SHEET_MAP.containsKey(interactors.toLowerCase())
				? INTERACTORS_SHEET_MAP.get(interactors.toLowerCase())
				: INTERACTORS_SHEET_MAP.get(DEFAULT_INTERACTORS_PROFILE);
	}

	private AnalysisSheet getAnalysisSheet(String analysis) {
		return analysis != null && ANALYSIS_SHEET_MAP.containsKey(analysis.toLowerCase())
				? ANALYSIS_SHEET_MAP.get(analysis.toLowerCase())
				: ANALYSIS_SHEET_MAP.get(DEFAULT_ANALYSIS_PROFILE);
	}

	private DiagramSheet getDiagramSheet(String diagram) {
		return diagram != null && DIAGRAM_SHEET_MAP.containsKey(diagram.toLowerCase())
				? DIAGRAM_SHEET_MAP.get(diagram.toLowerCase())
				: DIAGRAM_SHEET_MAP.get(DEFAULT_DIAGRAM_PROFILE);
	}

	@JsonCreator
	public ColorProfiles(Map<String, Object> profiles) {
		diagramSheet = getDiagramSheet((String) profiles.get("diagram"));
		analysisSheet = getAnalysisSheet((String) profiles.get("analysis"));
		interactorsSheet = getInteractorsSheet((String) profiles.get("interactors"));
	}

	private static <T> T getSheet(Class<T> clazz, String prefix, String name) {
		String filename = String.format("%s_%s.json", prefix, name.toLowerCase());
		InputStream resource = ColorProfiles.class.getResourceAsStream(filename);
		try {
			final String json = IOUtils.toString(resource, Charset.defaultCharset());
			final ObjectMapper mapper = new ObjectMapper();
			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
			return mapper.readValue(json, clazz);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public DiagramSheet getDiagramSheet() {
		return diagramSheet;
	}

	public AnalysisSheet getAnalysisSheet() {
		return analysisSheet;
	}

	public InteractorsSheet getInteractorsSheet() {
		return interactorsSheet;
	}
}
