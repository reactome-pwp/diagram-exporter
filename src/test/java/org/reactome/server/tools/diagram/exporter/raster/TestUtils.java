package org.reactome.server.tools.diagram.exporter.raster;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Assert;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.content.ContentServiceClient;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.DiagramRendererTest;
import org.reactome.server.tools.diagram.exporter.raster.ehld.EhldRendererTest;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EhldException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Supporting methods for testing diagram renderering. It is used both in {@link
 * DiagramRendererTest} and {@link EhldRendererTest}. It contains all the common
 * staff, like performing the analysis or creating and deleting the images dir.
 */
public class TestUtils {

	public static final String TOKEN_OVER_1 = "MjAxODAyMTIxMTI5MzdfMQ==";
	public static final String TOKEN_OVER_2 = "MjAxODAyMTIxMTMwMTRfMg==";
	public static final String TOKEN_EXPRESSION_1 = "MjAxODAyMTIxMTMwNDhfMw==";
	public static final String TOKEN_EXPRESSION_2 = "MjAxODAyMTIxMTMxMTZfNA==";
	public static final String TOKEN_SPECIES = "MjAxODAyMTIxMTMyMzdfNQ==";
	private static final File OUTPUT_FOLDER = new File("test-images");
	private static final String TODAYS_SERVER = "https://reactomedev.oicr.on.ca";
	private static final String ANALYSIS_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/analysis";
	private static final String DIAGRAM_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/diagram";
	private static final String EHLD_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/ehld";
	private static final String SVG_SUMMARY = "src/test/resources/org/reactome/server/tools/diagram/exporter/svgsummary.txt";
	private static final TokenUtils TOKEN_UTILS = new TokenUtils(ANALYSIS_PATH);

	// Set to true for visual inspection of tests
	private static final boolean SAVE = Arrays.asList("yes", "y", "true", "ok").contains(System.getProperty("test.save", "false").toLowerCase());
	private static final RasterExporter EXPORTER;

	static {
		EXPORTER = new RasterExporter(DIAGRAM_PATH, EHLD_PATH, ANALYSIS_PATH, SVG_SUMMARY);
		ContentServiceClient.setHost(TODAYS_SERVER);
		ContentServiceClient.setService("/ContentService");
		createDir(OUTPUT_FOLDER);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (!SAVE) removeDir(OUTPUT_FOLDER);
		}));
	}

	public static AnalysisStoredResult getResult(String token) {
		return TOKEN_UTILS.getFromToken(token);
	}

	private static String getFileName(RasterArgs args, AnalysisStoredResult result) {
		String decorators = "";
		if (args.getSelected() != null && !args.getSelected().isEmpty())
			decorators += "s" + args.getSelected().size();
		if (args.getFlags() != null && !args.getFlags().isEmpty())
			decorators += "f" + args.getFlags().size();
		if (!decorators.isEmpty())
			decorators = "_" + decorators;
		String analysis = "";
		if (result != null) analysis += "_" + result.getSummary().getType();
		else if (args.getToken() != null)
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
		final AnalysisStoredResult result = getResult(token);
		return result == null ? "UNKNOWN" : result.getSummary().getType();
	}

	private static void createDir(File file) {
		if (!file.exists() && !file.mkdirs())
			System.err.println("Couldn't create dir " + file);
	}

	private static void removeDir(File dir) {
		if (!dir.exists()) return;
		final File[] files = dir.listFiles();
		if (files != null)
			for (File file : files)
				if (!file.delete())
					System.err.println("Couldn't delete " + file);
		if (!dir.delete())
			System.err.println("Couldn't delete " + dir);
	}

	public static void render(RasterArgs args, AnalysisStoredResult result) {
		try {
			final OutputStream os = SAVE
					? new FileOutputStream(new File(OUTPUT_FOLDER, getFileName(args, result)))
					: new NullOutputStream();
			EXPORTER.export(args, os, result);
		} catch (EhldException | AnalysisException | DiagramJsonDeserializationException | DiagramJsonNotFoundException | TranscoderException | IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

}
