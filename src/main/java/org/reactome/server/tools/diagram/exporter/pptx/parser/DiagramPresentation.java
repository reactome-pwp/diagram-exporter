package org.reactome.server.tools.diagram.exporter.pptx.parser;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.DiagramExporter;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.pptx.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

import static org.reactome.server.tools.diagram.exporter.pptx.util.PPTXShape.reorder;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 * @author Guilherme Viteri <gviteri@ebi.ac.uk>
 */
public class DiagramPresentation {

    private static final Logger logger = LoggerFactory.getLogger("infoLogger");

    private Diagram diagram;
    private DiagramProfile profile;
    private Presentation presentation;
    private IShapeCollection shapes;
    private Map<Long, PPTXNode> nodesMap = new HashMap<>();
    private Decorator decorator;

    public DiagramPresentation(Diagram diagram, DiagramProfile profile, Decorator decorator) {
        this.diagram = diagram;
        this.profile = profile;
        presentation = new Presentation();
        ISlide slide = presentation.getSlides().get_Item(0);
        shapes = slide.getShapes();
        this.decorator = decorator;
    }

    public void export() {
        Adjustment adjustment = new Adjustment(diagram);

        // Set slide size.
        Dimension pageSize = new Dimension(adjustment.getSlideWidth(), adjustment.getSlideHeight());
        presentation.getSlideSize().setSize(pageSize);

        // Render Compartments
        logger.debug("Exporting compartments");
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
        if(diagram.getNotes() != null && !diagram.getNotes().isEmpty()) {
            logger.debug("Exporting notes");
            for (Note note : diagram.getNotes()) {
                CompartmentNote compartmentNote = new CompartmentNote(note, profile, adjustment);
                compartmentNote.render(shapes);
            }
        }

        // Render Nodes
        logger.debug("Exporting nodes");
        for (Node node : diagram.getNodes()) {
            boolean flag = decorator != null && decorator.getFlags().contains(node.getReactomeId());
            boolean selected = decorator != null && decorator.getSelected().contains(node.getReactomeId());
            PPTXNode pptxNode = getNode(node, adjustment, flag, selected);
            nodesMap.put(pptxNode.getId(), pptxNode); //If two nodes share identifier, only the second one is kept >> NOTE: It shouldn't happen //TODO: Report?
            pptxNode.render(shapes, profile);
        }

        // Render Edges
        logger.debug("Exporting Edges");
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
        if (!diseaseEdges.isEmpty()) {
            logger.debug("Rendering diseases edges");
            for (Edge diseaseEdge : diseaseEdges) {
                boolean selected = decorator != null && decorator.getSelected().contains(diseaseEdge.getReactomeId());
                PPTXReaction pptxReaction = new PPTXReaction(diseaseEdge, nodesMap, profile, adjustment, selected);
                pptxReaction.render(shapes);
            }
        }

        // Render Links
        if(diagram.getLinks() != null && !diagram.getLinks().isEmpty()) {
            logger.debug("Rendering links");
            for (Link link : diagram.getLinks()) {
                PPTXLink pptxLink = new PPTXLink(link, nodesMap, adjustment);
                pptxLink.render(shapes, profile);
            }

        }

        // REORDERING SHAPES: All nodes in the were brought to front
        reorder(shapes, nodesMap.values());
    }

    /**
     * Saving the powerpoint based on the given file full path.
     *
     * @return the pptx file
     */
    public File save(String outputFolder, String stId, String license) {
        License lic = getLicense(license);
        if (!lic.isLicensed()) {
            logger.warn("3rd party library does not have a valid license. Evaluation version will be created.");
        }

        // setting some document properties
        IDocumentProperties dp = presentation.getDocumentProperties();
        dp.setAuthor("reactome.org");
        dp.setTitle(diagram.getStableId());

        // To avoid two+ files being generated at the same time having different flg and sel (and one can overwrite the other)
        // we are adding the UUID as part of the filename when decorators are present. Otherwise we keep the original getName.
        // We must have an unique getName before saving.
        final String fileExtension = ".pptx";
        File file = new File(outputFolder, stId + fileExtension);
        if(decorator.isDecorated()){
            logger.info("Decoration is enabled, temporary file is being generated", file.getPath());
            String pptxDecor = stId + "-" + UUID.randomUUID().toString();
            file = new File(outputFolder, pptxDecor + fileExtension);
        }

        // full path already contains the file getName and the extension.
        presentation.save(file.getPath(), SaveFormat.Pptx);
        return file;
    }

    /**
     * Get proper instance of a node and its coordinates e size
     *
     * @param node a diagram layout node
     * @return instance of PPTXNode
     */
    private PPTXNode getNode(Node node, Adjustment adjustment, boolean flag, boolean selected) {
        PPTXNode pptxNode = null;
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
            case "ChemicalDrug":
                pptxNode = new ChemicalDrug(node, adjustment, flag, selected);
                break;
            case "Pathway":
                if (Objects.equals(node.getRenderableClass(), "ProcessNode")) {
                    pptxNode = new EncapsulatedPathway(node, adjustment, flag, selected);
                }
                if (Objects.equals(node.getRenderableClass(), "EncapsulatedNode")) {
                    pptxNode = new EncapsulatedNode(node, adjustment, flag, selected);
                }
                break;
            default:
                logger.error("Invalid schema class [{}]. Create the switch-case for the given class", node.getSchemaClass());
                throw new IllegalArgumentException("Invalid schema class [" + node.getSchemaClass() + "]. Create the switch-case for the given class");
        }

        if (pptxNode == null) {
            logger.error("Invalid renderable class [{}]. Create the switch-case for the given class", node.getRenderableClass());
            throw new IllegalArgumentException("Invalid renderable class [" + node.getRenderableClass() + "]. Create the switch-case for the given class");
        }

        return pptxNode;
    }

    /**
     * Get Software License
     *
     */
    private License getLicense(String licFilePath) {
        InputStream is = DiagramExporter.class.getResourceAsStream("/license/Aspose.Slides.lic");
        if(licFilePath != null && !licFilePath.isEmpty()){
            try {
                is = new FileInputStream(new File(licFilePath));
            } catch (FileNotFoundException e) {
                // nothing here, evaluation version will be exported
            }
        }

        License license = new License();
        license.setLicense(is);
        return license;
    }
}
