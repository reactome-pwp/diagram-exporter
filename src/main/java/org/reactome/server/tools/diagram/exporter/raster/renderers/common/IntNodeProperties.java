package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import org.reactome.server.tools.diagram.data.layout.NodeProperties;

/**
 * Wraps a NodeProperties object and adds the <code>intX, intY, intWidth and
 * intHeight</code> methods, so no casting is needed.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class IntNodeProperties implements NodeProperties {

	private NodeProperties properties;

	/**
	 * Creates a IntNodeProperties that contains a NodeProperties inside.
	 *
	 * @param properties object to be wrapped
	 */
	public IntNodeProperties(NodeProperties properties) {
		this.properties = properties;
	}

	@Override
	public Double getX() {
		return properties.getX();
	}

	@Override
	public Double getY() {
		return properties.getY();
	}

	@Override
	public Double getWidth() {
		return properties.getWidth();
	}

	@Override
	public Double getHeight() {
		return properties.getHeight();
	}

	public int intX() {
		return properties.getX().intValue();
	}

	public int intY() {
		return properties.getY().intValue();
	}

	public int intHeight() {
		return properties.getHeight().intValue();
	}

	public int intWidth() {
		return properties.getWidth().intValue();
	}

}
