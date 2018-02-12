package org.reactome.server.tools.diagram.exporter.raster;

import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisClient;
import org.reactome.server.tools.diagram.exporter.common.content.ContentServiceClient;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.DiagramRendererTest;
import org.reactome.server.tools.diagram.exporter.raster.ehld.EhldRendererTest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Supporting methods for testing diagram renderering. It is used both in {@link
 * DiagramRendererTest} and {@link EhldRendererTest}. It contains all the common
 * staff, like performing the analysis or creating and deleting the images dir.
 */
public class TestUtils {

	private static final String TODAYS_SERVER = "https://reactomedev.oicr.on.ca";

	public static final String TOKEN_OVER_1 = "MjAxODAyMTIxMTI5MzdfMQ==";
	public static final String TOKEN_OVER_2 = "MjAxODAyMTIxMTMwMTRfMg==";
	public static final String TOKEN_EXPRESSION_1 = "MjAxODAyMTIxMTMwNDhfMw==";
	public static final String TOKEN_EXPRESSION_2 = "MjAxODAyMTIxMTMxMTZfNA==";
	public static final String TOKEN_SPECIES = "MjAxODAyMTIxMTMyMzdfNQ==";

	private static final String tokenPath = "src/test/resources/org/reactome/server/tools/diagram/exporter/analysis";

	private static Map<String, AnalysisStoredResult> cache = new HashMap<>();

	static {
		ContentServiceClient.setHost(TODAYS_SERVER);
		ContentServiceClient.setService("/ContentService");
		AnalysisClient.initialise(tokenPath);
	}

	public static AnalysisStoredResult getResult(String token) {
		return cache.get(token);
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
		final AnalysisStoredResult asr = AnalysisClient.token.getFromToken(token);
		return asr == null ? "UNKNOWN" : asr.getSummary().getType();
		//		for (AnalysisStoredResult summary : cache.values())
//			if (summary.getSummary().getToken().equals(token))
//				return summary.getSummary().getType();
//		return "UNKNOWN";
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
