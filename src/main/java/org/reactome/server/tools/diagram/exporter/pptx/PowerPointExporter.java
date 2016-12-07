package org.reactome.server.tools.diagram.exporter.pptx;

import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.data.DiagramFactory;
import org.reactome.server.tools.diagram.data.exception.DeserializationException;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.exporter.DiagramExporter;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileFactory;
import org.reactome.server.tools.diagram.exporter.common.profiles.model.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.pptx.parser.DiagramPresentation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class PowerPointExporter {

    public static void export(String pathway, DiagramProfile profile, String path) {
        Diagram diagram = null;
        try {
            diagram = getDiagram(pathway);
        } catch (DeserializationException | IOException e) {
            System.err.println("Oops! We have a problem!"); //TODO: log this and recover?
            e.printStackTrace();
            System.exit(1);
        }

        DiagramPresentation diagramPresentation = new DiagramPresentation(diagram, profile);
        diagramPresentation.export();
        diagramPresentation.save(path);
    }

    public static DiagramProfile getDiagramProfile(String name) throws DiagramProfileException, IOException {
        InputStream is = DiagramExporter.class.getResourceAsStream("/profiles/" + name + ".json");
        return DiagramProfileFactory.getModelObject(IOUtils.toString(is));
    }

    private static Diagram getDiagram(String identifier) throws DeserializationException, IOException {
        String json = new String(Files.readAllBytes(Paths.get("/Users/reactome/diagram/static/" + identifier + ".json")));
        return DiagramFactory.getDiagram(json);
    }
}
