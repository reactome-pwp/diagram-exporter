package org.reactome.server.tools.diagram.exporter.raster;


import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
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
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
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
	private static final File DIAGRAMS_FOLDER = new File("/media/pascual/Disco1TB/reactome/diagram");
	private static final File IMAGES_FOLDER = new File("/media/pascual/Disco1TB/reactome/images");

	@Test
	public void testPerformance() {
		final List<String> pathways = getLines(new File("all-pathways.txt"));
		if (pathways != null) {
			Collections.shuffle(pathways);
			// Warm up with 5 diagrams
			pathways.stream()
					.filter(this::exists)
					.limit(5)
					.forEach(s -> new RendererInvoker(s)
							.setDebug(false)
							.render());
			pathways.stream()
					.filter(this::exists)
					.limit(1000)
					.forEach(s -> new RendererInvoker(s).render());
		}
	}

	@Test
	public void testPerformanceMaxResolution() {
		final double maxResolution = 10;
		final List<String> pathways = getLines(new File("all-pathways.txt"));
		if (pathways != null) {
			Collections.shuffle(pathways);
			// Warm up with 5 diagrams
			pathways.stream()
					.filter(this::exists)
					.limit(5)
					.forEach(s -> new RendererInvoker(s)
							.setDebug(false)
							.setFactor(maxResolution)
							.render());
			pathways.stream()
					.filter(this::exists)
					.limit(200)
					.forEach(s -> new RendererInvoker(s)
							.setDebug(true)
							.setFactor(maxResolution)
							.render());
		}
	}

	private List<String> getLines(File file) {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			return reader.lines().collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean exists(String stId) {
		if (new File(DIAGRAMS_FOLDER, stId + ".json").exists())
			return true;
		try {
			HttpURLConnection.setFollowRedirects(false);
			// note : you may also need
			//        HttpURLConnection.setInstanceFollowRedirects(false)
			HttpURLConnection con =
					(HttpURLConnection) new URL(host + stId + ".json").openConnection();
			con.setRequestMethod("HEAD");
			return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
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
		final Decorator decorator = new Decorator(flags, selected, analysis);
		new RendererInvoker(stId)
				.setDecorator(decorator)
				.setSave(true)
				.setDebug(false)
				.setFactor(10)
				.render();
	}

	@Test
	public void testDecorationDisease() {
		final String stId = "R-HSA-5602410";
		final List<Long> selected = Arrays.asList(5602549L);
		final Decorator decorator = new Decorator(null, selected, null);
		new RendererInvoker(stId)
				.setDecorator(decorator)
				.setSave(true)
				.setDebug(false)
				.setFactor(10)
				.render();
	}

	@Test
	public void testHardestDiagrams() {
		final List<String> pathways = Arrays.asList(
				"R-ATH-448424",
				"R-DDI-448424",
				"R-CEL-448424",
				"R-DME-448424",
				"R-OSA-448424",
				"R-SCE-448424",
				"R-SPO-448424",
				"R-DDI-212436",
				"R-PFA-212436",
				"R-OSA-6804756",
				"R-PFA-8854691",
				"R-SPO-8854691"
		);
		// This is a list of the diagrams that do take more time per element
		pathways.forEach(stId -> new RendererInvoker(stId).setDebug(false).render());
	}

	@Test
	public void testSomeDiagrams() {
		final List<String> pathways = Arrays.asList(
				"R-HSA-5602410",
				"R-HSA-1362409",  // Mithocondrial iron-sulfur cluster biogenesis
				"R-HSA-169911",  // Regulation of apoptosis
				"R-HSA-68874",  // M/G1 Transition
				"R-HSA-211945",  // Phase I -   Functionlaization of compounds
				"R-HSA-109581",  // Apoptosis
				"R-HSA-391251",  // Protein folding
				"R-HSA-6796648",  // TP53 Regulates Transcription of DNA Repair Genes
				"R-HSA-5638302",
				"R-HSA-1474244",
				"R-HSA-4839726",
				"R-HSA-8963743",
				"R-HSA-8935690",
				"R-HSA-448424",
				"R-HSA-72312",
				"R-HSA-168643"
		);
		pathways.forEach(stId ->
				new RendererInvoker(stId)
						.setDebug(false)
						.setSave(true)
						.setFactor(5)
//						.setFormat("jpeg")
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
				.setDebug(false)
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
				file.getParentFile().mkdirs();
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

	@Test
	public void testJpegColoring() {
		final BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

		final Graphics2D graphics = image.createGraphics();
		graphics.setBackground(Color.WHITE);
		graphics.clearRect(0, 0, 100, 100);
		try {
			ImageIO.write(image, "jpg", new File("image.jpg"));
			ImageIO.write(image, "png", new File("image.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class RendererInvoker {
		private String stId;
		private String format = "png";
		private Decorator decorator = null;
		private boolean save = false;
		private boolean debug = true;
		private double factor = 1;

		public RendererInvoker(String stId) {
			this.stId = stId;
		}

		/**
		 * default is png
		 *
		 * @param format
		 *
		 * @return
		 */
		RendererInvoker setFormat(String format) {
			this.format = format;
			return this;
		}

		/**
		 * default is true
		 *
		 * @param debug
		 *
		 * @return
		 */
		public RendererInvoker setDebug(boolean debug) {
			this.debug = debug;
			return this;
		}

		/**
		 * Default is null
		 *
		 * @param decorator
		 *
		 * @return
		 */
		public RendererInvoker setDecorator(Decorator decorator) {
			this.decorator = decorator;
			return this;
		}

		/**
		 * default is 1
		 *
		 * @param factor
		 *
		 * @return
		 */
		public RendererInvoker setFactor(double factor) {
			this.factor = factor;
			return this;
		}

		/**
		 * Default is false
		 *
		 * @param save
		 *
		 * @return
		 */
		public RendererInvoker setSave(boolean save) {
			this.save = save;
			return this;
		}

		/**
		 * This method is a real use case from the server side.
		 */
		public void render() {
			// Put json files in local
			if (!download(stId)) return;
			try {
				System.out.println(stId);
				// Create the output stream
				IMAGES_FOLDER.mkdirs();
				final OutputStream outputStream = save
						? new BufferedOutputStream(new FileOutputStream(new File(IMAGES_FOLDER, stId + "." + format)))
						: NullOutputStream.NULL_OUTPUT_STREAM;
				// Call to service
				RasterExporter.export(stId, DIAGRAMS_FOLDER.getAbsolutePath(),
						"modern", decorator, format, outputStream, factor, debug);
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


