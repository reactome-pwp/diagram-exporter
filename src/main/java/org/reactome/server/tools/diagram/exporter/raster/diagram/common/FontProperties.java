package org.reactome.server.tools.diagram.exporter.raster.diagram.common;

import java.awt.*;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class FontProperties {

	public static final Font LEGEND_FONT;
	public static final Font DEFAULT_FONT;

	static {
		LEGEND_FONT = new Font("arial", Font.BOLD, 16);
		DEFAULT_FONT = new Font("arial", Font.BOLD, 8);
	}


}
