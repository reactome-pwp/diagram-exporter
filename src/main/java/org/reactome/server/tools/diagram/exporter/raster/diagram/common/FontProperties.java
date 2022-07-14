package org.reactome.server.tools.diagram.exporter.raster.diagram.common;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.exporter.raster.resources.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class FontProperties {
	private static final Logger logger = LoggerFactory.getLogger("diagram-exporter");

	public static final Font LEGEND_FONT;
	public static final Font DEFAULT_FONT;

	public static PdfFont REGULAR;

	static {
		try {
			DEFAULT_FONT = Font.createFont(Font.TRUETYPE_FONT, Resources.class.getResourceAsStream("fonts/arialbd.ttf")).deriveFont(8f);
			LEGEND_FONT = DEFAULT_FONT.deriveFont(16f);
			byte[] bytes;
			bytes = IOUtils.toByteArray(Resources.class.getResourceAsStream("fonts/arialbd.ttf"));
			REGULAR = PdfFontFactory.createFont(bytes, PdfEncodings.UTF8,  PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
		} catch (FontFormatException | IOException e) {
			// resources shouldn't throw exceptions
			logger.error("Couldn't load font", e);
			throw new RuntimeException("Couldn't load fonts", e);
		}
	}


}
