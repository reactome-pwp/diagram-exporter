package org.reactome.server.tools.diagram.exporter.raster;

import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.common.ResourcesFactory;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.image.BufferedImage;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class RasterExporter {

	/**
	 * Service layer that provides access to the raster exporter. This service
	 * outputs the result as a BufferedImage, not to a File.
	 * <p>
	 * To save the image to an URL: <code>
	 * <pre>
	 *     URL url = new URL("http://host.com/");
	 *     HttpUrlConnection connection = (HttpUrlConnection)
	 * url.openConnection();
	 *     connection.setDoOutput(true);  // your url must support writing
	 *     OutputStream os = connection.getOutputStream();
	 *     ImageIO.write(image, ext, os);
	 * </pre>
	 * </code>
	 * <p>
	 * To save to a File <code>
	 * <pre>
	 * File file = new File(path, stId + ".png");
	 * ImageIO.write(image, ext, file);
	 * </pre>
	 * </code>
	 *
	 * @param stId        stable identifier of the diagram
	 * @param diagramPath static directory for the diagram json
	 * @param ext         output format: jpg, jpeg, png or gif. Case
	 *                    insensitive
	 * @param factor      quality of output image. number of pixels per point in
	 *                    the diagram
	 * @param decorator   flagged and selected elements
	 * @param token       token of analysis or null
	 * @param scheme      color profile for diagram, analysis and interactors
	 */
	public static BufferedImage export(String stId, String diagramPath,
	                                   String ext, double factor,
	                                   Decorator decorator,
	                                   String token, ColorScheme scheme)
			throws DiagramJsonNotFoundException, DiagramProfileException, DiagramJsonDeserializationException {
		final Graph graph = ResourcesFactory.getGraph(diagramPath, stId);
		final Diagram diagram = ResourcesFactory.getDiagram(diagramPath, stId);
		final ColorProfiles colorProfiles = new ColorProfiles(scheme);
		final RasterRenderer renderer = new RasterRenderer(diagram, graph, decorator, colorProfiles, token);
		return renderer.render(factor, ext);
	}

}
