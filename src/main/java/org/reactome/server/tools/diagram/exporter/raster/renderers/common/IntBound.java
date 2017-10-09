package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import org.reactome.server.tools.diagram.data.layout.Bound;

/**
 * Decorator for Bound. It will add intX, intY, intWidth and intHeight, so you
 * don't need to cast them outside.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class IntBound implements Bound {

	private Bound bound;

	public IntBound(Bound bound) {
		this.bound = bound;
	}

	@Override
	public Double getX() {
		return bound.getX();
	}

	@Override
	public Double getY() {
		return bound.getY();
	}

	@Override
	public Double getWidth() {
		return bound.getWidth();
	}

	@Override
	public Double getHeight() {
		return bound.getHeight();
	}

	public int intX() {
		return bound.getX().intValue();
	}

	public int intY() {
		return bound.getY().intValue();
	}

	public int intHeight() {
		return bound.getHeight().intValue();
	}

	public int intWidth() {
		return bound.getWidth().intValue();
	}

}
