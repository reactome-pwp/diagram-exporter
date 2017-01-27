package org.reactome.server.tools.diagram.exporter;

import org.reactome.server.tools.diagram.exporter.pptx.PowerPointExporter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class for the diagram exporter project
 */
public class DiagramExporter {

    // TODO Convert this Class to a TEST Class
    public static void main(String[] args) throws Exception {
        // Because the graph-core is not a dependency of this project and won't be, then once the files are exported
        // we won't create the dbVersion folder. This class is for testing only.
        for (ColorProfiles colorProfile : ColorProfiles.values()) {
            for (String stId : getStId()) {
                File path = new File("/Users/reactome/diagram/exporter/" + colorProfile.name().toLowerCase());
                if(!path.exists()) path.mkdirs();
                String outputFile = path.getPath() + "/" + stId + ".pptx";
                PowerPointExporter.export("/Users/reactome/diagram/static/" + stId, colorProfile.name().toLowerCase(), outputFile);
            }
        }
        System.out.println("Diagrams exported.");
    }

    /**
     * Get a list of Stable Identifier to test.
     */
    private static List<String> getStId() {
        List<String> stdIds = new ArrayList<>();
        stdIds.add("R-HSA-169911"); // To start with...
        stdIds.add("R-MMU-2990846"); // Activator, simple diagram with long text to be place inside the shape
        stdIds.add("R-HSA-2990846"); // Activator, nice diagram layout.
        stdIds.add("R-HSA-177929"); // Inhibitor
        stdIds.add("R-HSA-69620"); // CDKN1A Gene and CDKN1A mRNA Shape. Links, Activator and Inhibitor
        stdIds.add("R-HSA-5205647"); // Mitophagy, multiple Compartments
        stdIds.add("R-HSA-1489509"); // Multiple Compartments
        stdIds.add("R-HSA-162909"); // Disease
        stdIds.add("R-HSA-5603041"); // Crossed and Fade out
        stdIds.add("R-HSA-5603029"); // Crossed and Fade out
        stdIds.add("R-HSA-1222556"); // Encapsulated Pathway
        stdIds.add("R-HSA-388396"); // Messy!
        return stdIds;
    }

    private enum ColorProfiles {
        Standard, Modern
    }
}
