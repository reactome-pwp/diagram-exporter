package org.reactome.server.tools.diagram.exporter.raster;


import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reactome.server.tools.diagram.data.DiagramFactory;
import org.reactome.server.tools.diagram.data.exception.DeserializationException;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.graph.GraphNode;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RasterExporterTest {

	private static final String host = "http://reactomerelease.oicr.on.ca/download/current/diagram/";
	private static final File DIAGRAMS_FOLDER = new File("test-diagrams");
	private static final File IMAGES_FOLDER = new File("test-images");
	private static final String MODERN = "modern";

	@BeforeClass
	public static void beforeClass() {
		IMAGES_FOLDER.mkdirs();
		DIAGRAMS_FOLDER.mkdirs();
	}

	@AfterClass
	public static void afterClass() {
		removeDir(IMAGES_FOLDER);
		removeDir(DIAGRAMS_FOLDER);
	}

	private static void removeDir(File dir) {
		final File[] files = dir.listFiles();
		if (files != null)
			for (File file : files)
				if (!file.delete())
					System.err.println("Couldn't delete " + file);
		if (!dir.delete()) {
			System.err.println("Couldn't delete " + dir);
		}
	}

	@Test
	public void testDecorationNormal() {
		final String stId = "R-HSA-169911";// Regulation of apoptosis
		final Graph graph = getGraph(stId);
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
		final List<String> pathways = Arrays.asList(
				"R-HSA-391251",  // Protein folding
				"R-HSA-6796648" // TP53 Regulates Transcription of DNA Repair Genes
		);
		pathways.forEach(stId -> renderToFile(stId, "png", 10, null, MODERN));
	}

	@Test
	public void testStoichiometry() {
		final String stId = "R-HSA-2173782";
		final Graph graph = getGraph(stId);
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
	public void testProfileStandard() {
		final String stId = "R-HSA-2173782";
		renderToFile(stId, "jpg", 1, null, "standard");
	}

	private List<Long> getIdsFor(String prot, Graph graph) {
		// Also parents
		return graph.getNodes().stream()
				.filter(node -> Objects.nonNull(node.getIdentifier()))
				.filter(node -> node.getIdentifier().equals(prot))
				.map(GraphNode::getDbId)
				.collect(Collectors.toList());
	}

	private boolean download(String stId) {
		for (String extension : Arrays.asList(".json", ".graph.json")) {
			final File file = new File(DIAGRAMS_FOLDER, stId + extension);
			if (!file.exists()) {
				try {
					final URL url = new URL(host + stId + extension);
					final ReadableByteChannel rbc = Channels.newChannel(url.openStream());
					final FileOutputStream fos = new FileOutputStream(file);
					fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
					fos.flush();
				} catch (IOException e) {
					return false;
				}
			}
		}
		return true;
	}

	private Graph getGraph(String stId) {
		if (!download(stId))
			return null;
		try {
			final File file = new File(DIAGRAMS_FOLDER, stId + ".graph.json");
			final String json = IOUtils.toString(file.toURI(), Charset.defaultCharset());
			return DiagramFactory.getGraph(json);
		} catch (DeserializationException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void renderToFile(String stId, String ext, int factor, Decorator decorator, String profile) {
		if (!download(stId))
			Assert.fail("Diagram " + stId + " not found");
		try {
			final BufferedImage image = RasterExporter.export(stId, ext, factor, decorator, profile, DIAGRAMS_FOLDER.getAbsolutePath());
			final File file = new File(IMAGES_FOLDER, stId + "." + ext);
			ImageIO.write(image, ext, file);
		} catch (DiagramJsonNotFoundException | DiagramJsonDeserializationException | IOException | DiagramProfileException e) {
			e.printStackTrace();
		}
	}


	private void renderSilent(String stId, String ext, double factor, Decorator decorator, String profile) {
		if (!download(stId))
			Assert.fail("Diagram " + stId + " not found");
		try {
			RasterExporter.export(stId, ext, factor, decorator, profile, DIAGRAMS_FOLDER.getAbsolutePath());
		} catch (DiagramJsonNotFoundException | DiagramProfileException | DiagramJsonDeserializationException e) {
			e.printStackTrace();
		}
	}

}
