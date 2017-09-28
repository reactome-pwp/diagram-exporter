package org.reactome.server.tools.diagram.exporter.pptx.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class Decorator {

    public static final String EXTENSION = ".tmp";
    private Set<Long> flags = new HashSet<>();
    private Set<Long> selected = new HashSet<>();
    private Set<Long> analysis = new HashSet<>();

    public Decorator(List<Long> flags, List<Long> selected) {
        if(flags != null) this.flags = new HashSet<>(flags);
        if (selected != null) this.selected = new HashSet<>(selected);
    }

    public Decorator(List<Long> flags, List<Long> selected, List<Long> analysis) {
        if (flags != null)
            this.flags = new HashSet<>(flags);
        if (selected != null)
            this.selected = new HashSet<>(selected);
        if (analysis != null)
            this.analysis = new HashSet<>(analysis);
    }

    public Set<Long> getFlags() {
        return flags;
    }

    public Set<Long> getSelected() {
        return selected;
    }

    public Set<Long> getAnalysis() {
        return analysis;
    }

    public boolean isDecorated(){
        return !flags.isEmpty() || !selected.isEmpty() || ! analysis.isEmpty();
    }
}
