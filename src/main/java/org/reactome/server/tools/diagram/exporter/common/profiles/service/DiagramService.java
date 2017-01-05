package org.reactome.server.tools.diagram.exporter.common.profiles.service;

import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.pptx.PowerPointExporter;
import org.reactome.server.tools.diagram.exporter.pptx.exception.LicenseException;

import java.io.File;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class DiagramService {

    /**
     * Service layer that provides access to the pptx exporter
     *
     * @param diagramJsonFullpath full path for the diagram json
     * @param profileName         Color profile name
     * @param outputPath          output folder + stId.pptx will be written based on the color profile
     * @return the pptx file
     * @throws DiagramJsonDeserializationException, DiagramProfileException, DiagramJsonNotFoundException, LicenseException
     */
    public File exportToPPTX(String diagramJsonFullpath, String profileName, String outputPath) throws DiagramJsonDeserializationException, DiagramProfileException, DiagramJsonNotFoundException, LicenseException {
        PowerPointExporter.export(diagramJsonFullpath, profileName, outputPath);
        return new File(outputPath);
    }
}
