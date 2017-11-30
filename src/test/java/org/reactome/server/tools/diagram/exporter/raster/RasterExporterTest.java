package org.reactome.server.tools.diagram.exporter.raster;


import org.junit.*;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.graph.GraphNode;
import org.reactome.server.tools.diagram.exporter.common.ResourcesFactory;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisClient;
import org.reactome.server.tools.diagram.exporter.common.analysis.ContentServiceClient;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class RasterExporterTest {

	private static final File IMAGES_FOLDER = new File("test-images");
	private static final String MODERN = "modern";
	private static final String DIAGRAM_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/raster/diagram";
	private static final String EHLD_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/raster/ehld";

	@BeforeClass
	public static void beforeClass() {
		AnalysisClient.setServer("https://reactomedev.oicr.on.ca");
		AnalysisClient.setService("/AnalysisService");
		ContentServiceClient.setHost("https://reactomedev.oicr.on.ca");
		ContentServiceClient.setService("/ContentService");
		if (!IMAGES_FOLDER.mkdirs())
			System.err.println("Couldn't create dir for testing " + IMAGES_FOLDER);
	}

	@AfterClass
	public static void afterClass() {
		removeDir(IMAGES_FOLDER);
	}

	private static void removeDir(File dir) {
		final File[] files = dir.listFiles();
		if (files != null)
			for (File file : files)
				if (!file.delete())
					System.err.println("Couldn't delete " + file);
		if (!dir.delete())
			System.err.println("Couldn't delete " + dir);
	}

	// TODO: simple -> Chemical, Complex, Entity, EntitySet, Gene, ProcessNode, Protein and RNA
	// TODO: selection -> reaction
	// TODO: selection -> Chemical, Complex, Entity, EntitySet, Gene, ProcessNode, Protein and RNA
	// TODO: flag -> Chemical, Complex, Entity, EntitySet, Gene, ProcessNode, Protein and RNA

	// TODO: profiles -> no standard/null/wrong

	// TODO: enrichment -> Chemical, Complex, Entity, EntitySet, Gene, ProcessNode, Protein and RNA
	// TODO: species comparison -> Chemical, Complex, Entity, EntitySet, Gene, ProcessNode, Protein and RNA
	// TODO: expression -> Chemical, Complex, Entity, EntitySet, Gene, ProcessNode, Protein and RNA

	// TODO: expression/enrichment + selecting non hit element

	@Test
	public void  testSimpleDiagram() {
		// These 3 diagrams contain all the types of nodes:
		// Chemical, Complex, Entity, EntitySet, Gene, ProcessNode, Protein and RNA
		final List<String> stIds = Arrays.asList(
				"R-HSA-5687128", "R-HSA-376176", "R-HSA-69620");
		for (String stId : stIds) {
			final RasterArgs args = new RasterArgs(stId, "png");
		}
	}

	@Test
	public void testDecorationNormal() {
		final String stId = "R-HSA-169911"; // Regulation of apoptosis
		try {
			final Graph graph = ResourcesFactory.getGraph(DIAGRAM_PATH, stId);
			final List<String> selected = getIdsFor("A1A4S6", graph).stream()
					.map(String::valueOf).collect(Collectors.toList());
//			final List<Long> analysis = getIdsFor("Q13177", graph);
			final List<String> flags = getIdsFor("O60313", graph).stream()
					.map(String::valueOf).collect(Collectors.toList());
			selected.add("211734");
			renderSilent(stId, "png", 1, selected, flags, MODERN);
		} catch (DiagramJsonDeserializationException | DiagramJsonNotFoundException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testDecorationDisease() {
		final String stId = "R-HSA-5602410";
		final List<String> selected = Arrays.asList("5602549");
//		final List<Long> selected = Arrays.asList(5602649L);
		renderSilent(stId, "png", 1, selected, null, MODERN);
	}

	@Test
	public void testJpeg() {
		final List<String> pathways = Arrays.asList(
				"R-HSA-5602410",
				"R-HSA-1362409",  // Mithocondrial iron-sulfur cluster biogenesis
				"R-HSA-169911"  // Regulation of apoptosis
		);
		pathways.forEach(stId -> renderToFile(stId, "jpeg", 1, null, null, MODERN));
	}

	@Test
	public void testGif() {
		final List<String> pathways = Arrays.asList(
				"R-HSA-169911",  // Regulation of apoptosis
				"R-HSA-68874",  // M/G1 Transition
				"R-HSA-109581"  // Apoptosis
		);
		pathways.forEach(stId -> renderToFile(stId, "gif", 1, null, null, MODERN));
	}

	@Test
	public void testHighQuality() {
		final String stId = "R-HSA-391251";
		renderToFile(stId, "png", 3, null, null, MODERN);
	}

	@Test
	public void testStoichiometry() {
		try {
			final String stId = "R-HSA-2173782";
			final Graph graph = ResourcesFactory.getGraph(DIAGRAM_PATH, stId);
			final List<String> ids = getIdsFor("R-HSA-2168880", graph).stream()
					.map(String::valueOf).collect(Collectors.toList());
			renderSilent(stId, "png", 1, ids, null, MODERN);
		} catch (DiagramJsonDeserializationException | DiagramJsonNotFoundException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testLowQuality() {
		final String stId = "R-HSA-2173782";
		renderSilent(stId, "png", 1, null, null, MODERN);
	}

	@Test
	@Ignore("Renderers do not limit diagram size anymore")
	public void testVeryHugeQuality() {
		final String stId = "R-HSA-2173782";
		renderToFile(stId, "jpg", 10000, null, null, MODERN);
	}

	@Test
	public void testStandardProfile() {
		final String stId = "R-HSA-2173782";
		renderToFile(stId, "jpg", 1, null, null, "standard");
	}

	@Test
	public void testLegendFormat() {
		final double max = 2.9;
		final DecimalFormat NF = new DecimalFormat("#.##E0", DecimalFormatSymbols.getInstance(Locale.UK));
		Assert.assertEquals("2.9E0", NF.format(max));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testScaleLImit() {
		final int factor = 0;
		final RasterArgs args = new RasterArgs("stid", "png");
		args.setQuality(factor);
	}

	private List<Long> getIdsFor(String prot, Graph graph) {
		// Also parents
		return graph.getNodes().stream()
				.filter(node -> Objects.nonNull(node.getIdentifier()))
				.filter(node -> node.getIdentifier().equals(prot))
				.map(GraphNode::getDbId)
				.collect(Collectors.toList());
	}

	private void renderToFile(String stId, String ext, int factor, List<String> selected, List<String> flags, String profile) {
		try {
			final RasterArgs args = new RasterArgs(stId, ext);
			args.setQuality(factor);
			args.setSelected(selected);
			args.setFlags(flags);
			args.setProfiles(new ColorProfiles(profile, "standard", "cyan"));
			final BufferedImage image = RasterExporter.export(args, DIAGRAM_PATH, EHLD_PATH);
			final File file = new File(IMAGES_FOLDER, stId + "." + ext);
			ImageIO.write(image, ext, file);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	private void renderSilent(String stId, String ext, int factor, List<String> selected, List<String> flags, String profile) {
		try {
			final RasterArgs args = new RasterArgs(stId, ext);
			args.setQuality(factor);
			args.setSelected(selected);
			args.setFlags(flags);
			args.setProfiles(new ColorProfiles(profile, "standard", "cyan"));
			RasterExporter.export(args, DIAGRAM_PATH, EHLD_PATH);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
}
