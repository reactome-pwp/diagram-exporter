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

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class RasterExporter {

	/**
	 * Service layer that provides access to the raster exporter. This service
	 * outputs the result to an outputStream, not to a File.
	 * <p>
	 * To create an OutputStream to an URL: <code>
	 * <pre>
	 *     URL url = new URL("http://host.com/");
	 *     HttpUrlConnection connection = (HttpUrlConnection)
	 * url.openConnection();
	 *     connection.setDoOutput(true);  // your url must support writing
	 *     OutputStream = connection.getOutputStream();
	 * </pre>
	 * </code>
	 * <p>
	 * To create from a File <code>
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
		final DiagramProfile profile = ResourcesFactory.getDiagramProfile(profileName);
		final Graph graph = ResourcesFactory.getGraph(diagramFolder, stId);
		final Diagram diagram = ResourcesFactory.getDiagram(diagramFolder, stId);
		final RasterRenderer renderer = new RasterRenderer(diagram, graph, profile, decorator);
		final BufferedImage image = renderer.render(factor, fileExtension);

		ImageIO.write(image, fileExtension, output);
	}

}
