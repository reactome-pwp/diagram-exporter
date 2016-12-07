package org.reactome.server.tools.diagram.exporter.pptx.parser;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.exporter.DiagramExporter;
import org.reactome.server.tools.diagram.exporter.common.profiles.model.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.pptx.model.*;

import java.awt.*;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.reactome.server.tools.diagram.exporter.pptx.util.PPTXShape.reorder;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class DiagramPresentation {

    private Diagram diagram;
    private DiagramProfile profile;
    private Presentation presentation;
    private IShapeCollection shapes;

    //TODO: Use DiagramProfile instead
    private ColourProfile colourProfile;

    private Map<Long, PPTXNode> nodesMap = new HashMap<>();

    public DiagramPresentation(Diagram diagram, DiagramProfile profile) {
        this.diagram = diagram;
        this.profile = profile;
        presentation = new Presentation();
        ISlide slide = presentation.getSlides().get_Item(0);
        shapes = slide.getShapes();
        colourProfile = new ModernColourProfile();
    }

    public void export() {
        if (!isLicensed()) {
            // checking license before!
            // TODO WARN ? ERROR? STOP PROCESSING ? saving as evaluated version and license expired
            System.out.println("Missing Software License.");
            System.exit(1); // does not make sense continue the process here - files will be all wrong :)
        }

        // Set slide size.
        Dimension pageSize = new Dimension(diagram.getMaxX() + 70, diagram.getMaxY() + 70);
        presentation.getSlideSize().setSize(pageSize);

        // Render Compartments
        Collections.reverse(diagram.getCompartments());
        for (Compartment compartment : diagram.getCompartments()) {
            EntityCompartment entityCompartment = new EntityCompartment(compartment);
            entityCompartment.render(shapes, colourProfile);
        }

        // Render Notes
        for (Note note : diagram.getNotes()) {
            CompartmentNote compartmentNote = new CompartmentNote(note);
            compartmentNote.render(shapes);
        }

        // Render Nodes
        for (Node node : diagram.getNodes()) {
            PPTXNode pptxNode = getNode(node);
            nodesMap.put(pptxNode.getId(), pptxNode); //If two nodes share identifier, only the second one is kept >> NOTE: It shouldn't happen //TODO: Report?
            pptxNode.render(shapes, colourProfile);
        }

        // Render Edges
        for (Edge edge : diagram.getEdges()) {
            PPTXReaction pptxReaction = new PPTXReaction(edge, nodesMap);
            pptxReaction.render(shapes);
        }

        // Render Links
        for (Link link : diagram.getLinks()){
            PPTXLink pptxLink = new PPTXLink(link, nodesMap);
            pptxLink.render(shapes);
        }

        // REORDER SHAPES HERE IF NEEDED!
        reorder(shapes, nodesMap.values());

    }

    public void save(String path) {
        String fileName = path + diagram.getStableId() + ".pptx";
        presentation.save(fileName, SaveFormat.Pptx);
    }

    /**
     * Get proper instance of a node and its coordinates e size
     *
     * @param node a diagram layout node
     * @return instance of PPTXNode
     */
    private PPTXNode getNode(Node node) {
        PPTXNode pptxNode;
        switch (node.getSchemaClass()) {
            case "Complex":
                pptxNode = new Complex(node);
                break;
            case "DefinedSet":
            case "CandidateSet":
            case "OpenSet":
                pptxNode = new Set(node);
                break;
            case "EntityWithAccessionedSequence":
                pptxNode = new Protein(node);
                if (Objects.equals(node.getRenderableClass(), "Gene")) {
                    pptxNode = new Gene(node);
                }
                if (Objects.equals(node.getRenderableClass(), "RNA")) {
                    pptxNode = new RNA(node);
                }
                break;
            case "GenomeEncodedEntity":
            case "OtherEntity":
                pptxNode = new OtherEntity(node);
                break;
            case "SimpleEntity":
                pptxNode = new Chemical(node);
                break;
            case "Pathway":
                pptxNode = new EncapsulatedPathway(node);
                break;
            default:
                throw new IllegalArgumentException("Invalid schema class [" + node.getSchemaClass() + "]. Create the switch-case for the given class");
        }
        return pptxNode;
    }

    private boolean isLicensed() {
        InputStream is = DiagramExporter.class.getResourceAsStream("/license/Aspose.Slides.lic");
        License license = new License();
        license.setLicense(is);
        return license.isLicensed();
    }
}