package org.reactome.server.tools.diagram.exporter.pptx.model;

import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class Reaction {

    private Long id;
    private Long stId; // stId does not have R-HSA..


    private PPTXShape pptxShape;
    private PPTXSegment backboneInput;
    private PPTXSegment backboneOutput;
    private List<PPTXNode> participants;
    private String reactionType; // maybe we should use an Enumeration or maybe we don't need and the pptx will deal with.


    public PPTXSegment getBackboneInput() {
        return backboneInput;
    }

    public void setBackboneInput(PPTXSegment backboneInput) {
        this.backboneInput = backboneInput;
    }

    public PPTXSegment getBackboneOutput() {
        return backboneOutput;
    }

    public void setBackboneOutput(PPTXSegment backboneOutput) {
        this.backboneOutput = backboneOutput;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<PPTXNode> getParticipants() {
        return participants;
    }

    public void setParticipants(List<PPTXNode> participants) {
        this.participants = participants;
    }

    public String getReactionType() {
        return reactionType;
    }

    public void setReactionType(String reactionType) {
        this.reactionType = reactionType;
    }

    public PPTXShape getPptxShape() {
        return pptxShape;
    }

    public void setPptxShape(PPTXShape pptxShape) {
        this.pptxShape = pptxShape;
    }

    public Long getStId() {
        return stId;
    }

    public void setStId(Long stId) {
        this.stId = stId;
    }

    public String  render(){

        // take all parameters and pptx the reaction.
        return "Please, PPT pptx a reaction!";
    }
}
