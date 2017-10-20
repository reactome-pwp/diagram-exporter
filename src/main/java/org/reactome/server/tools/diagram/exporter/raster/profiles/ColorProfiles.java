package org.reactome.server.tools.diagram.exporter.raster.profiles;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.exporter.raster.ColorScheme;
import org.reactome.server.tools.diagram.exporter.raster.color.AnalysisSheet;
import org.reactome.server.tools.diagram.exporter.raster.color.AnalysisSheetImpl;
import org.reactome.server.tools.diagram.exporter.raster.color.DiagramSheet;
import org.reactome.server.tools.diagram.exporter.raster.color.DiagramSheetImpl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ColorProfiles {
	private static final String DEFAULT_DIAGRAM_PROFILE = "modern";
	private static final String DEFAULT_ANALYSIS_PROFILE = "standard";
	private static final String DEFAULT_INTERACTORS_PROFILE = "cyan";
	private DiagramSheet diagramSheet;
	private AnalysisSheet analysisSheet;

	public ColorProfiles(ColorScheme scheme) {
		diagramSheet = getDiagramSheet(scheme.getDiagramProfileName());
		analysisSheet = getAnalysisSheet(scheme.getAnalysisProfileName());
	}

	private AnalysisSheet getAnalysisSheet(String name) {
		if (name == null)
			name = DEFAULT_ANALYSIS_PROFILE;
		String filename = "analysis_" + name.trim().toLowerCase() + ".json";
		InputStream resource = getClass().getResourceAsStream(filename);
		if (resource == null) {
			filename = "diagram_" + DEFAULT_ANALYSIS_PROFILE + ".json";
			resource = getClass().getResourceAsStream(filename);
		}
		try {
			final String json = IOUtils.toString(resource, Charset.defaultCharset());
			final ObjectMapper mapper = new ObjectMapper();
			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
			return mapper.readValue(json, AnalysisSheetImpl.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private DiagramSheet getDiagramSheet(String name) {
		if (name == null)
			name = DEFAULT_DIAGRAM_PROFILE;
		String filename = "diagram_" + name.trim().toLowerCase() + ".json";
		InputStream resource = getClass().getResourceAsStream(filename);
		if (resource == null) {
			filename = "diagram_" + DEFAULT_DIAGRAM_PROFILE + ".json";
			resource = getClass().getResourceAsStream(filename);
		}
		try {
			final String json = IOUtils.toString(resource, Charset.defaultCharset());
			final ObjectMapper mapper = new ObjectMapper();
			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
			return mapper.readValue(json, DiagramSheetImpl.class);
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
}
