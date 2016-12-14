package org.reactome.server.tools.diagram.exporter;

import org.reactome.server.tools.diagram.exporter.common.profiles.model.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.pptx.PowerPointExporter;

/**
 * Main class for the diagram exporter project
 */
public class DiagramExporter {

    public static void main(String[] args) throws Exception {
        for (ColorProfiles colorProfile : ColorProfiles.values()) {

//            if (!colorProfile.name().equals("Modern")) continue;

            // All the diagrams will be exported with the same colour profile
            DiagramProfile profile = PowerPointExporter.getDiagramProfile(colorProfile.name());
            String path = "/Users/reactome/diagram/exporter/" + profile.getName() + "/";

            // To start with...
            PowerPointExporter.export("R-HSA-169911", profile, path);

            // Activator, simple diagram with long text to be place inside the shape
            PowerPointExporter.export("R-MMU-2990846", profile, path);

            // Activator, nice diagram layout.
            PowerPointExporter.export("R-HSA-2990846", profile, path);

            // Inhibitor
            PowerPointExporter.export("R-HSA-177929", profile, path);

            // Gene (CDKN1A Gene) and RNA (CDKN1A mRNA) Shape (also tricky diagram)
            // Also has few links, One activator and one inhibitor
            PowerPointExporter.export("R-HSA-69620", profile, path);

            // Mitophagy, multiple Compartments
            PowerPointExporter.export("R-HSA-5205647", profile, path);

            // Multiple Compartments
            PowerPointExporter.export("R-HSA-1489509", profile, path);

            // Disease
            PowerPointExporter.export("R-HSA-162909", profile, path);

            // Crossed and Fade out
            PowerPointExporter.export("R-HSA-5603041", profile, path);
            PowerPointExporter.export("R-HSA-5603029", profile, path);

            // Encapsulated Pathway
            PowerPointExporter.export("R-HSA-1222556", profile, path);

            // Messy!
            PowerPointExporter.export("R-HSA-388396", profile, path);
        }

        System.out.println("Diagrams exported.");
    }

    private enum ColorProfiles {
        Standard, Modern
    }
}
