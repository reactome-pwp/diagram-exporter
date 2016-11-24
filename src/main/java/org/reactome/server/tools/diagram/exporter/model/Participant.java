package org.reactome.server.tools.diagram.exporter.model;

import java.util.LinkedList;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public abstract class Participant {
    Long id;
    PPTXShape pptxShape;
    Stoichiometry stoichiometry;
    PPTXNode pptxNode;
    LinkedList<PPTXSegment> pptxSegments; // or connectors

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PPTXShape getPptxShape() {
        return pptxShape;
    }

    public void setPptxShape(PPTXShape pptxShape) {
        this.pptxShape = pptxShape;
    }

    public Stoichiometry getStoichiometry() {
        return stoichiometry;
    }

    public void setStoichiometry(Stoichiometry stoichiometry) {
        this.stoichiometry = stoichiometry;
    }

    public PPTXNode getPptxNode() {
        return pptxNode;
    }

    public void setPptxNode(PPTXNode pptxNode) {
        this.pptxNode = pptxNode;
    }

    public LinkedList<PPTXSegment> getPptxSegments() {
        return pptxSegments;
    }

    public void setPptxSegments(LinkedList<PPTXSegment> pptxSegments) {
        this.pptxSegments = pptxSegments;
    }

    public void addPPTXSegment(PPTXSegment pptxSegment) {
        if (pptxSegments == null) {
            pptxSegments = new LinkedList<>();
        }
        pptxSegments.add(pptxSegment);
    }

    public abstract void render();

}
