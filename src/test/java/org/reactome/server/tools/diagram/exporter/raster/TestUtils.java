package org.reactome.server.tools.diagram.exporter.raster;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisClient;
import org.reactome.server.tools.diagram.exporter.common.analysis.ContentServiceClient;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisSummary;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.DiagramRendererTest;
import org.reactome.server.tools.diagram.exporter.raster.ehld.EhldRendererTest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Supporting methods for testing diagram renderering. It is used both in {@link
 * DiagramRendererTest} and {@link EhldRendererTest}. It contains all the common
 * staff, like performing the analysis or creating and deleting the images dir.
 */
public class TestUtils {

	private static final String TODAYS_SERVER = "https://reactomedev.oicr.on.ca";

	private static Map<String, AnalysisSummary> cache = new HashMap<>();

	static {
		AnalysisClient.setServer(TODAYS_SERVER);
		AnalysisClient.setService("/AnalysisService");
		ContentServiceClient.setHost(TODAYS_SERVER);
		ContentServiceClient.setService("/ContentService");
	}

	public static String createSpeciesComparisonToken(String species) {
		if (cache.containsKey(species))
			return cache.get(species).getToken();
		try {
			final AnalysisSummary summary = AnalysisClient.performSpeciesComparison(species).getSummary();
			cache.put(species, summary);
			return summary.getToken();
		} catch (AnalysisException | AnalysisServerError e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		return null;
	}

	public static String performAnalysis(String resource) {
		if (cache.containsKey(resource)) return cache.get(resource).getToken();
		try {
			final String query = IOUtils.toString(TestUtils.class.getResourceAsStream(resource), Charset.defaultCharset());
			final AnalysisSummary summary = AnalysisClient.performAnalysis(query).getSummary();
			cache.put(resource, summary);
			return summary.getToken();
		} catch (AnalysisServerError | IOException | AnalysisException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		return null;
	}

	public static String getFileName(RasterArgs args) {
		String decorators = "";
		if (args.getSelected() != null && !args.getSelected().isEmpty())
			decorators += "s" + args.getSelected().size();
		if (args.getFlags() != null && !args.getFlags().isEmpty())
			decorators += "f" + args.getFlags().size();
		if (!decorators.isEmpty())
			decorators = "_" + decorators;
		String analysis = "";
		if (args.getToken() != null)
			analysis += "_" + getAnalysisType(args.getToken());
		return String.format("%s_%s_%s%s%s.%s",
				args.getStId(),
				args.getQuality(),
				args.getProfiles().getDiagramSheet().getName(),
				decorators,
				analysis,
				args.getFormat());
	}

	private static String getAnalysisType(String token) {
		if (token == null) return "NONE";
		for (AnalysisSummary summary : cache.values())
			if (summary.getToken().equals(token))
				return summary.getType();
		return "UNKNOWN";
	}

	public static void createDir(File file) {
		if (!file.exists() && !file.mkdirs())
			System.err.println("Couldn't create dir " + file);
	}

	public static void removeDir(File dir) {
		if (!dir.exists()) return;
		final File[] files = dir.listFiles();
		if (files != null)
			for (File file : files)
				if (!file.delete())
					System.err.println("Couldn't delete " + file);
		if (!dir.delete())
			System.err.println("Couldn't delete " + dir);
	}
}
