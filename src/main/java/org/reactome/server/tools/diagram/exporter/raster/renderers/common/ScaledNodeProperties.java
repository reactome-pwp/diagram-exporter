package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import org.reactome.server.tools.diagram.data.layout.NodeProperties;

// TODO: what about an interface that already scales, returns ints and accept both NodeProperties and Bound

/**
 * Wrapper for NodeProperties that returns values scaled.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
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
