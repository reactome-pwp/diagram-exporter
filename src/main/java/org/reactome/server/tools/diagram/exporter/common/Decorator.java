package org.reactome.server.tools.diagram.exporter.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

	public Decorator() {
	}

	public Decorator(List<Long> flags, List<Long> selected) {
		if (flags != null) this.flags = new HashSet<>(flags);
		if (selected != null) this.selected = new HashSet<>(selected);
	}

	/**
	 * set of selected elements in the diagram. Values are reactome
	 * identifiers.
	 */
	public Set<Long> getFlags() {
		return flags;
	}

	/**
	 * set of flag elements in the diagram. Values are reactome identifiers.
	 */
	public Set<Long> getSelected() {
		return selected;
	}

	@JsonIgnore
	public boolean isDecorated() {
		return !flags.isEmpty() || !selected.isEmpty();
	}

	public void setFlags(List<Long> flags) {
		if (flags != null) this.flags = new HashSet<>(flags);
	}

	public void setSelected(List<Long> selected) {
		if (selected != null) this.selected = new HashSet<>(selected);
	}
}
