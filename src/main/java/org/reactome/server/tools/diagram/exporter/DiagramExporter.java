package org.reactome.server.tools.diagram.exporter;

import org.reactome.server.tools.diagram.exporter.pptx.PowerPointExporter;

/**
 * Main class for the diagram exporter project
 */
public class DiagramExporter {

    public static void main(String[] args) throws Exception{

//        PowerPointExporter.export("R-HSA-169911", "/Users/reactome/diagram/exporter/");

        //Activator
//        PowerPointExporter.export("R-MMU-2990846", "/Users/reactome/diagram/exporter/");
//        PowerPointExporter.export("R-HSA-2990846", "/Users/reactome/diagram/exporter/");

        //Inhibitor
//        PowerPointExporter.export("R-HSA-177929", "/Users/reactome/diagram/exporter/");
//        PowerPointExporter.export("R-MMU-177929", "/Users/reactome/diagram/exporter/");

        // Gene
        PowerPointExporter.export("R-HSA-69620", "/Users/reactome/diagram/exporter/");

        System.out.println("Diagram exported.");
    }

}
