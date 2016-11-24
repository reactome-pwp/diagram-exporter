package org.reactome.server.tools.diagram.exporter;

import org.reactome.server.tools.diagram.data.DiagramFactory;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.exporter.model.Pathway;
import org.reactome.server.tools.diagram.exporter.parser.DiagramParser;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Main class for the diagram exporter project
 */
public class DiagramExporter {

    public static void main(String[] args) throws Exception{
        String json = new String(Files.readAllBytes(Paths.get("R-HSA-169911.json")));
        Diagram diagram = DiagramFactory.getDiagram(json);

        Pathway pathway  = new DiagramParser().iDontKnowTheNameOfThisMethod("R-HSA-500792.json");
        System.out.println(pathway);
        System.out.println("Hello World!");
    }

}
