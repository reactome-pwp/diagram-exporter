package org.reactome.server.tools.diagram.exporter.raster;


import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.graph.GraphNode;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.common.ResourcesFactory;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDException;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;

public class RasterExporterTest {

	private static final String host = "http://reactomerelease.oicr.on.ca/download/current/diagram/";
	private static final File IMAGES_FOLDER = new File("test-images");
	private static final String MODERN = "modern";

	@BeforeClass
	public static void beforeClass() {
		IMAGES_FOLDER.mkdirs();
		ResourcesFactory.setHost("http://localhost:8080");
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
		final String stId = "R-HSA-169911";// Regulation of apoptosis
		final Graph graph;
		try {
			graph = ResourcesFactory.getGraph(stId);
		} catch (DiagramJsonNotFoundException | DiagramJsonDeserializationException e) {
			Assert.fail(e.getMessage());
			return;
		}
		final List<Long> selected = getIdsFor("A1A4S6", graph);
		final List<Long> analysis = getIdsFor("Q13177", graph);
		final List<Long> flags = getIdsFor("O60313", graph);
		selected.add(211734L);
		final Decorator decorator = new Decorator(flags, selected);
		renderSilent(stId, "png", 1, decorator, MODERN);
	}

	@Test
	public void testDecorationDisease() {
		final String stId = "R-HSA-5602410";
		final List<Long> selected = Collections.singletonList(5602549L);
//		final List<Long> selected = Arrays.asList(5602649L);
		final Decorator decorator = new Decorator(null, selected);
		renderSilent(stId, "png", 1, decorator, MODERN);
	}

	@Test
	public void testJpeg() {
		final List<String> pathways = Arrays.asList(
				"R-HSA-5602410",
				"R-HSA-1362409",  // Mithocondrial iron-sulfur cluster biogenesis
				"R-HSA-169911"  // Regulation of apoptosis
		);
		pathways.forEach(stId -> renderToFile(stId, "jpeg", 1, null, MODERN));
	}

	@Test
	public void testGif() {
		final List<String> pathways = Arrays.asList(
				"R-HSA-169911",  // Regulation of apoptosis
				"R-HSA-68874",  // M/G1 Transition
				"R-HSA-109581"  // Apoptosis
		);
		pathways.forEach(stId -> renderToFile(stId, "gif", 1, null, MODERN));
	}

	@Test
	public void testHighQuality() {
		final String stId = "R-HSA-391251";
		renderToFile(stId, "png", 10, null, MODERN);
	}

	@Test
	public void testStoichiometry() {
		final String stId = "R-HSA-2173782";
		final Graph graph;
		try {
			graph = ResourcesFactory.getGraph(stId);
		} catch (DiagramJsonNotFoundException | DiagramJsonDeserializationException e) {
			Assert.fail(e.getMessage());
			return;
		}
		final List<Long> ids = getIdsFor("R-HSA-2168880", graph);
		final Decorator decorator = new Decorator(null, ids);
		renderSilent(stId, "png", 1, decorator, MODERN);
	}

	@Test
	public void testLowQuality() {
		final String stId = "R-HSA-2173782";
		renderSilent(stId, "png", 0.1, null, MODERN);
	}

	@Test
	public void testVeryHugeQuality() {
		final String stId = "R-HSA-2173782";
		renderToFile(stId, "jpg", 10000, null, MODERN);
	}

	@Test
	public void testStandardProfile() {
		final String stId = "R-HSA-2173782";
		renderToFile(stId, "jpg", 1, null, "standard");
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

	private void renderToFile(String stId, String ext, double factor, Decorator decorator, String profile) {
		try {
			final RasterArgs args = new RasterArgs(stId, ext);
			args.setFactor(factor);
			args.setDecorator(decorator);
			args.setProfiles(new ColorProfiles(profile, "standard", "cyan"));
			final BufferedImage image = RasterExporter.export(args);
			final File file = new File(IMAGES_FOLDER, stId + "." + ext);
			ImageIO.write(image, ext, file);
		} catch (DiagramJsonNotFoundException | DiagramJsonDeserializationException | IOException e) {
			e.printStackTrace();
		} catch (EHLDException e) {
			e.printStackTrace();
		}
	}

	private void renderSilent(String stId, String ext, double factor, Decorator decorator, String profile) {
		try {
			final RasterArgs args = new RasterArgs(stId, ext);
			args.setFactor(factor);
			args.setDecorator(decorator);
			args.setProfiles(new ColorProfiles(profile, "standard", "cyan"));
			RasterExporter.export(args);
		} catch (DiagramJsonNotFoundException | DiagramJsonDeserializationException e) {
			e.printStackTrace();
		} catch (EHLDException e) {
			e.printStackTrace();
		}
	}

}
