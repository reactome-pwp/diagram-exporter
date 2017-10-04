package org.reactome.server.tools.diagram.exporter.raster;


import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;
import org.reactome.server.tools.diagram.data.DiagramFactory;
import org.reactome.server.tools.diagram.data.exception.DeserializationException;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.graph.GraphNode;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.pptx.model.Decorator;

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
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ImageExporterTest {

	private static final String host = "http://reactomerelease.oicr.on.ca/download/current/diagram/";
	private static final File DIAGRAMS_FOLDER = new File("/media/pascual/Disco1TB/reactome/diagrams");
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
					.forEach(s -> render(s, "png", null, false, false));
			pathways.stream()
					.filter(this::exists)
					.limit(1000)
					.forEach(s -> render(s, "png", null, false, true));
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
	public void testSelection() {
		final String stId = "R-HSA-169911";// Regulation of apoptosis
		final Graph graph = getGraph(stId);
		final List<Long> selected = getIdsFor("A1A4S6", graph);
		final List<Long> analysis = getIdsFor("Q13177", graph);
		final List<Long> flags = getIdsFor("O60313", graph);
		selected.add(211734L);
		final Decorator decorator = new Decorator(flags, selected, analysis);
		render(stId, "png", decorator, true, false);
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
		pathways.forEach(stId -> render(stId, "png", null, true, false));
	}

	@Test
	public void testSomeDiagrams() {
		final List<String> pathways = Arrays.asList(
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
				"R-HSA-72312"
		);
		pathways.forEach(stId -> render(stId, "png", null, true, false));
	}

	/**
	 * This method is a real use case from the server side.
	 *
	 * @param stId      diagram stId
	 * @param format    image format: JPEG, PNG, GIF, BMP and WBMP
	 * @param decorator selections and flags
	 * @param save      if true, diagram will be saved to a file in diagrams
	 */
	private void render(String stId, String format, Decorator decorator, boolean save, boolean debug) {
		// Put json files in local
		if (!download(stId)) return;
		try {
			// Create the output stream
			final OutputStream outputStream = save
					? new FileOutputStream(new File(IMAGES_FOLDER, stId + "." + format))
					: NullOutputStream.NULL_OUTPUT_STREAM;
			// Call to service
			ImageExporter.export(stId, DIAGRAMS_FOLDER.getAbsolutePath(),
					"modern", decorator, format, outputStream, 1, true);
		} catch (DiagramJsonNotFoundException | DiagramProfileException
				| DiagramJsonDeserializationException | IOException e) {
			e.printStackTrace();
		}

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

}


