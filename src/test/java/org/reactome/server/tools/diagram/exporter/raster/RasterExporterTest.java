package org.reactome.server.tools.diagram.exporter.raster;


import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisClient;
import org.reactome.server.tools.diagram.exporter.common.analysis.ContentServiceClient;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisResult;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.DiagramRenderer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

public class RasterExporterTest {

	// TODO: expression/enrichment + selecting non hit element

	private static final File IMAGES_FOLDER = new File("test-images");
	private static final String DIAGRAM_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/raster/diagram";

	// Set to true for visual inspection of tests
	private static final boolean save = true;

	private static String ENRICHMENT_TOKEN;
	private static String EXPRESSION_TOKEN;
	private static String SPECIES_TOKEN;

	@BeforeClass
	public static void beforeClass() {
		initAnalysisTokens();
		createImagesFolder();
	}

	private static void createImagesFolder() {
		if (!IMAGES_FOLDER.exists() && !IMAGES_FOLDER.mkdirs())
			System.err.println("Couldn't create dir for testing " + IMAGES_FOLDER);
	}

	private static void initAnalysisTokens() {
		AnalysisClient.setServer("https://reactomedev.oicr.on.ca");
		AnalysisClient.setService("/AnalysisService");
		ContentServiceClient.setHost("https://reactomedev.oicr.on.ca");
		ContentServiceClient.setService("/ContentService");
		try {
			SPECIES_TOKEN = createSpeciesComparisonToken();
			ENRICHMENT_TOKEN = createEnrichmentToken();
			EXPRESSION_TOKEN = createExpressionToken();
		} catch (AnalysisException | AnalysisServerError | IOException e) {
			e.printStackTrace();
		}
	}

	private static String createSpeciesComparisonToken() throws AnalysisException, AnalysisServerError {
		return AnalysisClient.performSpeciesComparison("48898").getSummary().getToken();
	}

	private static String createEnrichmentToken() throws IOException, AnalysisServerError, AnalysisException {
		final String query = IOUtils.toString(RasterExporterTest.class.getResourceAsStream("enrichment_data.txt"), Charset.defaultCharset());
		final AnalysisResult result = AnalysisClient.performAnalysis(query);
		return result.getSummary().getToken();
	}

	private static String createExpressionToken() throws IOException, AnalysisException, AnalysisServerError {
		final String query = IOUtils.toString(RasterExporterTest.class.getResourceAsStream("expression_data.txt"), Charset.defaultCharset());
		final AnalysisResult result = AnalysisClient.performAnalysis(query);
		return result.getSummary().getToken();
	}

	@AfterClass
	public static void afterClass() {
		if (!save) removeDir(IMAGES_FOLDER);
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

	@Test
	public void testSimpleDiagram() {
		// These diagrams contain all the types of nodes:
		// Chemical, Complex, Entity, EntitySet, Gene, ProcessNode, Protein and RNA
		final List<String> stIds = Arrays.asList("R-HSA-5687128", "R-HSA-376176", "R-HSA-69620");
		final List<String> formats = Arrays.asList("PNG", "jpg", "Gif");
		for (String stId : stIds)
			for (String format : formats)
				render(new RasterArgs(stId, format));
	}

	@Test
	public void testQuality() {
		IntStream.range(1, 11)
				.forEach(quality -> {
					final RasterArgs args = new RasterArgs("R-HSA-376176", "jpg");
					args.setQuality(quality);
					render(args);
				});
	}

	@Test
	public void testSelectNodes() {
		RasterArgs args = new RasterArgs("R-HSA-5687128", "png");
		// EntitySet, Protein, Chemical
		args.setSelected(Arrays.asList("R-HSA-5692706", "R-HSA-5687026", "R-ALL-29358"));
		render(args);

		args = new RasterArgs("R-HSA-376176", "png");
		// Gene, Complex, ProcessNode
		args.setSelected(Arrays.asList("R-HSA-9010561", "R-HSA-428873", "R-HSA-5627117"));
		render(args);
		// FIXME: the arrow in gene is not filled with selection color
		// FIXME: inner rectangle of ProcessNode is not drawn with selection color

		args = new RasterArgs("R-HSA-69620", "png");
		// RNA, Entity
		args.setSelected(Arrays.asList("R-HSA-6803386", "R-ALL-176104"));
		render(args);
	}

	@Test
	public void testSelectReactions() {
		RasterArgs args = new RasterArgs("R-HSA-982772", "png");
		// Reactions: Transition, Omitted Process, Uncertain, Association
		// Connectors: filled stop
		args.setSelected(Arrays.asList("R-HSA-1168423", "R-HSA-1168459", "R-HSA-982810", "R-HSA-982775"));
		render(args);
		// REPORT: stoichiometry black in PathwayBrowser

		args = new RasterArgs("R-HSA-877300", "png");
		// Reactions: Association, Dissociation
		// Connectors: empty circle, filled arrow, empty arrow
		args.setSelected(Arrays.asList("R-HSA-877269", "R-HSA-873927"));
		render(args);
		// FIXME: dissociation circumferences merge
	}

	@Test
	public void testFlagNodes() {
		RasterArgs args = new RasterArgs("R-HSA-5687128", "png");
		// EntitySet, Protein, Chemical
		args.setFlags(Arrays.asList("R-HSA-5692706", "R-HSA-5687026", "R-ALL-29358"));
		render(args);
		// FIXME: chemical flag does not work with name

		args = new RasterArgs("R-HSA-376176", "png");
		// Gene, Complex, ProcessNode
		args.setFlags(Arrays.asList("R-HSA-9010561", "R-HSA-428873", "R-HSA-5627117"));
		render(args);
		// REPORT: ProcessNode not flagged in PathwayBrowser

		args = new RasterArgs("R-HSA-69620", "png");
		// RNA, Entity
		args.setFlags(Arrays.asList("R-HSA-6803386", "R-ALL-176104"));
		render(args);
	}

	@Test
	public void testDiseases() {
		RasterArgs args = new RasterArgs("R-HSA-162587", "png");
		// Entity, RNA, EntitySet, Complex
		args.setSelected(Arrays.asList("R-HIV-165543", "R-HIV-173808", "R-HIV-173120", "R-HSA-167286"));
		args.setFlags(Arrays.asList("R-HIV-165543", "R-HIV-173808", "R-HIV-173120", "R-HSA-167286"));

		render(args);
		args = new RasterArgs("R-HSA-5467343", "png");
		// Gene
		args.setSelected(Collections.singletonList("R-HSA-5251547"));
		render(args);
	}

	@Test
	public void testDiagramProfiles() {
		final String stId = "R-HSA-5687128";
		final List<String> diagramProfiles = Arrays.asList("Standard", "MODERN", "not valid");
		final List<String> formats = Arrays.asList("png", "jpeg", "gif");
		for (String diagramProfile : diagramProfiles) {
			for (String format : formats) {
				RasterArgs args = new RasterArgs(stId, format);
				args.setProfiles(new ColorProfiles(diagramProfile, null, null));
				render(args);
			}
		}
	}

	@Test
	public void testSpeciesComparison() {
		RasterArgs args = new RasterArgs("R-HSA-5687128", "png");
		args.setToken(SPECIES_TOKEN);
		args.setWriteTitle(true);
		render(args);
	}

	@Test
	public void testEnrichment() {
		RasterArgs args = new RasterArgs("R-HSA-69620", "png");
		args.setToken(ENRICHMENT_TOKEN);
		args.setWriteTitle(true);
		render(args);
	}

	@Test
	public void testExpression() {
		RasterArgs args = new RasterArgs("R-HSA-69620", "png");
		args.setToken(EXPRESSION_TOKEN);
		args.setWriteTitle(true);
		render(args);
		// FIXME: attachments color!!
	}

	@Test
	public void testAnimatedGif() {
		final ColorProfiles profiles = new ColorProfiles("modern", "copper plus", "teal");
		final RasterArgs args = new RasterArgs("R-HSA-109606", "gif");
		args.setSelected(Collections.singletonList("R-HSA-114255"));
		args.setToken(EXPRESSION_TOKEN);
		args.setProfiles(profiles);
		renderGif(args);
	}

	private void render(RasterArgs args) {
		try {
			final DiagramRenderer renderer = new DiagramRenderer(args, DIAGRAM_PATH);
			final BufferedImage image = renderer.render();
			if (save) {
				final String filename = getFileName(args);
				ImageIO.write(image, args.getFormat(), new File(IMAGES_FOLDER, filename));
			}
		} catch (DiagramJsonNotFoundException | DiagramJsonDeserializationException | AnalysisServerError | AnalysisException | IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	private void renderGif(RasterArgs args) {
		try {
			final DiagramRenderer renderer = new DiagramRenderer(args, DIAGRAM_PATH);
			final File file = new File(IMAGES_FOLDER, getFileName(args));
			final OutputStream os = new FileOutputStream(file);
			renderer.renderToAnimatedGif(os);
		} catch (IOException | DiagramJsonNotFoundException | DiagramJsonDeserializationException | AnalysisException | AnalysisServerError e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	private String getFileName(RasterArgs args) {
		return String.format("%s_%s_%s_%s.%s",
				args.getStId(),
				args.getQuality(),
				args.getProfiles().getDiagramSheet().getName(),
				getAnalysisType(args.getToken()),
				args.getFormat());
	}

	private String getAnalysisType(String token) {
		if (token == null) return "none";
		if (token.equals(ENRICHMENT_TOKEN))
			return "enrichment";
		if (token.equals(EXPRESSION_TOKEN))
			return "expression";
		if (token.equals(SPECIES_TOKEN))
			return "species";
		return "unknown";
	}

	@Test
	public void testLegendFormat() {
		final double max = 2.9;
		final DecimalFormat NF = new DecimalFormat("#.##E0", DecimalFormatSymbols.getInstance(Locale.UK));
		Assert.assertEquals("2.9E0", NF.format(max));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testScaleLimits() {
		final int factor = 0;
		final RasterArgs args = new RasterArgs("stid", "png");
		args.setQuality(factor);
	}

}
