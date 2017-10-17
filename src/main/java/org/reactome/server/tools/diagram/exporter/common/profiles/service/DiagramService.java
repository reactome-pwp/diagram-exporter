package org.reactome.server.tools.diagram.exporter.common.profiles.service;

import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.pptx.PowerPointExporter;
import org.reactome.server.tools.diagram.exporter.raster.ColorScheme;
import org.reactome.server.tools.diagram.exporter.raster.RasterExporter;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@SuppressWarnings("unused")
public class DiagramService {

	/**
	 * Service layer that provides access to the pptx exporter This method
	 * assumes a software is embedded in the project, otherwise evaluation will
	 * be generated
	 *
	 * @param stId              stable identifier of the diagram
	 * @param diagramJsonFolder static for the diagram json
	 * @param profileName       Color diagram name
	 * @param outputFolder      output folder, the given folder + the color
	 *                          diagram as a folder.
	 * @param decorator         class that holds the flagged and the selected
	 *                          elements into two different lists.
	 *
	 * @return the pptx file
	 *
	 * @throws DiagramJsonDeserializationException, DiagramProfileException,
	 *                                              DiagramJsonNotFoundException
	 */
	public File exportToPPTX(String stId, String diagramJsonFolder, String profileName, String outputFolder, Decorator decorator) throws DiagramJsonDeserializationException, DiagramProfileException, DiagramJsonNotFoundException {
		return PowerPointExporter.export(stId, diagramJsonFolder, profileName.toLowerCase(), outputFolder, decorator, "");
	}

	/**
	 * Service layer that provides access to the pptx exporter This method
	 * assumes a valid software is provided, otherwise evaluation will be
	 * generated
	 *
	 * @param stId              stable identifier of the diagram
	 * @param diagramJsonFolder static for the diagram json
	 * @param profileName       Color diagram name
	 * @param outputFolder      output folder, the given folder + the color
	 *                          diagram as a folder.
	 * @param decorator         class that holds the flagged and the selected
	 *                          elements into two different lists.
	 * @param licenseFilePath   a valid Aspose Software License.
	 *
	 * @return the pptx file
	 *
	 * @throws DiagramJsonDeserializationException, DiagramProfileException,
	 *                                              DiagramJsonNotFoundException
	 */
	public File exportToPPTX(String stId, String diagramJsonFolder, String profileName, String outputFolder, Decorator decorator, String licenseFilePath) throws DiagramJsonDeserializationException, DiagramProfileException, DiagramJsonNotFoundException {
		return PowerPointExporter.export(stId, diagramJsonFolder, profileName.toLowerCase(), outputFolder, decorator, licenseFilePath);
	}

	/**
	 * Service layer that provides access to the raster exporter. This service
	 * outputs the result as a BufferedImage, not to a File.
	 * <p>
	 * To save the image to an URL: <code>
	 * <pre>
	 * URL url = new URL("http://host.com/");
	 * HttpUrlConnection connection = (HttpUrlConnection) url.openConnection();
	 * connection.setDoOutput(true);  // your url must support writing
	 * OutputStream os = connection.getOutputStream();
	 * ImageIO.write(image, ext, os);
	 * </pre>
	 * </code>
	 * <p>
	 * To save to a File <code>
	 * <pre>
	 * File file = new File(path, stId + "." + ext);
	 * ImageIO.write(image, ext, file);
	 * </pre>
	 * </code>
	 *
	 * @param stId        stable identifier of the diagram
	 * @param ext         output format: jpg, jpeg, png or gif. Case
	 *                    insensitive
	 * @param factor      quality of output image. Factor represents the number
	 *                    of pixels per point in the diagram
	 * @param decorator   flagged and selected elements
	 * @param diagramPath static directory for the diagram json
	 * @param token       analysis token if available
	 * @param scheme      color profiles for diagram, analysis and interactors
	 */
	public BufferedImage exportToRaster(String stId, String diagramPath,
	                                    String ext, double factor,
	                                    Decorator decorator,
	                                    String token, ColorScheme scheme)
			throws DiagramProfileException, DiagramJsonDeserializationException,
			DiagramJsonNotFoundException {
		return RasterExporter.export(stId, diagramPath, ext, factor, decorator,
				token, scheme);
	}
}
