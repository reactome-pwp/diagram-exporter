package org.reactome.server.tools.diagram.exporter.common.profiles.service;

import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.pptx.PowerPointExporter;
import org.reactome.server.tools.diagram.exporter.pptx.model.Decorator;

import java.io.File;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@SuppressWarnings("unused")
public class DiagramService {

    /**
     * Service layer that provides access to the pptx exporter
     * This method won't work in case of invalid software license
     *
     * @param stId                stable identifier of the diagram
     * @param diagramJsonFolder   static for the diagram json
     * @param profileName         Color profile name
     * @param outputFolder        output folder, the given folder + the color profile as a folder.
     * @return the pptx file
     * @throws DiagramJsonDeserializationException, DiagramProfileException, DiagramJsonNotFoundException
     */
    public File exportToPPTX(String stId, String diagramJsonFolder, String profileName, String outputFolder, Decorator decorator) throws DiagramJsonDeserializationException, DiagramProfileException, DiagramJsonNotFoundException {
        return PowerPointExporter.export(stId, diagramJsonFolder, profileName.toLowerCase(), outputFolder, decorator, "");
    }
}
