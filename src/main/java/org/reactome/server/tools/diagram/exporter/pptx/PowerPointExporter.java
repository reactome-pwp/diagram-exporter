package org.reactome.server.tools.diagram.exporter.pptx;

import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.data.DiagramFactory;
import org.reactome.server.tools.diagram.data.exception.DeserializationException;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.exporter.DiagramExporter;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileFactory;
import org.reactome.server.tools.diagram.exporter.common.profiles.model.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.pptx.exception.LicenseException;
import org.reactome.server.tools.diagram.exporter.pptx.parser.DiagramPresentation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class PowerPointExporter {

    public static void export(String pathway, String profileName, String path) throws DiagramJsonDeserializationException, DiagramJsonNotFoundException, DiagramProfileException, LicenseException {
        Diagram diagram = getDiagram(pathway);
        DiagramProfile profile = getDiagramProfile(profileName);

        DiagramPresentation diagramPresentation = new DiagramPresentation(diagram, profile);
        diagramPresentation.export();
        diagramPresentation.save(path);
    }

    public static DiagramProfile getDiagramProfile(String name) throws DiagramProfileException {
        InputStream is = DiagramExporter.class.getResourceAsStream("/profiles/" + name + ".json");
        try {
            if(is == null) throw new DiagramProfileException("Could not read diagram color profile " + name);
            return DiagramProfileFactory.getModelObject(IOUtils.toString(is));
        } catch (IOException e) {
            throw new DiagramProfileException("Could not read diagram color profile " + name);
        }
    }

    private static Diagram getDiagram(String pathway) throws DiagramJsonDeserializationException, DiagramJsonNotFoundException {
        if (!pathway.endsWith(".json")) pathway += ".json";
        try {
            String json = new String(Files.readAllBytes(Paths.get(pathway)));
            return DiagramFactory.getDiagram(json);
        } catch (DeserializationException e) {
            throw new DiagramJsonDeserializationException("Could not deserialize diagram json for pathway " + pathway);
        } catch (IOException e) {
            throw new DiagramJsonNotFoundException("Could not read diagram json for pathway " + pathway);
        }
    }
}
