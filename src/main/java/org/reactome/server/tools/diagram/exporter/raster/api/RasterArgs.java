package org.reactome.server.tools.diagram.exporter.raster.api;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Arguments for the diagram exporter */
public class RasterArgs {

	private String stId;
	private Double factor = 1.;
	private String format;
	private String token;
	// TODO: change decorator for a selected, flags field
	private Decorator decorator;

	@JsonIgnore
	private Set<String> flags = new HashSet<>();
	@JsonIgnore
	private Set<String> selected = new HashSet<>();

	private ColorProfiles profiles;
	private Color background;
	private Integer column;

	public RasterArgs(String stId, String format) {
		this.stId = stId;
		this.format = format;
	}

	public RasterArgs() {
	}

	/** diagram stable identifier */
	public String getStId() {
		return stId;
	}

	public void setStId(String stId) {
		this.stId = stId;
	}

	/** quality of output image. number of pixels per point in the diagram */
	public Double getFactor() {
		return factor;
	}

	public void setFactor(Double factor) {
		this.factor = factor;
	}

	/** output image format (png, jpg, gif) */
	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	/** color profiles for diagram, analysis and interactors */
	public ColorProfiles getProfiles() {
		if (profiles == null)
			profiles = new ColorProfiles(null, null, null);
		return profiles;
	}

	public void setProfiles(ColorProfiles profiles) {
		this.profiles = profiles;
	}

	/** selection and flagging */
	public Decorator getDecorator() {
		return decorator;
	}

	public void setDecorator(Decorator decorator) {
		this.decorator = decorator;
	}

	public String getToken() {
		return token;
	}

	/** Analysis token */
	public void setToken(String token) {
		this.token = token;
	}

	/** Background color when no transparency is available */
	public Color getBackground() {
		return background;
	}

	public void setBackground(String color) {
		this.background = ColorFactory.parseColor(color);
	}

	/**
	 * In case an expression analysis is run, the column to show. Leave it null
	 * to generate a GIF with all the columns. May take longer.
	 */
	public Integer getColumn() {
		return column;
	}

	public void setSelected(List<String> selected) {
		this.selected = new HashSet<>(selected);
	}

	public void setFlags(List<String> flags) {
		this.flags = new HashSet<>(flags);
	}

	public Set<String> getFlags() {
		return flags;
	}

	public Set<String> getSelected() {
		return selected;
	}
}
