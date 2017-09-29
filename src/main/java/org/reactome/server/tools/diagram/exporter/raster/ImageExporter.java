package org.reactome.server.tools.diagram.exporter.raster;

import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.profile.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.common.ResourcesFactory;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.pptx.model.Decorator;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ImageExporter {

	public static void save(BufferedImage image, String format, File file) throws IOException {
		if (format.equalsIgnoreCase("jpeg")) {
			final BufferedImage jImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
			jImage.getGraphics().drawImage(image, 0, 0, Color.WHITE, null);
			ImageIO.write(jImage, format, file);
		} else {
			ImageIO.write(image, format, file);
		}
	}

	/**
	 * Service layer that provides access to the raster exporter.
	 *
	 * @param stId              stable identifier of the diagram
	 * @param diagramJsonFolder static for the diagram json
	 * @param profileName       Color profile name
	 * @param outputFolder      output folder, the given folder + the color profile as a folder.
	 * @param decorator         class that holds the flagged and the selected elements into two different lists.
	 * @param factor            quality of output image. Factor represents the number of pixels per points in the diagram
	 * @param fileExtension     output format: JPEG, PNG, GIF, BMP and WBMP. Case insensitive
	 */
	public static File export(String stId, String diagramJsonFolder,
	                          String profileName, String outputFolder,
	                          Decorator decorator, String fileExtension,
	                          double factor)
			throws DiagramProfileException, DiagramJsonDeserializationException,
			DiagramJsonNotFoundException, IOException {
		final long startPre = System.currentTimeMillis();
		final DiagramProfile profile = ResourcesFactory.getDiagramProfile(profileName);
		final Graph graph = ResourcesFactory.getGraph(diagramJsonFolder, stId);
		final Diagram diagram = ResourcesFactory.getDiagram(diagramJsonFolder, stId);
		final ImageRenderer renderer = new ImageRenderer(diagram, graph, profile, decorator);
		final long startRender = System.currentTimeMillis();
		final BufferedImage image = renderer.render(factor);
		final long startSave = System.currentTimeMillis();
		final File file = new File(outputFolder, stId + "." + fileExtension);
		save(image, fileExtension, file);
		final long end = System.currentTimeMillis();
		final long pre = startRender - startPre;
		final long render = startSave - startRender;
		final long save = end - startSave;
		long elements = countElements(diagram);
		long paintCalls = contPaintCalls(renderer.getIndex(), diagram);
		printline("diagram", "elements", "paint calls", "pre-process", "rendering", "saving");
		printline(stId, elements, paintCalls, pre, render, save);
		return file;
	}

	private static int countElements(Diagram diagram) {
		return diagram.getEdges().size()
				+ diagram.getNodes().size()
				+ diagram.getCompartments().size()
				+ diagram.getNotes().size()
				+ diagram.getShadows().size();
	}

	private static long contPaintCalls(DiagramIndex index, Diagram diagram) {
		final AtomicLong graphicCalls = new AtomicLong();
		index.getClassifiedNodes().forEach((rClass, map) ->
				map.forEach((renderType, nodes) ->
						nodes.forEach(node -> {
							// fill, border, text
							graphicCalls.addAndGet(3);
						})));
		index.getClassifiedReactions().forEach((rClass, map) ->
				map.forEach((renderType, edges) -> {
					edges.forEach(edge -> {
						// fill, border, text
						graphicCalls.addAndGet(3);
						graphicCalls.addAndGet(edge.getSegments().size());
					});
				}));
		index.getClassifiedConnectors().forEach((rClass, map) ->
				map.forEach((renderType, connectors) -> {
					connectors.forEach(connector -> {
						// fill, border, text
						graphicCalls.addAndGet(3);
						graphicCalls.addAndGet(connector.getSegments().size());
					});
				}));
		index.getSelectedReactions().forEach(edge -> {
			graphicCalls.addAndGet(2);  // fill, border
			graphicCalls.addAndGet(edge.getSegments().size());
		});
		index.getSelectedConnectors().forEach(connector -> {
			graphicCalls.addAndGet(2);  // fill, border
			graphicCalls.addAndGet(connector.getSegments().size());
		});
		// only border
		graphicCalls.addAndGet(index.getSelectedNodes().size());
		graphicCalls.addAndGet(index.getHaloNodes().size());
		index.getHaloEdges().forEach(edge -> {
			graphicCalls.addAndGet(1); // border
			graphicCalls.addAndGet(edge.getSegments().size());
		});
		index.getHaloConnectors().forEach(connector -> {
			graphicCalls.addAndGet(1); // border
			graphicCalls.addAndGet(connector.getSegments().size());
		});
		graphicCalls.addAndGet(index.getHaloNodes().size());
		index.getFlagConnectors().forEach(connector -> {
			graphicCalls.addAndGet(1);  // border
			graphicCalls.addAndGet(connector.getSegments().size());
		});
		index.getFlagReactions().forEach(connector -> {
			graphicCalls.addAndGet(1);  // border
			graphicCalls.addAndGet(connector.getSegments().size());
		});
		diagram.getShadows().forEach(shadow -> graphicCalls.addAndGet(3));
		return graphicCalls.longValue();
	}

	private static void printline(Object... objects) {
		final List<String> strings = Arrays.stream(objects)
				.map(String::valueOf)
				.collect(Collectors.toList());
		System.out.println(String.join("\t", strings));

	}
}
