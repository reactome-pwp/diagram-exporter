package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import java.awt.*;

public class FontProperties {

	public static Font DEFAULT_FONT;
	public static Font SHADOWS_FONT;

	static {
		setFactor(1.0);
	}

	public static void setFactor(double factor) {

		FontProperties.SHADOWS_FONT = new Font("arial", Font.BOLD, (int) (24 * factor));
		FontProperties.DEFAULT_FONT = new Font("arial", Font.BOLD, (int) (9 * factor));
	}

}
