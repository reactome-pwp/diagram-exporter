package org.reactome.server.tools.diagram.exporter.pptx;

import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.data.DiagramFactory;
import org.reactome.server.tools.diagram.data.exception.DeserializationException;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.DiagramExporter;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.pptx.model.Decorator;
import org.reactome.server.tools.diagram.exporter.pptx.parser.DiagramPresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

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
        Diagram diagram = getDiagram(staticFolder, stId);
        DiagramProfile profile = getDiagramProfile(profileName.toLowerCase());

        DiagramPresentation diagramPresentation = new DiagramPresentation(diagram, profile, decorator);
        diagramPresentation.export();
        return diagramPresentation.save(outputFolder, stId, license);
    }

    public static DiagramProfile getDiagramProfile(String name) throws DiagramProfileException, DiagramJsonDeserializationException {
        logger.info("Getting Profile [{}]", name);
        InputStream is = DiagramExporter.class.getResourceAsStream("/profiles/" + name + ".json");
        try {
            if (is == null) {
                logger.error("Could not read diagram color profile {}", name);
                throw new DiagramProfileException("Could not read diagram color profile " + name);
            }
//            return DiagramProfileFactory.getModelObject(IOUtils.toString(is, "UTF-8"));
            return DiagramFactory.getProfile(IOUtils.toString(is, "UTF-8"));
        } catch (DeserializationException e) {
            logger.error("Could not deserialize diagram color profile {}", name);
            throw new DiagramJsonDeserializationException("Could not deserialize diagram color profile " + name);
        }catch (IOException e) {
            logger.error("Could not read diagram color profile {}", name);
            throw new DiagramProfileException("Could not read diagram color profile " + name);
        }
    }

    private static Diagram getDiagram(String staticFolder, String stId) throws DiagramJsonDeserializationException, DiagramJsonNotFoundException {
        String pathway = staticFolder + "/" + stId + ".json";
        logger.info("Getting diagram JSON {}", pathway);
        try {
            String json = new String(Files.readAllBytes(Paths.get(pathway)));
            return DiagramFactory.getDiagram(json);
        } catch (DeserializationException e) {
            logger.error("Could not deserialize diagram json for pathway {}", pathway);
            throw new DiagramJsonDeserializationException("Could not deserialize diagram json for pathway " + pathway);
        } catch (IOException e) {
            logger.error("Could not read diagram json for pathway {}", pathway);
            throw new DiagramJsonNotFoundException("Could not read diagram json for pathway " + pathway);
        }
    }
}
