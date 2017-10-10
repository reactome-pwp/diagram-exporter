package org.reactome.server.tools.diagram.exporter.raster;


import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
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

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RasterExporterTest {

	private static final String host = "http://reactomerelease.oicr.on.ca/download/current/diagram/";
	private static final File DIAGRAMS_FOLDER = new File("test-diagrams");
	private static final File IMAGES_FOLDER = new File("test-images");

	@BeforeClass
	public static void beforeClass() {
		// Create the output stream
		IMAGES_FOLDER.mkdirs();
		DIAGRAMS_FOLDER.mkdirs();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			removeDir(IMAGES_FOLDER);
			removeDir(DIAGRAMS_FOLDER);
		}));
	}

	private static void removeDir(File dir) {
		final File[] files = dir.listFiles();
		if (files != null)
			for (File file : files)
				file.delete();
		dir.delete();
	}

	@Test
	public void testDecorationNormal() {
		final String stId = "R-HSA-169911";// Regulation of apoptosis
		final Graph graph = getGraph(stId);
		final List<Long> selected = getIdsFor("A1A4S6", graph);
		final List<Long> analysis = getIdsFor("Q13177", graph);
		final List<Long> flags = getIdsFor("O60313", graph);
		selected.add(211734L);
		final Decorator decorator = new Decorator(flags, selected, analysis);
		new RendererInvoker(stId)
				.setDecorator(decorator)
				.setSave(true)
				.render();
	}

	@Test
	public void testDecorationDisease() {
		final String stId = "R-HSA-5602410";
		final List<Long> selected = Arrays.asList(5602549L);
//		final List<Long> selected = Arrays.asList(5602649L);
		final Decorator decorator = new Decorator(null, selected, null);
		new RendererInvoker(stId)
				.setDecorator(decorator)
				.setSave(true)
				.render();
	}

	@Test
	public void testJpeg() {
		final List<String> pathways = Arrays.asList(
				"R-HSA-5602410",
				"R-HSA-1362409",  // Mithocondrial iron-sulfur cluster biogenesis
				"R-HSA-169911",  // Regulation of apoptosis
				"R-HSA-68874",  // M/G1 Transition
				"R-HSA-109581"  // Apoptosis
		);
		pathways.forEach(stId ->
				new RendererInvoker(stId)
						.setSave(true)
						.setFormat("jpeg")
						.render());
	}

	@Test
	public void testGif() {
		final List<String> pathways = Arrays.asList(
				"R-HSA-5602410",
				"R-HSA-1362409",  // Mithocondrial iron-sulfur cluster biogenesis
				"R-HSA-169911",  // Regulation of apoptosis
				"R-HSA-68874",  // M/G1 Transition
				"R-HSA-109581"  // Apoptosis
		);
		pathways.forEach(stId ->
				new RendererInvoker(stId)
						.setSave(true)
						.setFormat("gif")
						.render());
	}

	@Test
	public void testHighQuality() {
		final List<String> pathways = Arrays.asList(
				"R-HSA-391251",  // Protein folding
				"R-HSA-6796648" // TP53 Regulates Transcription of DNA Repair Genes
		);
		pathways.forEach(stId ->
				new RendererInvoker(stId)
						.setSave(true)
						.setFactor(10)
						.setFormat("jpeg")
						.render());
	}

	@Test
	public void testStoichiometry() {
		final String stId = "R-HSA-2173782";
		final Graph graph = getGraph(stId);
		final List<Long> ids = getIdsFor("R-HSA-2168880", graph);
		final Decorator decorator = new Decorator(null, ids);
		new RendererInvoker(stId)
				.setDecorator(decorator)
				.setSave(true)
				.render();
	}

	@Test
	public void testLowQuality() {
		final String stId = "R-HSA-2173782";
		new RendererInvoker(stId)
				.setSave(true)
				.setFactor(0.1)
				.render();
	}

	@Test
	public void testVeryHugeQuality() {
		final String stId = "R-HSA-2173782";
		new RendererInvoker(stId)
				.setSave(true)
				.setFactor(10000)
				.render();
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

	private class RendererInvoker {
		private String stId;
		private String format = "png";
		private Decorator decorator = null;
		private boolean save = false;
		private double factor = 1;

		RendererInvoker(String stId) {
			this.stId = stId;
		}

		/**
		 * default is png
		 */
		RendererInvoker setFormat(String format) {
			this.format = format;
			return this;
		}

		/**
		 * Default is null
		 */
		RendererInvoker setDecorator(Decorator decorator) {
			this.decorator = decorator;
			return this;
		}

		/**
		 * default is 1
		 */
		RendererInvoker setFactor(double factor) {
			this.factor = factor;
			return this;
		}

		/**
		 * Default is false
		 */
		RendererInvoker setSave(boolean save) {
			this.save = save;
			return this;
		}

		/**
		 * This method is a real use case from the server side.
		 */
		void render() {
			// Put json files in local
			if (!download(stId)) return;
			try {
//				System.out.println(stId);
				final OutputStream outputStream = save
						? new BufferedOutputStream(new FileOutputStream(new File(IMAGES_FOLDER, stId + "." + format)))
						: NullOutputStream.NULL_OUTPUT_STREAM;
				// Call to service
				RasterExporter.export(stId, DIAGRAMS_FOLDER.getAbsolutePath(),
						"modern", decorator, format, outputStream, factor);
				outputStream.flush();
				outputStream.close();
				System.gc();
			} catch (DiagramJsonNotFoundException | DiagramProfileException
					| DiagramJsonDeserializationException | IOException e) {
				e.printStackTrace();
			}
		}

	}
}
