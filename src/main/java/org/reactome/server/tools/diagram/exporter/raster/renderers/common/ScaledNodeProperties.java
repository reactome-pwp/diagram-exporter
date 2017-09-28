package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import org.reactome.server.tools.diagram.data.layout.NodeProperties;

public class ScaledNodeProperties implements NodeProperties {

	private final double scale;
	private final NodeProperties properties;

	public ScaledNodeProperties(NodeProperties properties, double scale) {
		this.properties = properties;
		this.scale = scale;
	}

	@Override
	public Double getX() {
		return scale * properties.getX();
	}

	@Override
	public Double getY() {
		return scale * properties.getY();
	}

	@Override
	public Double getWidth() {
		return scale * properties.getWidth();
	}

	@Override
	public Double getHeight() {
		return scale * properties.getHeight();
	}
}
