package org.reactome.server.tools.diagram.exporter.raster;


import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.reactome.server.tools.diagram.data.DiagramFactory;
import org.reactome.server.tools.diagram.data.exception.DeserializationException;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.graph.GraphNode;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.profile.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.common.ResourcesFactory;
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
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ImageExporterTest {

	private static final String host = "http://reactome.org/download/current/diagram/";
	private final Logger logger = Logger.getLogger(getClass().getName());


	@Ignore
	public void testPerformance() {
		final List<String> pathways = getLines(new File("all-pathways.txt"));
		if (pathways != null) {
			pathways.stream()
					.filter(this::exists)
					.forEach(this::render);
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
		try {
			final String stId = "R-HSA-169911";// Regulation of apoptosis
			final DiagramProfile profile = ResourcesFactory.getDiagramProfile("modern");
			final Diagram diagram = getDiagram(stId);
			final Graph graph = getGraph(stId);
			final List<Long> selected = getIdsFor("A1A4S6", graph);
			final List<Long> analysis = getIdsFor("Q13177", graph);
			final List<Long> flags = getIdsFor("O60313", graph);
			selected.add(211734L);
			final Decorator decorator = new Decorator(flags, selected, analysis);

			final String format = "png";
			final String diagramFolder = new File("cache").getAbsolutePath();
			final String output = new File("diagrams").getAbsolutePath();
			try {
				ImageExporter.export(stId, diagramFolder, "modern", output, decorator, format, 1);
			} catch (DiagramJsonNotFoundException e) {
				e.printStackTrace();
			}

		} catch (DiagramProfileException | DiagramJsonDeserializationException | IOException e) {
			e.printStackTrace();
		}

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
		pathways.forEach(this::render);
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
				"R-HSA-448424"
		);
		pathways.forEach(this::render);
	}

	private void render(String stId) {
		try {
			final Diagram diagram = getDiagram(stId);
			if (diagram == null) return;
			final Graph graph = getGraph(stId);
			if (graph == null) return;

			// JPEG, PNG, GIF, BMP and WBMP
			final String format = "png";
			final String diagramFolder = new File("cache").getAbsolutePath();
			final String output = new File("diagrams").getAbsolutePath();
			try {
				ImageExporter.export(stId, diagramFolder, "modern", output, null, format, 1);
//				new File("diagrams/" + stId + ".png").delete();
//				new File("cache/" + stId + ".graph.json").delete();
//				new File("cache/" + stId + ".json").delete();
			} catch (DiagramJsonNotFoundException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Can't read render", e);
		} catch (DiagramProfileException e) {
			logger.log(Level.SEVERE, "Can't read profile", e);
		} catch (DiagramJsonDeserializationException e) {
			logger.log(Level.SEVERE, "Can't read diagram", e);
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

	private Graph getGraph(String stId) {
		final File cached = new File("cache", stId + ".graph.json");
		try {
			if (!cached.exists()) {
//				System.out.println("saving graph of " + stId);
				cached.getParentFile().mkdirs();
				final URL url = new URL(host + stId + ".graph.json");
				final ReadableByteChannel rbc = Channels.newChannel(url.openStream());
				final FileOutputStream fos = new FileOutputStream(cached);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.flush();
			}
			final String json = IOUtils.toString(cached.toURI(), Charset.defaultCharset());
			return DiagramFactory.getGraph(json);
		} catch (IOException | DeserializationException e) {
		}
		return null;
	}


	private Diagram getDiagram(String stId) {
		final File cached = new File("cache", stId + ".json");
		try {
			if (!cached.exists()) {
				cached.getParentFile().mkdirs();
//				System.out.println("saving diagram of " + stId);
				final URL url = new URL(host + stId + ".json");
				final ReadableByteChannel rbc = Channels.newChannel(url.openStream());
				final FileOutputStream fos = new FileOutputStream(cached);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.flush();
			}
			final String json = IOUtils.toString(cached.toURI(), Charset.defaultCharset());
			return DiagramFactory.getDiagram(json);
		} catch (DeserializationException | IOException e) {
		}
		return null;
	}
}


