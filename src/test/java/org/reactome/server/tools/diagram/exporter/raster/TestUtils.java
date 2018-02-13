package org.reactome.server.tools.diagram.exporter.raster;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisClient;
import org.reactome.server.tools.diagram.exporter.common.content.ContentServiceClient;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.DiagramRendererTest;
import org.reactome.server.tools.diagram.exporter.raster.ehld.EhldRendererTest;
import org.w3c.dom.svg.SVGDocument;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Set;
import java.util.TreeSet;

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
	private static final File OUTPUT_FOLDER = new File("src/test/resources/org/reactome/server/tools/diagram/exporter/test-images");
	private static final String TODAYS_SERVER = "https://reactomedev.oicr.on.ca";
	private static final String ANALYSIS_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/analysis";
	private static final String DIAGRAM_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/raster/diagram";
	private static final String EHLD_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/raster/ehld";
	private static final TokenUtils TOKEN_UTILS = new TokenUtils(ANALYSIS_PATH);

	private static Set<String> EHLDS;
	// Set to true for visual inspection of tests
	// todo: don't forget to set to false before pushing
	private static final boolean save = false;
	private static final RasterExporter EXPORTER;

	static {
		try {
			EHLDS = new TreeSet<>(IOUtils.readLines(new FileReader("src/test/resources/org/reactome/server/tools/diagram/exporter/svgsummary.txt")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		EXPORTER = new RasterExporter(DIAGRAM_PATH, EHLD_PATH, ANALYSIS_PATH, EHLDS);
		ContentServiceClient.setHost(TODAYS_SERVER);
		ContentServiceClient.setService("/ContentService");
		AnalysisClient.initialise(ANALYSIS_PATH);
		createDir(OUTPUT_FOLDER);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (!save) removeDir(OUTPUT_FOLDER);
		}));
	}

	public static AnalysisStoredResult getResult(String token) {
		return TOKEN_UTILS.getFromToken(token);
	}

	public static String getFileName(RasterArgs args, AnalysisStoredResult result) {
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
			final BufferedImage image = result == null
					? EXPORTER.export(args)
					: EXPORTER.export(args, result);
			if (save) {
				final String filename = getFileName(args, result);
				RasterOutput.save(image, args.getFormat(), new File(OUTPUT_FOLDER, filename));
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	public static void renderGif(RasterArgs args, AnalysisStoredResult result) {
		try {
			final File file = new File(OUTPUT_FOLDER, getFileName(args, result));
			final OutputStream os = new FileOutputStream(file);
			if (result == null)
				EXPORTER.exportToGif(args, os);
			else EXPORTER.exportToGif(args, os, result);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	public static void renderSvg(RasterArgs args, AnalysisStoredResult result) {
		try {
			final SVGDocument document = result == null
					? EXPORTER.exportToSvg(args)
					: EXPORTER.exportToSvg(args, result);
			if (save) {
				final File file = new File(OUTPUT_FOLDER, TestUtils.getFileName(args, result));
				RasterOutput.save(document, file);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

	}
}
