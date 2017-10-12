package org.reactome.server.tools.diagram.exporter.common;

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
    private String token;

    public Decorator(List<Long> flags, List<Long> selected, String token) {
        this.token = token;
        if(flags != null) this.flags = new HashSet<>(flags);
        if (selected != null) this.selected = new HashSet<>(selected);
    }

    public Set<Long> getFlags() {
        return flags;
    }

    public Set<Long> getSelected() {
        return selected;
    }

    public boolean isDecorated(){
        return !flags.isEmpty() || !selected.isEmpty() || token != null;
    }

    public String getToken() {
        return token;
    }
}
