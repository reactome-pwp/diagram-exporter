package org.reactome.server.tools.diagram.exporter.pptx.parser;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.layout.Edge;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.DiagramExporter;
import org.reactome.server.tools.diagram.exporter.pptx.model.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class DiagramPresentation {

    private Diagram diagram;
    private Presentation presentation;
    private IShapeCollection shapes;

    private Map<Long, PPTXNode> nodesMap = new HashMap<>();

    public DiagramPresentation(Diagram diagram) {
        this.diagram = diagram;
        presentation = new Presentation();
        ISlide slide = presentation.getSlides().get_Item(0);
        shapes = slide.getShapes();
    }

    public void export() {
        if(!isLicensed()) {
            // checking license before!
            // TODO WARN ? ERROR? STOP PROCESSING ? saving as evaluated version and license expired
            System.out.println("Missing Software License.");
            System.exit(1); // does not make sense continue the process here - files will be all wrong :)
        }

        // TODO: Process compartments

        for (Node node : diagram.getNodes()) {
            PPTXNode pptxNode = getNode(node);
            nodesMap.put(pptxNode.getId(), pptxNode); //If two nodes share identifier, only the second one is kept >> NOTE: It shouldn't happen //TODO: Report?
            pptxNode.render(shapes);
        }

        for (Edge edge : diagram.getEdges()) {
            PPTXReaction pptxReaction = new PPTXReaction(edge, nodesMap);
            pptxReaction.render(shapes);
        }
    }

    public void save(String path){
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
                break;
            case "GenomeEncodedEntity":
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

    private boolean isLicensed(){
        InputStream is = DiagramExporter.class.getResourceAsStream("/Aspose.Slides.lic");
        License license = new License();
        license.setLicense(is);
        return license.isLicensed();
    }
}