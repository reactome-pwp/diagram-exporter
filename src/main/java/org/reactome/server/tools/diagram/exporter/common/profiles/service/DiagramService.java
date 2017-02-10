package org.reactome.server.tools.diagram.exporter.common.profiles.service;

import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.pptx.PowerPointExporter;
import org.reactome.server.tools.diagram.exporter.pptx.exception.LicenseException;
import org.reactome.server.tools.diagram.exporter.pptx.model.Decorator;

import java.io.File;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class DiagramService {

    /**
     * Service layer that provides access to the pptx exporter
     * This method won't work in case of invalid software license
     *
     * @param diagramJsonFullpath full path for the diagram json
     * @param profileName         Color profile name
     * @param outputPath          output folder + stId.pptx will be written based on the color profile
     * @return the pptx file
     * @throws DiagramJsonDeserializationException, DiagramProfileException, DiagramJsonNotFoundException, LicenseException
     */
    public File exportToPPTX(String diagramJsonFullpath, String profileName, String outputPath, Decorator decorator) throws DiagramJsonDeserializationException, DiagramProfileException, DiagramJsonNotFoundException, LicenseException {
        PowerPointExporter.export(diagramJsonFullpath, profileName.toLowerCase(), outputPath, decorator);
        if(decorator.isDecorated()){
            // add the tmp file extension. We don't save the file if there is flag or selection
            outputPath = outputPath + Decorator.EXTENSION;
        }
        return new File(outputPath);
    }
}
