package org.reactome.server.tools.diagram.exporter.pptx.parser;

import com.aspose.slides.IShapeCollection;
import com.aspose.slides.ISlide;
import com.aspose.slides.Presentation;
import com.aspose.slides.SaveFormat;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.layout.Edge;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.pptx.model.*;

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
            default:
                throw new IllegalArgumentException("Invalid schema class [" + node.getSchemaClass() + "]. Create the switch-case for the given class");
        }
        return pptxNode;
    }
}