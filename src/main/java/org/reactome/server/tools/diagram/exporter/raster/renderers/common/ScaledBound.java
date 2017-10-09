package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import org.reactome.server.tools.diagram.data.layout.Bound;

/**
 * Wrapper for Bound that returns the values scaled.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ScaledBound implements Bound {

	private final double scale;
	private final Bound bound;

	public ScaledBound(Bound bound, double scale) {
		this.scale = scale;
		this.bound = bound;
	}

	@Override
	public Double getX() {
		return scale * bound.getX();
	}

	@Override
	public Double getY() {
		return scale * bound.getY();
	}

	@Override
	public Double getWidth() {
		return scale * bound.getWidth();
	}

	@Override
	public Double getHeight() {
		return scale * bound.getHeight();
	}
}
