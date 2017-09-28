package org.reactome.server.tools.diagram.exporter.raster;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.data.DiagramFactory;
import org.reactome.server.tools.diagram.data.exception.DeserializationException;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.graph.GraphNode;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.profile.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileFactory;
import org.reactome.server.tools.diagram.exporter.pptx.model.Decorator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
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

public class ImageExporterTest extends TestCase {

	private static final String host = "http://reactome.org/download/current/diagram/";
	private final Logger logger = Logger.getLogger(getClass().getName());


	public ImageExporterTest(String name) {
		super(name);

	}

	public static Test suite() {

		return new TestSuite(ImageExporterTest.class);
	}

	public void test() {
		final List<String> pathways = Arrays.asList(
//				"R-HSA-1362409",  // Mithocondrial iron-sulfur cluster biogenesis
//				"R-HSA-169911",  // Regulation of apoptosis
//				"R-HSA-68874",  // M/G1 Transition
//				"R-HSA-211945",  // Phase I -   Functionlaization of compounds
//				"R-HSA-109581",  // Apoptosis
//				"R-HSA-391251",  // Protein folding
//				"R-HSA-6796648",  // TP53 Regulates Transcription of DNA Repair Genes
				"R-HSA-5638302"
		);
		pathways.forEach(this::render);

	}

	private void render(String stId) {
		try {
			final DiagramProfile profile = getProfile("modern");
			final Diagram diagram = getDiagram(stId);
			final Graph graph = getGraph(stId);

			final List<Long> selected = getIdsFor("A1A4S6", graph);
			final List<Long> analysis = getIdsFor("Q13177", graph);
			final List<Long> flags = getIdsFor("O60313", graph);
			selected.add(211734L);

			final Decorator decorator = new Decorator(flags, selected, analysis);

			assertNotNull(diagram);
			assertNotNull(profile);
			assertNotNull(decorator);
			assertNotNull(graph);
//			assertEquals("Standard", profile.getName());

			final BufferedImage image = new ImageRenderer(diagram, graph, profile, decorator).render(2);

			// JPEG, PNG, GIF, BMP and WBMP
			new File("diagrams").mkdirs();
			ImageExport.save(image, "png", stId);

		} catch (IOException | DeserializationException e) {
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

	private Graph getGraph(String stId) throws IOException, DeserializationException {
		final File cached = new File("cache", stId + ".graph.json");
		if (!cached.exists()) {
			System.out.println("saving graph of " + stId);
			cached.getParentFile().mkdirs();
			final URL url = new URL(host + stId + ".graph.json");
			final ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			final FileOutputStream fos = new FileOutputStream(cached);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		}
		final String json = IOUtils.toString(cached.toURI(), Charset.defaultCharset());
		return DiagramFactory.getGraph(json);

	}

	private Diagram getDiagram(String stId) throws IOException, DeserializationException {
		final File cached = new File("cache", stId + ".json");
		if (!cached.exists()) {
			cached.getParentFile().mkdirs();
			System.out.println("saving diagram of " + stId);
			final URL url = new URL(host + stId + ".json");
			final ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			final FileOutputStream fos = new FileOutputStream(cached);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		}
		final String json = IOUtils.toString(cached.toURI(), Charset.defaultCharset());
		return DiagramFactory.getDiagram(json);
	}

	private DiagramProfile getProfile(String name) throws DiagramProfileException, DiagramJsonDeserializationException {
		return DiagramProfileFactory.getDiagramProfile(name);
	}


}