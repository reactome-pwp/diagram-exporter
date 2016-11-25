package org.reactome.server.tools.diagram.exporter.pptx.model;

import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class PPTXDiagram {

    private List<Note> notes;
    private List<Compartment> compartments;
    private List<PPTXReaction> reactions;

    public List<Compartment> getCompartments() {
        return compartments;
    }

    public void setCompartments(List<Compartment> compartments) {
        this.compartments = compartments;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public List<PPTXReaction> getReactions() {
        return reactions;
    }

    public void setReactions(List<PPTXReaction> reactions) {
        this.reactions = reactions;
    }
}
