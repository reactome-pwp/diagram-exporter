/*
 *
 * This file is part of the iText (R) project.
    Copyright (c) 1998-2018 iText Group NV
 * Authors: Bruno Lowagie, Paulo Soares, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package org.reactome.server.tools.diagram.exporter.raster.itext.awt;

import com.itextpdf.io.font.FontNames;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.FontStyles;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.exporter.raster.resources.Resources;

import java.awt.*;
import java.io.IOException;

/**
 * Default class to map awt fonts to BaseFont.
 *
 * @author Paulo Soares
 */

public class DefaultFontMapper {

	private PdfFont REGULAR;
	private PdfFont BOLD;


	public DefaultFontMapper() {
		byte[] bytes;
		try {
			bytes = IOUtils.toByteArray(Resources.class.getResourceAsStream("fonts/arial.ttf"));
			REGULAR = PdfFontFactory.createFont(bytes, PdfEncodings.UTF8, true, true);
			bytes = IOUtils.toByteArray(Resources.class.getResourceAsStream("fonts/arialbd.ttf"));
			BOLD = PdfFontFactory.createFont(bytes, PdfEncodings.UTF8, true, true);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Returns a BaseFont which can be used to represent the given AWT Font
	 *
	 * @param font the font to be converted
	 *
	 * @return a BaseFont which has similar properties to the provided Font
	 */
	public PdfFont awtToPdf(Font font) {
		if (font.isBold()) return BOLD;
		return REGULAR;
//		try {
//			// fontName - either a font alias, if the font file has been registered with an alias, or just a font name otherwise
//			// encoding - the encoding of the font to be created. See PdfEncodings
//			// embedded - indicates whether the font is to be embedded into the target document
//			// style - the style of the font to look for. Possible values are listed in FontStyles. See FontStyles.BOLD, FontStyles.ITALIC, FontStyles.NORMAL, FontStyles.BOLDITALIC, FontStyles.UNDEFINED
//			// cached - whether to try to get the font program from cache
//			FontProgramFactory.createRegisteredFont(font.getPSName(), extractItextStyle(font));
////			return PdfFontFactory.createRegisteredFont(font.getPSName(), PdfEncodings.UTF8, false, extractItextStyle(font), true);
//		} catch (IOException e) {
//			LoggerFactory.getLogger(DefaultFontMapper.class).error("Font " + font.getName() + " is not registered");
//		}
//		return null;
	}

	private int extractAwtStyle(PdfFont font) {
		final FontNames fontNames = font.getFontProgram().getFontNames();
		int style = Font.PLAIN;
		if (fontNames.isBold()) style |= Font.BOLD;
		if (fontNames.isItalic()) style |= Font.ITALIC;
		return style;
	}

	/**
	 * Returns an AWT Font which can be used to represent the given BaseFont
	 *
	 * @param font the font to be converted
	 * @param size the desired point size of the resulting font
	 *
	 * @return a Font which has similar properties to the provided BaseFont
	 */

	public  Font pdfToAwt(PdfFont font, int size) {
		final String fontName = font.getFontProgram().getFontNames().getFontName();
		//noinspection MagicConstant
		return Font.getFont(fontName)
				.deriveFont(extractAwtStyle(font), size);
	}

	private  int extractItextStyle(Font font) {
		if (font.isItalic() && font.isBold()) return FontStyles.BOLDITALIC;
		if (font.isBold()) return FontStyles.BOLD;
		if (font.isItalic()) return FontStyles.ITALIC;
		if (font.isPlain()) return FontStyles.NORMAL;
		return FontStyles.UNDEFINED;
	}
}
