package org.reactome.server.tools.diagram.exporter;

import org.reactome.server.tools.diagram.exporter.pptx.PowerPointExporter;

/**
 * Main class for the diagram exporter project
 */
public class DiagramExporter {

    public static void main(String[] args) throws Exception{

        PowerPointExporter.export("R-HSA-169911", "/Users/reactome/diagram/exporter/");

        System.out.println("Done man!");
    }

}
