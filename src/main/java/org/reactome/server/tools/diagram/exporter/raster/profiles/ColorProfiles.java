package org.reactome.server.tools.diagram.exporter.raster.profiles;


import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class ColorProfiles {
	private static final String DEFAULT_DIAGRAM_PROFILE = "modern";
	private static final String DEFAULT_ANALYSIS_PROFILE = "standard";
	private static final String DEFAULT_INTERACTORS_PROFILE = "cyan";
	private InteractorsSheet interactorsSheet;
	private DiagramSheet diagramSheet;
	private AnalysisSheet analysisSheet;

	public ColorProfiles(String diagram, String analysis, String interactors) {
		diagramSheet = getSheet(DiagramSheetImpl.class, DEFAULT_DIAGRAM_PROFILE, "diagram", diagram);
		analysisSheet = getSheet(AnalysisSheetImpl.class, DEFAULT_ANALYSIS_PROFILE, "analysis", analysis);
		interactorsSheet = getSheet(InteractorsSheetImpl.class, DEFAULT_INTERACTORS_PROFILE, "interactors", interactors);
	}

	private <T> T getSheet(Class<T> clazz, String defaultValue, String prefix, String name) {
		if (name == null)
			name = defaultValue;
		String filename = String.format("%s_%s.json", prefix, name.toLowerCase());
		InputStream resource = getClass().getResourceAsStream(filename);
		if (resource == null) {
			filename = String.format("%s_%s.json", prefix, defaultValue);
			resource = getClass().getResourceAsStream(filename);
		}
		try {
			final String json = IOUtils.toString(resource);
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
