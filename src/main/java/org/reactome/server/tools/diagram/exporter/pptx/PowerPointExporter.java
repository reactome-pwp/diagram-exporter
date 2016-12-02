package org.reactome.server.tools.diagram.exporter.pptx;

import org.reactome.server.tools.diagram.data.DiagramFactory;
import org.reactome.server.tools.diagram.data.exception.DeserializationException;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.exporter.pptx.parser.DiagramPresentation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class PowerPointExporter {

    public static void export(String pathway, String path) {
        Diagram diagram = null;
        try {
            diagram = getDiagram(pathway);
        } catch (DeserializationException | IOException e) {
            System.err.println("Oops! We have a problem!"); //TODO: log this and recover?
            e.printStackTrace();
            System.exit( 1 );
        }

        DiagramPresentation diagramPresentation = new DiagramPresentation(diagram);
        diagramPresentation.export();
        diagramPresentation.save(path);
    }

    private static Diagram getDiagram(String identifier) throws DeserializationException, IOException {
        String json = new String(Files.readAllBytes(Paths.get("/Users/reactome/diagram/static/" + identifier + ".json")));
        return DiagramFactory.getDiagram(json);
    }
}
