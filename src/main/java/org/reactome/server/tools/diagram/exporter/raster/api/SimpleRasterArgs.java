package org.reactome.server.tools.diagram.exporter.raster.api;

import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SimpleRasterArgs implements RasterArgs {

	private String stId;
	private Double factor = 1.;
	private String format;
	private String token;
	private Set<String> flags;
	private Set<String> selected;

	private ColorProfiles profiles;
	private Color background;
	private Integer column;

	public SimpleRasterArgs(String stId, String format) {
		this.stId = stId;
		this.format = format;
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
		this.format = format == null ? "png" : format.trim().toLowerCase();
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

	public void setBackground(Color color) {
		this.background = color;
	}

	/**
	 * In case an expression analysis is run, the column to show. Leave it null
	 * to generate a GIF with all the columns. May take longer.
	 */
	public Integer getColumn() {
		return column;
	}

	public void setColumn(Integer column) {
		this.column = column;
	}

	public Set<String> getFlags() {
		return flags;
	}

	public void setFlags(Collection<String> flags) {
		if (flags != null)
			this.flags = new HashSet<>(flags);
	}

	public Set<String> getSelected() {
		return selected;
	}

	public void setSelected(Collection<String> selected) {
		if (selected != null)
			this.selected = new HashSet<>(selected);
	}
}
