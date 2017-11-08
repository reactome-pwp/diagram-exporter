package org.reactome.server.tools.diagram.exporter.raster;


import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.graph.GraphNode;
import org.reactome.server.tools.diagram.exporter.common.ResourcesFactory;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.api.SimpleRasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDException;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class RasterExporterTest {

//	private static final String host = "http://reactomerelease.oicr.on.ca/download/current/diagram/";

	private static final File IMAGES_FOLDER = new File("test-images");
	private static final String MODERN = "modern";
	private static final String DIAGRAM_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/raster/diagram";
	private static final String EHLD_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/raster/ehld";

	@BeforeClass
	public static void beforeClass() {
		IMAGES_FOLDER.mkdirs();
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

	@Test
	public void testDecorationNormal() {
		final String stId = "R-HSA-169911"; // Regulation of apoptosis
		try {
			final Graph graph = ResourcesFactory.getGraph(DIAGRAM_PATH, stId);
			final List<String> selected = getIdsFor("A1A4S6", graph).stream()
					.map(String::valueOf).collect(Collectors.toList());
//			final List<Long> analysis = getIdsFor("Q13177", graph);
			final List<String> flags = getIdsFor("O60313", graph).stream()
					.map(String::valueOf).collect(Collectors.toList());;
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
		pathways.forEach(stId -> renderToFile(stId, "jpeg", 1,null, null, MODERN));
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
		renderToFile(stId, "png", 10, null, null, MODERN);
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
		renderSilent(stId, "png", 0.1, null, null, MODERN);
	}

	@Test
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
		System.out.println(NF.format(max));
	}

	private List<Long> getIdsFor(String prot, Graph graph) {
		// Also parents
		return graph.getNodes().stream()
				.filter(node -> Objects.nonNull(node.getIdentifier()))
				.filter(node -> node.getIdentifier().equals(prot))
				.map(GraphNode::getDbId)
				.collect(Collectors.toList());
	}

	private void renderToFile(String stId, String ext, double factor, List<String> selected, List<String> flags, String profile) {
		try {
			final SimpleRasterArgs args = new SimpleRasterArgs(stId, ext);
			args.setFactor(factor);
			args.setSelected(selected);
			args.setFlags(flags);
			args.setProfiles(new ColorProfiles(profile, "standard", "cyan"));
			final BufferedImage image = RasterExporter.export(args, DIAGRAM_PATH, EHLD_PATH);
			final File file = new File(IMAGES_FOLDER, stId + "." + ext);
			ImageIO.write(image, ext, file);
		} catch (DiagramJsonNotFoundException | DiagramJsonDeserializationException | IOException | EHLDException e) {
			Assert.fail(e.getMessage());
		}
	}

	private void renderSilent(String stId, String ext, double factor, List<String> selected, List<String> flags, String profile) {
		try {
			final SimpleRasterArgs args = new SimpleRasterArgs(stId, ext);
			args.setFactor(factor);
			args.setSelected(selected);
			args.setFlags(flags);
			args.setProfiles(new ColorProfiles(profile, "standard", "cyan"));
			RasterExporter.export(args, DIAGRAM_PATH, EHLD_PATH);
		} catch (DiagramJsonNotFoundException | DiagramJsonDeserializationException | EHLDException e) {
			Assert.fail(e.getMessage());
		}
	}

}
