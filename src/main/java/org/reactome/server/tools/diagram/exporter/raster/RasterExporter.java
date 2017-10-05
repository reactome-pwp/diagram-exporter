package org.reactome.server.tools.diagram.exporter.raster;

import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.profile.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.common.ResourcesFactory;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class RasterExporter {
	private static final Logger logger = Logger.getLogger("log");

	static {
		try {
			final FileHandler fileHandler = new FileHandler("log.txt");
			logger.setUseParentHandlers(false);
			logger.addHandler(fileHandler);
			fileHandler.setFormatter(new Formatter() {
				@Override
				public String format(LogRecord logRecord) {
					return String.join("\t", logRecord.getMessage()) + System.lineSeparator();
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private static void save(BufferedImage image, String format, OutputStream outputStream) throws IOException {
		if (format.equalsIgnoreCase("jpeg")) {
			final BufferedImage jImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
			jImage.getGraphics().drawImage(image, 0, 0, Color.WHITE, null);
			ImageIO.write(jImage, format, outputStream);
		} else {
			ImageIO.write(image, format, outputStream);
		}
	}

	/**
	 * Service layer that provides access to the raster exporter. This service
	 * outputs the result to an outputStream, not to a File.
	 * <p>
	 * To create an OutputStream to an URL:
	 * <code>
	 * <pre>
	 *     URL url = new URL("http://host.com/");
	 *     HttpUrlConnection connection = (HttpUrlConnection)
	 * url.openConnection();
	 *     connection.setDoOutput(true);  // your url must support writing
	 *     OutputStream = connection.getOutputStream();
	 * </pre>
	 * </code>
	 * <p>
	 * To create from a File
	 * <code>
	 * <pre>
	 * File file = new File(path, stId + ".png");
	 * OutputStream outputStream = new FileOutputStream(file);
	 * </pre>
	 * </code>
	 *
	 * @param stId          stable identifier of the diagram
	 * @param diagramFolder static for the diagram json
	 * @param profileName   Color profile name
	 * @param output        output stream. Image will be written here
	 * @param decorator     class that holds the flagged and the selected
	 *                      elements into two different lists.
	 * @param factor        quality of output image. Factor represents the
	 *                      number of pixels per points in the diagram
	 * @param fileExtension output format: JPEG, PNG, GIF, BMP and WBMP. Case
	 *                      insensitive
	 * @param debug         if true, times will be measured and printed to
	 *                      System.out
	 */
	public static void export(String stId, String diagramFolder,
	                          String profileName, Decorator decorator,
	                          String fileExtension, OutputStream output,
	                          double factor, boolean debug)
			throws DiagramProfileException, DiagramJsonDeserializationException,
			DiagramJsonNotFoundException, IOException {
		if (debug) {
			exportWithDebug(stId, diagramFolder, profileName, decorator, fileExtension, output, factor);
		} else {
			final DiagramProfile profile = ResourcesFactory.getDiagramProfile(profileName);
			final Graph graph = ResourcesFactory.getGraph(diagramFolder, stId);
			final Diagram diagram = ResourcesFactory.getDiagram(diagramFolder, stId);
			final ImageRenderer renderer = new ImageRenderer(diagram, graph, profile, decorator);
			final BufferedImage image = renderer.render(factor);
			save(image, fileExtension, output);
		}
	}

	/**
	 * Service layer that provides access to the raster exporter. This service
	 * outputs the result to an outputStream, not to a File.
	 * <p>
	 * To create an OutputStream to an URL:
	 * <code>
	 * <pre>
	 *     URL url = new URL("http://host.com/");
	 *     HttpUrlConnection connection = (HttpUrlConnection)
	 * url.openConnection();
	 *     connection.setDoOutput(true);  // your url must support writing
	 *     OutputStream = connection.getOutputStream();
	 * </pre>
	 * </code>
	 * <p>
	 * To create from a File
	 * <code>
	 * <pre>
	 * File file = new File(path, stId + ".png");
	 * OutputStream outputStream = new FileOutputStream(file);
	 * </pre>
	 * </code>
	 *
	 * @param stId          stable identifier of the diagram
	 * @param diagramFolder static for the diagram json
	 * @param profileName   Color profile name
	 * @param output        output stream. Image will be written here
	 * @param decorator     class that holds the flagged and the selected
	 *                      elements into two different lists.
	 * @param factor        quality of output image. Factor represents the
	 *                      number of pixels per points in the diagram
	 * @param fileExtension output format: JPEG, PNG, GIF, BMP and WBMP. Case
	 *                      insensitive
	 */
	public static void export(String stId, String diagramFolder,
	                          String profileName, Decorator decorator,
	                          String fileExtension, OutputStream output,
	                          double factor)
			throws DiagramJsonNotFoundException, IOException,
			DiagramProfileException, DiagramJsonDeserializationException {
		export(stId, diagramFolder, profileName, decorator, fileExtension, output, factor, false);
	}

	private static void exportWithDebug(String stId, String diagramJsonFolder, String profileName, Decorator decorator, String fileExtension, OutputStream output, double factor) throws DiagramProfileException, DiagramJsonDeserializationException, DiagramJsonNotFoundException, IOException {
		final long startPre = System.currentTimeMillis();
		final DiagramProfile profile = ResourcesFactory.getDiagramProfile(profileName);
		final Graph graph = ResourcesFactory.getGraph(diagramJsonFolder, stId);
		final Diagram diagram = ResourcesFactory.getDiagram(diagramJsonFolder, stId);
		final ImageRenderer renderer = new ImageRenderer(diagram, graph, profile, decorator);
		final long startRender = System.currentTimeMillis();
		final BufferedImage image = renderer.render(factor);
		final long startSave = System.currentTimeMillis();
		save(image, fileExtension, output);
		final long end = System.currentTimeMillis();
		final long pre = startRender - startPre;
		final long render = startSave - startRender;
		final long save = end - startSave;
		long elements = countElements(diagram);
		final long size = image.getWidth() * image.getHeight();
		printline(stId, size, elements, pre + render);
		System.gc();
	}

	private static int countElements(Diagram diagram) {
		return diagram.getEdges().size()
				+ diagram.getNodes().size()
				+ diagram.getCompartments().size()
				+ diagram.getNotes().size()
				+ diagram.getShadows().size();
	}

	private static void printline(Object... objects) {
		final List<String> strings = Arrays.stream(objects)
				.map(String::valueOf)
				.collect(Collectors.toList());
		logger.info(String.join("\t", strings));

	}
}
