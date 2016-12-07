package org.reactome.server.tools.diagram.exporter;

import org.reactome.server.tools.diagram.exporter.common.profiles.model.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.pptx.PowerPointExporter;

/**
 * Main class for the diagram exporter project
 */
public class DiagramExporter {

    public static void main(String[] args) throws Exception{

        //All the diagrams will be exported with the same colour profile
        DiagramProfile profile = PowerPointExporter.getDiagramProfile("Modern");

        // To start with...
        PowerPointExporter.export("R-HSA-169911", profile, "/Users/reactome/diagram/exporter/");

        //Activator, simple diagram with long text to be place inside the shape
//        PowerPointExporter.export("R-MMU-2990846", profile, "/Users/reactome/diagram/exporter/");

        // Activator, nice diagram layout.
//        PowerPointExporter.export("R-HSA-2990846", profile, "/Users/reactome/diagram/exporter/");

        // Inhibitor
        PowerPointExporter.export("R-HSA-177929", profile, "/Users/reactome/diagram/exporter/");

        // Gene (CDKN1A Gene) and RNA (CDKN1A mRNA) Shape (also tricky diagram)
        // Also has few links, One activator and one inhibitor
        PowerPointExporter.export("R-HSA-69620", profile, "/Users/reactome/diagram/exporter/");

        // Mitophagy, multiple Compartments
        PowerPointExporter.export("R-HSA-5205647", profile, "/Users/reactome/diagram/exporter/");

        // Multiple Compartments
        PowerPointExporter.export("R-HSA-1489509", profile, "/Users/reactome/diagram/exporter/");

        System.out.println("Diagram exported.");
    }

}
