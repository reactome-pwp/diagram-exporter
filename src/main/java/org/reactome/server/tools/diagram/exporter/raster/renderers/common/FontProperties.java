package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import java.awt.*;

public class FontProperties {

	public static Font DEFAULT_FONT;
	public static Font SHADOWS_FONT;

	static {
		setFactor(1.0);
	}

	public static void setFactor(double factor) {
		final int shadowSize = (int) Math.max(1, 24 * factor);
		final int defaultSize = (int) Math.max(1, 9 * factor);

		FontProperties.SHADOWS_FONT = new Font("arial", Font.BOLD, shadowSize);
		FontProperties.DEFAULT_FONT = new Font("arial", Font.BOLD, defaultSize);
	}

}
