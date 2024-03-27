package org.reactome.server.tools.diagram.exporter.raster.profiles;


import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.exporter.raster.resources.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ColorProfiles {
	private static final Map<String, AnalysisSheet> ANALYSIS_SHEETS = new TreeMap<>();
	private static final Map<String, DiagramSheet> DIAGRAM_SHEETS = new TreeMap<>();
	private static final Map<String, InteractorsSheet> INTERACTORS_SHEETS = new TreeMap<>();

	private static final String DEFAULT_DIAGRAM_PROFILE = "modern";
	private static final String DEFAULT_ANALYSIS_PROFILE = "standard";
	private static final String DEFAULT_INTERACTORS_PROFILE = "cyan";

	private static final List<String> DIAGRAM_PROFILE_NAMES = Arrays.asList(
			"diagram_profile_01.json",
			"diagram_profile_02.json");
	private static final List<String> ANALYSIS_PROFILE_NAMES = Arrays.asList(
			"analysis_profile_01.json",
			"analysis_profile_02.json",
			"analysis_profile_03.json");
	private static final List<String> INTERACTORS_PROFILE_NAMES = Arrays.asList(
			"interactors_profile_01.json",
			"interactors_profile_02.json");


	static {
		DIAGRAM_PROFILE_NAMES.stream()
				.map(name -> getSheet(DiagramSheet.class, name))
				.forEach(sheet -> DIAGRAM_SHEETS.put(sheet.getName().toLowerCase(), sheet));
		ANALYSIS_PROFILE_NAMES.stream()
				.map(name -> getSheet(AnalysisSheet.class, name))
				.forEach(sheet -> ANALYSIS_SHEETS.put(sheet.getName().toLowerCase(), sheet));
		INTERACTORS_PROFILE_NAMES.stream()
				.map(name -> getSheet(InteractorsSheet.class, name))
				.forEach(sheet -> INTERACTORS_SHEETS.put(sheet.getName().toLowerCase(), sheet));
	}

	private InteractorsSheet interactorsSheet;
	private DiagramSheet diagramSheet;
	private AnalysisSheet analysisSheet;

	public ColorProfiles(String diagram, String analysis, String interactors) {
		diagramSheet = getDiagramSheet(diagram);
		analysisSheet = getAnalysisSheet(analysis);
		interactorsSheet = getInteractorsSheet(interactors);
	}

	private static <T> T getSheet(Class<T> clazz, String filename) {
		InputStream resource = Resources.class.getResourceAsStream("profiles/" + filename);
		try {
			final String json = IOUtils.toString(resource, Charset.defaultCharset());
			return JsonMapper.builder()
					.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
					.build()
					.readValue(json, clazz);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private InteractorsSheet getInteractorsSheet(String interactors) {
		return interactors != null && INTERACTORS_SHEETS.containsKey(interactors.toLowerCase())
				? INTERACTORS_SHEETS.get(interactors.toLowerCase())
				: INTERACTORS_SHEETS.get(DEFAULT_INTERACTORS_PROFILE);
	}

	private AnalysisSheet getAnalysisSheet(String analysis) {
		return analysis != null && ANALYSIS_SHEETS.containsKey(analysis.toLowerCase())
				? ANALYSIS_SHEETS.get(analysis.toLowerCase())
				: ANALYSIS_SHEETS.get(DEFAULT_ANALYSIS_PROFILE);
	}

	private DiagramSheet getDiagramSheet(String diagram) {
		return diagram != null && DIAGRAM_SHEETS.containsKey(diagram.toLowerCase())
				? DIAGRAM_SHEETS.get(diagram.toLowerCase())
				: DIAGRAM_SHEETS.get(DEFAULT_DIAGRAM_PROFILE);
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
