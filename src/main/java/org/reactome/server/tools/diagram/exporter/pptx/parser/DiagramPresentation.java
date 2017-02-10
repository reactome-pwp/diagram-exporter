package org.reactome.server.tools.diagram.exporter.pptx.parser;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.exporter.DiagramExporter;
import org.reactome.server.tools.diagram.exporter.common.profiles.model.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.pptx.exception.LicenseException;
import org.reactome.server.tools.diagram.exporter.pptx.model.*;

import java.awt.*;
import java.io.InputStream;
import java.util.*;
import java.util.List;

import static org.reactome.server.tools.diagram.exporter.pptx.util.PPTXShape.reorder;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class DiagramPresentation {

    private Diagram diagram;
    private DiagramProfile profile;
    private Presentation presentation;
    private IShapeCollection shapes;
    private Map<Long, PPTXNode> nodesMap = new HashMap<>();
    private Decorator decorator;
    private boolean allowEvaluation;

    public DiagramPresentation(Diagram diagram, DiagramProfile profile, Decorator decorator, boolean allowEvaluation) {
        this.diagram = diagram;
        this.profile = profile;
        presentation = new Presentation();
        ISlide slide = presentation.getSlides().get_Item(0);
        shapes = slide.getShapes();
        this.decorator = decorator;
        this.allowEvaluation = allowEvaluation;
    }

    public void export() throws LicenseException {
        if (!isLicensed() && !allowEvaluation) {
            throw new LicenseException();
        }

        Adjustment adjustment = new Adjustment(diagram);

        // Set slide size.
        Dimension pageSize = new Dimension(adjustment.getSlideWidth(), adjustment.getSlideHeight());
        presentation.getSlideSize().setSize(pageSize);

        // Render Compartments
        List<EntityCompartment> compartments = new ArrayList<>();
        for (Compartment compartment : diagram.getCompartments()) {
            EntityCompartment entityCompartment = new EntityCompartment(compartment, profile, adjustment);
            compartments.add(entityCompartment);
            entityCompartment.render(shapes);
        }
        for (EntityCompartment entityCompartment : compartments) {
            entityCompartment.renderText(shapes);
        }
        compartments.clear();

        // Render Notes
        for (Note note : diagram.getNotes()) {
            CompartmentNote compartmentNote = new CompartmentNote(note, profile, adjustment);
            compartmentNote.render(shapes);
        }

        // Render Nodes
        for (Node node : diagram.getNodes()) {
            boolean flag = decorator != null && decorator.getFlags().contains(node.getReactomeId());
            boolean selected = decorator != null && decorator.getSelected().contains(node.getReactomeId());
            PPTXNode pptxNode = getNode(node, adjustment, flag, selected);
            nodesMap.put(pptxNode.getId(), pptxNode); //If two nodes share identifier, only the second one is kept >> NOTE: It shouldn't happen //TODO: Report?
            pptxNode.render(shapes, profile);
        }

        // Render Edges
        List<Edge> diseaseEdges = new ArrayList<>();
        for (Edge edge : diagram.getEdges()) {
            boolean selected = decorator != null && decorator.getSelected().contains(edge.getReactomeId());
            // We don't process among all the edges later. This is done to make sure disease annotation is always on top.
            if (edge.getIsDisease() != null) {
                diseaseEdges.add(edge);
            } else {
                PPTXReaction pptxReaction = new PPTXReaction(edge, nodesMap, profile, adjustment, selected);
                pptxReaction.render(shapes);
            }
        }

        // Render disease edges
        for (Edge diseaseEdge : diseaseEdges) {
            boolean selected = decorator != null && decorator.getSelected().contains(diseaseEdge.getReactomeId());
            PPTXReaction pptxReaction = new PPTXReaction(diseaseEdge, nodesMap, profile, adjustment, selected);
            pptxReaction.render(shapes);
        }

        // Render Links
        for (Link link : diagram.getLinks()) {
            PPTXLink pptxLink = new PPTXLink(link, nodesMap, adjustment);
            pptxLink.render(shapes, profile);
        }

        // REORDERING SHAPES: All nodes in the were brought to front
        reorder(shapes, nodesMap.values());

    }

    public void save(String fullPath) {
        IDocumentProperties dp = presentation.getDocumentProperties();
        dp.setAuthor("reactome.org");
        dp.setTitle(diagram.getStableId());

        if(decorator.isDecorated()){
            fullPath = fullPath + Decorator.EXTENSION;
        }

        // full path already contains the file name and the extension.
        presentation.save(fullPath, SaveFormat.Pptx);
    }

    /**
     * Get proper instance of a node and its coordinates e size
     *
     * @param node a diagram layout node
     * @return instance of PPTXNode
     */
    private PPTXNode getNode(Node node, Adjustment adjustment, boolean flag, boolean selected) {
        PPTXNode pptxNode;
        switch (node.getSchemaClass()) {
            case "Complex":
                pptxNode = new Complex(node, adjustment, flag, selected);
                break;
            case "DefinedSet":
            case "CandidateSet":
            case "OpenSet":
                pptxNode = new EntitySet(node, adjustment, flag, selected);
                break;
            case "EntityWithAccessionedSequence":
                pptxNode = new Protein(node, adjustment, flag, selected);
                if (Objects.equals(node.getRenderableClass(), "Gene")) {
                    pptxNode = new Gene(node, adjustment, flag, selected);
                }
                if (Objects.equals(node.getRenderableClass(), "RNA")) {
                    pptxNode = new RNA(node, adjustment, flag, selected);
                }
                break;
            case "GenomeEncodedEntity":
            case "OtherEntity":
            case "Polymer":
                pptxNode = new OtherEntity(node, adjustment, flag, selected);
                break;
            case "SimpleEntity":
                pptxNode = new Chemical(node, adjustment, flag, selected);
                break;
            case "Pathway":
                pptxNode = new EncapsulatedPathway(node, adjustment, flag, selected);
                break;
            default:
                throw new IllegalArgumentException("Invalid schema class [" + node.getSchemaClass() + "]. Create the switch-case for the given class");
        }
        return pptxNode;
    }

    /**
     * Checking Software License
     *
     * @return true if the license is available and it is valid.
     */
    private boolean isLicensed() {
        InputStream is = DiagramExporter.class.getResourceAsStream("/license/Aspose.Slides.lic");
        License license = new License();
        license.setLicense(is);
        return license.isLicensed();
    }
}