package org.reactome.server.tools.diagram.exporter.raster.diagram.common;

import org.reactome.server.tools.diagram.exporter.raster.resources.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class FontProperties {
	private static final Logger logger = LoggerFactory.getLogger("infoLogger");

	public static final Font LEGEND_FONT;
	public static final Font DEFAULT_FONT;

	static {
		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, Resources.class.getResourceAsStream("fonts/arial.ttf")));
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, Resources.class.getResourceAsStream("fonts/arialbd.ttf")));
		} catch (FontFormatException | IOException e) {
			// resources shouldn't throw exceptions
			logger.error("Couldn't load font", e);
		}
		LEGEND_FONT = new Font("arial", Font.BOLD, 16);
		DEFAULT_FONT = new Font("arial", Font.BOLD, 8);
	}


}
