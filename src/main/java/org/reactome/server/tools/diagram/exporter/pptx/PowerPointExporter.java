package org.reactome.server.tools.diagram.exporter.pptx;

import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.common.ResourcesFactory;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.pptx.parser.DiagramPresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class PowerPointExporter {

    private static final Logger logger = LoggerFactory.getLogger("infoLogger");

    /**
     * This method allows a powerpoint to be generated for those who are using the library and does not have
     * software license. An evaluation version will be created and the program won't fail.
     */
    public static File export(String stId, String staticFolder, String profileName, String outputFolder, Decorator decorator, String license) throws DiagramJsonDeserializationException, DiagramJsonNotFoundException, DiagramProfileException {
        logger.info("Initialising the exporter to PowerPoint");
        logger.debug("Initialising the exporter to PowerPoint. Diagram [{}], Profile [{}] and Decorators [flg:{}, sel: {}]", stId, profileName, decorator.getFlags(), decorator.getSelected());
        Diagram diagram = ResourcesFactory.getDiagram(staticFolder, stId);
        DiagramProfile profile = ResourcesFactory.getDiagramProfile(profileName.toLowerCase());

        DiagramPresentation diagramPresentation = new DiagramPresentation(diagram, profile, decorator);
        diagramPresentation.export();
        return diagramPresentation.save(outputFolder, stId, license);
    }

}
