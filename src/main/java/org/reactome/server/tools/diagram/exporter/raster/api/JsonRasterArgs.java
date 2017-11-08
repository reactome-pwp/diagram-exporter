package org.reactome.server.tools.diagram.exporter.raster.api;

import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JsonRasterArgs implements RasterArgs {


	private String stId;
	private Double factor = 1.;
	private String format;
	private String token;
	private Set<String> flags;
	private Set<String> selected;

	private ColorProfiles profiles;
	private Color background;
	private Integer column;

	public JsonRasterArgs() {
	}

	public String getStId() {
		return stId;
	}

	public void setStId(String stId) {
		this.stId = stId;
	}

	public Double getFactor() {
		return factor;
	}

	public void setFactor(Double factor) {
		this.factor = factor;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

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

	public Set<String> getFlags() {
		return flags;
	}

	public void setFlags(java.util.List<String> flags) {
		this.flags = new HashSet<>(flags);
	}

	public Set<String> getSelected() {
		return selected;
	}

	public void setSelected(List<String> selected) {
		this.selected = new HashSet<>(selected);
	}
}
