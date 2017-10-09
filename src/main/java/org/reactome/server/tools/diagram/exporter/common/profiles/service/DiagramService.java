package org.reactome.server.tools.diagram.exporter.common.profiles.service;

import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.pptx.PowerPointExporter;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.raster.RasterExporter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@SuppressWarnings("unused")
public class DiagramService {

	/**
	 * Service layer that provides access to the pptx exporter
	 * This method assumes a software is embedded in the project, otherwise evaluation will be generated
	 *
	 * @param stId              stable identifier of the diagram
	 * @param diagramJsonFolder static for the diagram json
	 * @param profileName       Color profile name
	 * @param outputFolder      output folder, the given folder + the color profile as a folder.
	 * @param decorator         class that holds the flagged and the selected elements into two different lists.
	 * @return the pptx file
	 * @throws DiagramJsonDeserializationException, DiagramProfileException, DiagramJsonNotFoundException
	 */
	public File exportToPPTX(String stId, String diagramJsonFolder, String profileName, String outputFolder, Decorator decorator) throws DiagramJsonDeserializationException, DiagramProfileException, DiagramJsonNotFoundException {
		return PowerPointExporter.export(stId, diagramJsonFolder, profileName.toLowerCase(), outputFolder, decorator, "");
	}

	/**
	 * Service layer that provides access to the pptx exporter
	 * This method assumes a valid software is provided, otherwise evaluation will be generated
	 *
	 * @param stId              stable identifier of the diagram
	 * @param diagramJsonFolder static for the diagram json
	 * @param profileName       Color profile name
	 * @param outputFolder      output folder, the given folder + the color profile as a folder.
	 * @param decorator         class that holds the flagged and the selected elements into two different lists.
	 * @param licenseFilePath   a valid Aspose Software License.
	 * @return the pptx file
	 * @throws DiagramJsonDeserializationException, DiagramProfileException, DiagramJsonNotFoundException
	 */
	public File exportToPPTX(String stId, String diagramJsonFolder, String profileName, String outputFolder, Decorator decorator, String licenseFilePath) throws DiagramJsonDeserializationException, DiagramProfileException, DiagramJsonNotFoundException {
		return PowerPointExporter.export(stId, diagramJsonFolder, profileName.toLowerCase(), outputFolder, decorator, licenseFilePath);
	}

	/**
	 * Service layer that provides access to the raster exporter.
	 *
	 * @param stId          stable identifier of the diagram
	 * @param diagramFolder static for the diagram json
	 * @param profileName   Color profile name
	 * @param output        image will be written in this OutputStream
	 * @param decorator     class that holds the flagged and the selected elements into two different lists.
	 * @param factor        quality of output image. Factor represents the number of pixels per points in the diagram
	 * @param format        output format: JPEG, PNG, GIF, BMP and WBMP. Case insensitive
	 */
	public void exportToRaster(String stId, String diagramFolder,
	                           String profileName, OutputStream output,
	                           Decorator decorator, String format, double factor)
			throws DiagramProfileException, DiagramJsonDeserializationException,
			DiagramJsonNotFoundException, IOException {
		RasterExporter.export(stId, diagramFolder, profileName.toLowerCase(), decorator, format, output, factor);

	}
}
