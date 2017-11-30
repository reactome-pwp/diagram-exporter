package org.reactome.server.tools.diagram.exporter.raster.api;

import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RasterArgs implements RasterArgs {

	private String stId;
	private Double factor = 1.;
	private String format;
	private String token;
	private Set<String> flags;
	private Set<String> selected;
	private ColorProfiles profiles;
	private Color background;
	private Integer column;
	private String resource;
	private Boolean writeTitle;

	public RasterArgs(String stId, String format) {
		this.stId = stId;
		setFormat(format);
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

	public void setQuality(Integer quality) {
		if (quality != null)
			this.factor = scale(quality);
	}

	private double scale(int factor) {
		if (factor < 1 || factor > 10)
			throw new IllegalArgumentException("factor must be in the range [1-10]");
		if (factor < 5) {
			return interpolate(factor, 1, 5, 0.1, 1);
		} else return interpolate(factor, 5, 10, 1, 3);
	}

	private double interpolate(double x, double min, double max, double dest_min, double dest_max) {
		return (x - min) / (max - min) * (dest_max - dest_min) + dest_min;
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

	@Override
	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	@Override
	public Boolean getWriteTitle() {
		return writeTitle;
	}

	public void setWriteTitle(Boolean writeTitle) {
		this.writeTitle = writeTitle;
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
