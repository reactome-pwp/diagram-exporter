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

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.PdfCanvasConstants;
import com.itextpdf.kernel.pdf.colorspace.PdfColorSpace;
import com.itextpdf.kernel.pdf.colorspace.PdfPattern;
import com.itextpdf.kernel.pdf.colorspace.PdfShading;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.kernel.pdf.function.PdfFunction;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.itext.awt.geom.PolylineShape;

import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.color.ColorSpace;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.*;
import java.util.List;

public class PdfGraphics2D extends Graphics2D {

	private static final double AFM_MULTIPLIER = 0.001; // used to calculate coordinates
	private static final int FILL = 1;
	private static final int STROKE = 2;
	private static final int CLIP = 3;
	private static final AffineTransform IDENTITY = new AffineTransform();
	protected Font font;
	protected AffineTransform transform;
	protected Color background;
	protected float x;
	protected float y;
	protected float width;
	protected float height;
	protected Stroke stroke;
	private PdfFont baseFont;
	private float fontSize;
	private Paint paint;
	private Area clip;
	private RenderingHints rhints = new RenderingHints(null);
	private Stroke originalStroke;
	private PdfCanvas canvas;
	/** Storage for BaseFont objects created. */
	private HashMap<String, PdfFont> baseFonts;

	// Added by Jurij Bilas
	private boolean underline; // indicates if the font style is underlined
	/** @since 5.0.3 */
	private boolean strikeThrough;
	private PdfExtGState fillGState[];
	private PdfExtGState strokeGState[];
	private int currentFillGState = 255;
	private int currentStrokeGState = 255;
	private BasicStroke strokeOne = new BasicStroke(1);
	private Graphics2D dg2;
	// Added by Peter Severin
	private boolean onlyShapes = false;
	private Stroke oldStroke;
	private Paint paintFill;
	private Paint paintStroke;
	private MediaTracker mediaTracker;
	private float jpegQuality = .95f;
	// Added by Alexej Suchov
	private float alpha;
	// Added by Alexej Suchov
	private Composite composite;
	// Added by Alexej Suchov
	private Paint realPaint;
	private DefaultFontMapper fontMapper = new DefaultFontMapper();

//	private PdfGraphics2D(PdfCanvas canvas) {
//		this.canvas = canvas;
//		setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//	}

	public PdfGraphics2D(PdfCanvas canvas, final float x, final float y, final float width, final float height) {
		this(canvas, x, y, width, height, false, 0);
	}

	public PdfGraphics2D(PdfCanvas canvas, final float x, final float y, final float width, final float height, final boolean onlyShapes) {
		this(canvas, x, y, width, height, onlyShapes, 0);
	}

	/**
	 * Constructor for PDFGraphics2D.
	 */
	private PdfGraphics2D(PdfCanvas canvas, final float x, final float y, float width, float height, boolean onlyShapes, float quality) {
		this.baseFont = fontMapper.awtToPdf(FontProperties.DEFAULT_FONT);
		this.fillGState = new PdfExtGState[256];
		this.strokeGState = new PdfExtGState[256];
		this.jpegQuality = quality;
		this.onlyShapes = onlyShapes;
		this.transform = new AffineTransform();
		this.baseFonts = new HashMap<>();
		paint = Color.black;
		background = Color.white;
		setFont(new Font("sanserif", Font.PLAIN, 12));
		this.canvas = canvas;
		canvas.saveState();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		clip = new Area(new Rectangle2D.Float(x, y, width, height));
		clip(clip);
		originalStroke = stroke = oldStroke = strokeOne;
		setStrokeDiff(stroke, null);
		canvas.saveState();
	}

	/**
	 * Calculates position and/or stroke thickness depending on the font size
	 *
	 * @param d value to be converted
	 * @param i font size
	 *
	 * @return position and/or stroke thickness depending on the font size
	 */
	private static double asPoints(double d, int i) {
		return d * i * AFM_MULTIPLIER;
	}

	private static com.itextpdf.kernel.colors.Color prepareColor(Color color) {
		if (color.getColorSpace().getType() == ColorSpace.TYPE_CMYK) {
			float[] comp = color.getColorComponents(null);
			return new DeviceCmyk(comp[0], comp[1], comp[2], comp[3]);
		} else {
			return new DeviceRgb(color.getRed(), color.getGreen(), color.getBlue());

		}
	}

	/**
	 * Method that creates a Graphics2D object. Contributed by Peter Harvey: he
	 * moved code from the constructor to a separate method
	 *
	 * @since 5.0.2
	 */
	private Graphics2D getDG2() {
		if (dg2 == null) {
			dg2 = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB).createGraphics();
			dg2.setRenderingHints(rhints);
		}
		return dg2;
	}

	/**
	 * @see Graphics2D#draw(Shape)
	 */
	@Override
	public void draw(Shape s) {
		followPath(s, STROKE);
	}

	/**
	 * @see Graphics2D#drawImage(Image, AffineTransform, ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		return drawImage(img, null, xform, null, obs);
	}

	/**
	 * @see Graphics2D#drawImage(BufferedImage, BufferedImageOp, int, int)
	 */
	@Override
	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
		BufferedImage result = img;
		if (op != null) {
			result = op.createCompatibleDestImage(img, img.getColorModel());
			result = op.filter(img, result);
		}
		drawImage(result, x, y, null);
	}

	/**
	 * @see Graphics2D#drawRenderedImage(RenderedImage, AffineTransform)
	 */
	@Override
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		BufferedImage image;
		if (img instanceof BufferedImage) {
			image = (BufferedImage) img;
		} else {
			ColorModel cm = img.getColorModel();
			int width = img.getWidth();
			int height = img.getHeight();
			WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
			boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
			Hashtable<String, Object> properties = new Hashtable<>();
			String[] keys = img.getPropertyNames();
			if (keys != null) {
				for (String key : keys) {
					properties.put(key, img.getProperty(key));
				}
			}
			BufferedImage result = new BufferedImage(cm, raster, isAlphaPremultiplied, properties);
			img.copyData(raster);
			image = result;
		}
		drawImage(image, xform, null);
	}

	/**
	 * @see Graphics2D#drawRenderableImage(RenderableImage, AffineTransform)
	 */
	@Override
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		drawRenderedImage(img.createDefaultRendering(), xform);
	}

	/**
	 * @see Graphics#drawString(String, int, int)
	 */
	@Override
	public void drawString(String s, int x, int y) {
		drawString(s, (float) x, (float) y);
	}

	/**
	 * This routine goes through the attributes and sets the font before calling
	 * the actual string drawing routine
	 */
	@SuppressWarnings("unchecked")
	private void doAttributes(AttributedCharacterIterator iter) {
		underline = false;
		strikeThrough = false;
		for (AttributedCharacterIterator.Attribute attribute : iter.getAttributes().keySet()) {
			if (!(attribute instanceof TextAttribute))
				continue;
			TextAttribute textattribute = (TextAttribute) attribute;
			if (textattribute.equals(TextAttribute.FONT)) {
				Font font = (Font) iter.getAttributes().get(textattribute);
				setFont(font);
			} else if (textattribute.equals(TextAttribute.UNDERLINE)) {
				if (iter.getAttributes().get(textattribute) == TextAttribute.UNDERLINE_ON)
					underline = true;
			} else if (textattribute.equals(TextAttribute.STRIKETHROUGH)) {
				if (iter.getAttributes().get(textattribute) == TextAttribute.STRIKETHROUGH_ON)
					strikeThrough = true;
			} else if (textattribute.equals(TextAttribute.SIZE)) {
				Object obj = iter.getAttributes().get(textattribute);
				if (obj instanceof Integer) {
					int i = (Integer) obj;
					setFont(getFont().deriveFont(getFont().getStyle(), i));
				} else if (obj instanceof Float) {
					float f = (Float) obj;
					setFont(getFont().deriveFont(getFont().getStyle(), f));
				}
			} else if (textattribute.equals(TextAttribute.FOREGROUND)) {
				setColor((Color) iter.getAttributes().get(textattribute));
			} else if (textattribute.equals(TextAttribute.FAMILY)) {
				Font font = getFont();
				Map<TextAttribute, Object> fontAttributes = (Map<TextAttribute, Object>) font.getAttributes();
				fontAttributes.put(TextAttribute.FAMILY, iter.getAttributes().get(textattribute));
				setFont(font.deriveFont(fontAttributes));
			} else if (textattribute.equals(TextAttribute.POSTURE)) {
				Font font = getFont();
				Map<TextAttribute, Object> fontAttributes = (Map<TextAttribute, Object>) font.getAttributes();
				fontAttributes.put(TextAttribute.POSTURE, iter.getAttributes().get(textattribute));
				setFont(font.deriveFont(fontAttributes));
			} else if (textattribute.equals(TextAttribute.WEIGHT)) {
				Font font = getFont();
				Map<TextAttribute, Object> fontAttributes = (Map<TextAttribute, Object>) font.getAttributes();
				fontAttributes.put(TextAttribute.WEIGHT, iter.getAttributes().get(textattribute));
				setFont(font.deriveFont(fontAttributes));
			}
		}
	}

	/**
	 * @see Graphics2D#drawString(String, float, float)
	 */
	@Override
	public void drawString(String s, float x, float y) {
		if (s.length() == 0)
			return;
		setFillPaint();
		if (onlyShapes) {
			drawGlyphVector(this.font.layoutGlyphVector(getFontRenderContext(), s.toCharArray(), 0, s.length(), Font.LAYOUT_LEFT_TO_RIGHT), x, y);
//            Use the following line to compile in JDK 1.3
//            drawGlyphVector(this.font.createGlyphVector(getFontRenderContext(), s), x, y);
		} else {
			// we want an untarnished clone of the transformation for use with
			// underline & strikethrough
			AffineTransform at = getTransform();
			// this object will be manipulated in case of rotation, skewing, etc.
			AffineTransform at2 = getTransform();
			at2.translate(x, y);
			at2.concatenate(font.getTransform());
			setTransform(at2);
			AffineTransform inverse = this.normalizeMatrix();
			AffineTransform flipper = AffineTransform.getScaleInstance(1, -1);
			inverse.concatenate(flipper);
			canvas.beginText();
			canvas.setFontAndSize(baseFont, fontSize);
//			// Check if we need to simulate an italic font.
//			if (font.isItalic()) {
////				float angle = baseFont.getFontDescriptor(BaseFont.ITALICANGLE, 1000);
//				float angle2 = font.getItalicAngle();
//				// When there are different fonts for italic, bold, italic bold
//				// the font.getName() will be different from the font.getFontName()
//				// value. When they are the same value then we are normally dealing
//				// with a single font that has been made into an italic or bold
//				// font. When there are only a plain and a bold font available,
//				// we need to enter this logic too. This should be identifiable
//				// by the baseFont's and font's italic angles being 0.
//				if (font.getFontName().equals(font.getName()) || (angle == 0f && angle2 == 0f)) {
//					// We don't have an italic version of this font, so we need
//					// to set the font angle ourselves to produce an italic font.
//					if (angle2 == 0) {
//						// The JavaVM didn't find an angle setting for making
//						// the font an italic font so use a default italic
//						// angle of 10 degrees.
//						angle2 = 10.0f;
//					} else {
//						// This sign of the angle for Java and PDF
//						// seems to be reversed.
//						angle2 = -angle2;
//					}
//					if (angle == 0) {
//						// We need to concatenate the skewing transformation to
//						// the original ones.
//						AffineTransform skewing = new AffineTransform();
//						skewing.setTransform(1f, 0f, (float) Math.tan(angle2 * Math.PI / 180), 1f, 0f, 0f);
//						inverse.concatenate(skewing);
//					}
//				}
//			}
			// We must wait to fetch the transformation matrix until after the
			// potential skewing transformation
			double[] mx = new double[6];
			inverse.getMatrix(mx);
			canvas.setTextMatrix((float) mx[0], (float) mx[1], (float) mx[2], (float) mx[3], (float) mx[4], (float) mx[5]);
			Float fontTextAttributeWidth = (Float) font.getAttributes().get(TextAttribute.WIDTH);
			fontTextAttributeWidth = fontTextAttributeWidth == null
					? TextAttribute.WIDTH_REGULAR
					: fontTextAttributeWidth;
			if (!TextAttribute.WIDTH_REGULAR.equals(fontTextAttributeWidth))
				canvas.setHorizontalScaling(100.0f / fontTextAttributeWidth);

			// Check if we need to simulate a bold font.
			// Do nothing if the BaseFont is already bold. This test is not foolproof but it will work most of the times.
//			if (baseFont.getPostscriptFontName().toLowerCase().indexOf("bold") < 0) {
//				// Get the weight of the font so we can detect fonts with a weight
//				// that makes them bold, while there is only a single font file.
//				Float weight = (Float) font.getAttributes().get(TextAttribute.WEIGHT);
//				if (weight == null) {
//					weight = font.isBold() ? TextAttribute.WEIGHT_BOLD
//							: TextAttribute.WEIGHT_REGULAR;
//				}
//				if (font.isBold()
//						&& (weight.floatValue() >= TextAttribute.WEIGHT_SEMIBOLD.floatValue()
//						|| font.getFontName().equals(font.getName()))) {
//					// Simulate a bold font.
//					float strokeWidth = font.getSize2D() * (weight.floatValue() - TextAttribute.WEIGHT_REGULAR.floatValue()) / 20f;
//					if (realPaint instanceof Color) {
//						cb.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE);
//						cb.setLineWidth(strokeWidth);
//						Color color = (Color) realPaint;
//						int alpha = color.getAlpha();
//						if (alpha != currentStrokeGState) {
//							currentStrokeGState = alpha;
//							PdfGState gs = strokeGState[alpha];
//							if (gs == null) {
//								gs = new PdfGState();
//								gs.setStrokeOpacity(alpha / 255f);
//								strokeGState[alpha] = gs;
//							}
//							cb.setGState(gs);
//						}
//						cb.setColorStroke(prepareColor(color));
//						restoreTextRenderingMode = true;
//					}
//				}
//			}

			double width = 0;
			if (font.getSize2D() > 0) {
				if (RenderingHints.VALUE_FRACTIONALMETRICS_OFF.equals(getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS))) {
					width = font.getStringBounds(s, getFontRenderContext()).getWidth();
				} else {
					float scale = 1000 / font.getSize2D();
					Font derivedFont = font.deriveFont(AffineTransform.getScaleInstance(scale, scale));
					width = derivedFont.getStringBounds(s, getFontRenderContext()).getWidth();
					if (derivedFont.isTransformed())
						width /= scale;
				}
			}
			if (s.length() > 1) {
				float adv = ((float) width - baseFont.getWidth(s, fontSize)) / (s.length() - 1);
				canvas.setCharacterSpacing(adv);
			}
			canvas.showText(s);
			if (s.length() > 1) {
				canvas.setCharacterSpacing(0);
			}
			if (!TextAttribute.WIDTH_REGULAR.equals(fontTextAttributeWidth))
				canvas.setHorizontalScaling(100);

			canvas.endText();
			setTransform(at);
			if (underline) {
				// These two are supposed to be taken from the .AFM file
				//int UnderlinePosition = -100;
				int UnderlineThickness = 50;
				//
				double d = asPoints(UnderlineThickness, (int) fontSize);
				Stroke savedStroke = originalStroke;
				setStroke(new BasicStroke((float) d));
				// Setting of the underline must be 2 times the d-value,
				// otherwise it might be too close to the text
				// esp. in case of a manually created bold font.
				float lineY = (float) (y + d * 2);
				Line2D line = new Line2D.Double(x, lineY, width + x, lineY);
				draw(line);
				setStroke(savedStroke);
			}
			if (strikeThrough) {
				// These two are supposed to be taken from the .AFM file
				int StrikethroughThickness = 50;
				int StrikethroughPosition = 350;
				//
				double d = asPoints(StrikethroughThickness, (int) fontSize);
				double p = asPoints(StrikethroughPosition, (int) fontSize);
				Stroke savedStroke = originalStroke;
				setStroke(new BasicStroke((float) d));
				y = (float) (y + asPoints(StrikethroughThickness, (int) fontSize));
				Line2D line = new Line2D.Double(x, y - p, width + x, y - p);
				draw(line);
				setStroke(savedStroke);
			}
		}
	}

	/**
	 * @see Graphics#drawString(AttributedCharacterIterator, int, int)
	 */
	@Override
	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		drawString(iterator, (float) x, (float) y);
	}

	/**
	 * @see Graphics2D#drawString(AttributedCharacterIterator, float, float)
	 */
	@Override
	public void drawString(AttributedCharacterIterator iter, float x, float y) {
/*
        StringBuffer sb = new StringBuffer();
        for(char c = iter.first(); c != AttributedCharacterIterator.DONE; c = iter.next()) {
            sb.append(c);
        }
        drawString(sb.toString(),x,y);
*/
		StringBuilder builder = new StringBuilder(iter.getEndIndex());
		for (char c = iter.first(); c != '\uFFFF'; c = iter.next()) {
			if (iter.getIndex() == iter.getRunStart()) {
				if (builder.length() > 0) {
					drawString(builder.toString(), x, y);
					FontMetrics fontmetrics = getFontMetrics();
					x = (float) (x + fontmetrics.getStringBounds(builder.toString(), this).getWidth());
					builder.delete(0, builder.length());
				}
				doAttributes(iter);
			}
			builder.append(c);
		}

		drawString(builder.toString(), x, y);
		underline = false;
		strikeThrough = false;
	}

	/**
	 * @see Graphics2D#drawGlyphVector(GlyphVector, float, float)
	 */
	@Override
	public void drawGlyphVector(GlyphVector g, float x, float y) {
		Shape s = g.getOutline(x, y);
		fill(s);
	}

	/**
	 * @see Graphics2D#fill(Shape)
	 */
	@Override
	public void fill(Shape s) {
		followPath(s, FILL);
	}

	/**
	 * @see Graphics2D#hit(Rectangle, Shape, boolean)
	 */
	@Override
	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
		if (onStroke) {
			s = stroke.createStrokedShape(s);
		}
		s = transform.createTransformedShape(s);
		Area area = new Area(s);
		if (clip != null)
			area.intersect(clip);
		return area.intersects(rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * @see Graphics2D#getDeviceConfiguration()
	 */
	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		return getDG2().getDeviceConfiguration();
	}

	private Stroke transformStroke(Stroke stroke) {
		if (!(stroke instanceof BasicStroke))
			return stroke;
		BasicStroke st = (BasicStroke) stroke;
		float scale = (float) Math.sqrt(Math.abs(transform.getDeterminant()));
		float dash[] = st.getDashArray();
		if (dash != null) {
			for (int k = 0; k < dash.length; ++k)
				dash[k] *= scale;
		}
		return new BasicStroke(st.getLineWidth() * scale, st.getEndCap(), st.getLineJoin(), st.getMiterLimit(), dash, st.getDashPhase() * scale);
	}

	private void setStrokeDiff(Stroke newStroke, Stroke oldStroke) {
		if (newStroke == oldStroke)
			return;
		if (!(newStroke instanceof BasicStroke))
			return;
		BasicStroke nStroke = (BasicStroke) newStroke;
		boolean oldOk = oldStroke instanceof BasicStroke;
		BasicStroke oStroke = null;
		if (oldOk)
			oStroke = (BasicStroke) oldStroke;
		if (!oldOk || nStroke.getLineWidth() != oStroke.getLineWidth())
			canvas.setLineWidth(nStroke.getLineWidth());
		if (!oldOk || nStroke.getEndCap() != oStroke.getEndCap()) {
			switch (nStroke.getEndCap()) {
				case BasicStroke.CAP_BUTT:
					canvas.setLineCapStyle(PdfCanvasConstants.LineCapStyle.BUTT);
					break;
				case BasicStroke.CAP_SQUARE:
					canvas.setLineCapStyle(PdfCanvasConstants.LineCapStyle.PROJECTING_SQUARE);
					break;
				default:
					canvas.setLineCapStyle(PdfCanvasConstants.LineCapStyle.ROUND);
			}
		}
		if (!oldOk || nStroke.getLineJoin() != oStroke.getLineJoin()) {
			switch (nStroke.getLineJoin()) {
				case BasicStroke.JOIN_MITER:
					canvas.setLineJoinStyle(PdfCanvasConstants.LineJoinStyle.MITER);
					break;
				case BasicStroke.JOIN_BEVEL:
					canvas.setLineJoinStyle(PdfCanvasConstants.LineJoinStyle.BEVEL);
					break;
				default:
					canvas.setLineJoinStyle(PdfCanvasConstants.LineJoinStyle.ROUND);
			}
		}
		if (!oldOk || nStroke.getMiterLimit() != oStroke.getMiterLimit())
			canvas.setMiterLimit(nStroke.getMiterLimit());
		boolean makeDash;
		if (oldOk) {
			if (nStroke.getDashArray() != null) {
				if (nStroke.getDashPhase() != oStroke.getDashPhase()) {
					makeDash = true;
				} else
					makeDash = !Arrays.equals(nStroke.getDashArray(), oStroke.getDashArray());
			} else makeDash = oStroke.getDashArray() != null;
		} else {
			makeDash = true;
		}
		if (makeDash) {
			float dash[] = nStroke.getDashArray();
//			canvas.setLineDash(dash, 0);
//			if (dash == null)
//				cb.setLiteral("[]0 d\n");
//			else {
//				cb.setLiteral('[');
//				int lim = dash.length;
//				for (int k = 0; k < lim; ++k) {
//					cb.setLiteral(dash[k]);
//					cb.setLiteral(' ');
//				}
//				cb.setLiteral(']');
//				cb.setLiteral(nStroke.getDashPhase());
//				cb.setLiteral(" d\n");
//			}
		}
	}

	/**
	 * Sets a rendering hint
	 */
	@Override
	public void setRenderingHint(Key hintKey, Object hintValue) {
		if (hintValue != null) rhints.put(hintKey, hintValue);
		if (dg2 != null) dg2.setRenderingHint(hintKey, hintValue);
	}

	/**
	 * @param hintKey a key
	 *
	 * @return the rendering hint
	 */
	@Override
	public Object getRenderingHint(Key hintKey) {
		return rhints.get(hintKey);
	}

	/**
	 * @see Graphics2D#addRenderingHints(Map)
	 */
	@Override
	public void addRenderingHints(Map<?, ?> hints) {
		rhints.putAll(hints);
		if (dg2 != null) {
			dg2.addRenderingHints(hints);
		}
	}

	/**
	 * @see Graphics2D#getRenderingHints()
	 */
	@Override
	public RenderingHints getRenderingHints() {
		return rhints;
	}

	/**
	 * @see Graphics2D#setRenderingHints(Map)
	 */
	@Override
	public void setRenderingHints(Map<?, ?> hints) {
		rhints.clear();
		rhints.putAll(hints);
		if (dg2 != null) {
			dg2.setRenderingHints(hints);
		}
	}

	/**
	 * @see Graphics#translate(int, int)
	 */
	@Override
	public void translate(int x, int y) {
		translate((double) x, (double) y);
	}

	/**
	 * @see Graphics2D#translate(double, double)
	 */
	@Override
	public void translate(double tx, double ty) {
		transform.translate(tx, ty);
	}

	/**
	 * @see Graphics2D#rotate(double)
	 */
	@Override
	public void rotate(double theta) {
		transform.rotate(theta);
	}

	/**
	 * @see Graphics2D#rotate(double, double, double)
	 */
	@Override
	public void rotate(double theta, double x, double y) {
		transform.rotate(theta, x, y);
	}

	/**
	 * @see Graphics2D#scale(double, double)
	 */
	@Override
	public void scale(double sx, double sy) {
		transform.scale(sx, sy);
		this.stroke = transformStroke(originalStroke);
	}

	/**
	 * @see Graphics2D#shear(double, double)
	 */
	@Override
	public void shear(double shx, double shy) {
		transform.shear(shx, shy);
	}

	/**
	 * @see Graphics2D#transform(AffineTransform)
	 */
	@Override
	public void transform(AffineTransform tx) {
		transform.concatenate(tx);
		this.stroke = transformStroke(originalStroke);
	}

	/**
	 * @see Graphics2D#getTransform()
	 */
	@Override
	public AffineTransform getTransform() {
		return new AffineTransform(transform);
	}

	/**
	 * @see Graphics2D#setTransform(AffineTransform)
	 */
	@Override
	public void setTransform(AffineTransform t) {
		transform = new AffineTransform(t);
		this.stroke = transformStroke(originalStroke);
	}

	/**
	 * Method contributed by Alexej Suchov
	 *
	 * @see Graphics2D#getPaint()
	 */
	@Override
	public Paint getPaint() {
		if (realPaint != null) {
			return realPaint;
		} else {
			return paint;
		}
	}

	/**
	 * Method contributed by Alexej Suchov
	 *
	 * @see Graphics2D#setPaint(Paint)
	 */
	@Override
	public void setPaint(Paint paint) {
		if (paint == null)
			return;
		this.paint = paint;
		realPaint = paint;

		if (composite instanceof AlphaComposite && paint instanceof Color) {

			AlphaComposite co = (AlphaComposite) composite;

			if (co.getRule() == 3) {
				Color c = (Color) paint;
				this.paint = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (c.getAlpha() * alpha));
				realPaint = paint;
			}
		}

	}

	private void setPaint(boolean fill) {
		if (paint instanceof Color) {
			Color color = (Color) paint;
			int alpha = color.getAlpha();
			if (fill) {
				if (alpha != currentFillGState) {
					currentFillGState = alpha;
					PdfExtGState gs = fillGState[alpha];
					if (gs == null) {
						gs = new PdfExtGState();
						gs.setFillOpacity(alpha / 255f);
						fillGState[alpha] = gs;
					}
					canvas.setExtGState(gs);
				}
				canvas.setFillColor(prepareColor(color));
			} else {
				if (alpha != currentStrokeGState) {
					currentStrokeGState = alpha;
					PdfExtGState gs = strokeGState[alpha];
					if (gs == null) {
						gs = new PdfExtGState();
						gs.setStrokeOpacity(alpha / 255f);
						strokeGState[alpha] = gs;
					}
					canvas.setExtGState(gs);
				}
				canvas.setStrokeColor(prepareColor(color));
			}
		} else if (paint instanceof GradientPaint) {
			final GradientPaint gp = (GradientPaint) paint;
			final Point2D p1 = gp.getPoint1();
			transform.transform(p1, p1);
			final Point2D p2 = gp.getPoint2();
			transform.transform(p2, p2);
			final Color c1 = gp.getColor1();
			final Color c2 = gp.getColor2();
			final PdfShading.Axial shading = new PdfShading.Axial(DeviceRgb.RED.getColorSpace(), (float) p1.getX(), normalizeY((float) p1.getY()), c1.getRGBComponents(null), (float) p2.getX(), normalizeY((float) p2.getY()), c2.getRGBComponents(null));
			final PdfPattern.Shading pattern = new PdfPattern.Shading(shading);
			if (fill) canvas.setFillColorShading(pattern);
			else canvas.setStrokeColorShading(pattern);
		} else if (paint instanceof LinearGradientPaint) {
			final LinearGradientPaint gp = (LinearGradientPaint) paint;
			final Point2D p1 = gp.getStartPoint();
			transform.transform(p1, p1);
			final Point2D p2 = gp.getEndPoint();
			transform.transform(p2, p2);
			final float[][] colors = Arrays.stream(gp.getColors()).map(color -> color.getColorComponents(null)).toArray(float[][]::new);
			final AxialShading shading = new AxialShading(DeviceRgb.RED.getColorSpace(), (float) p1.getX(), normalizeY((float) p1.getY()), (float) p2.getX(), normalizeY((float) p2.getY()), colors);
//			final PdfShading.Axial shading = new PdfShading.Axial(DeviceRgb.RED.getColorSpace(), (float) p1.getX(), normalizeY((float) p1.getY()), c1.getRGBComponents(null), (float) p2.getX(), normalizeY((float) p2.getY()), c2.getRGBComponents(null));
			final PdfPattern.Shading pattern = new PdfPattern.Shading(shading);
			if (fill) canvas.setFillColorShading(pattern);
			else canvas.setStrokeColorShading(pattern);
		} else if (paint instanceof TexturePaint) {
//			try {
//				TexturePaint tp = (TexturePaint) paint;
//				BufferedImage img = tp.getImage();
//				Rectangle2D rect = tp.getAnchorRect();
//				com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(img, null);
//				PdfPatternPainter pattern = canvas.createPattern(image.getWidth(), image.getHeight());
//				AffineTransform inverse = this.normalizeMatrix();
//				inverse.translate(rect.getX(), rect.getY());
//				inverse.scale(rect.getWidth() / image.getWidth(), -rect.getHeight() / image.getHeight());
//				double[] mx = new double[6];
//				inverse.getMatrix(mx);
//				pattern.setPatternMatrix((float) mx[0], (float) mx[1], (float) mx[2], (float) mx[3], (float) mx[4], (float) mx[5]);
//				image.setAbsolutePosition(0, 0);
//				pattern.addImage(image);
//				if (fill)
//					canvas.setPatternFill(pattern);
//				else
//					canvas.setPatternStroke(pattern);
//			} catch (Exception ex) {
//				if (fill)
//					canvas.setColorFill(BaseColor.GRAY);
//				else
//					canvas.setColorStroke(BaseColor.GRAY);
//			}
		} else {
			try {
				BufferedImage img = null;
				int type = BufferedImage.TYPE_4BYTE_ABGR;
				if (paint.getTransparency() == Transparency.OPAQUE) {
					type = BufferedImage.TYPE_3BYTE_BGR;
				}
				img = new BufferedImage((int) width, (int) height, type);
				Graphics2D g = (Graphics2D) img.getGraphics();
				g.transform(transform);
				AffineTransform inv = transform.createInverse();
				Shape fillRect = new Rectangle2D.Double(0, 0, img.getWidth(), img.getHeight());
				fillRect = inv.createTransformedShape(fillRect);
				g.setPaint(paint);
				g.fill(fillRect);
				g.dispose();
			} catch (Exception ex) {
				if (fill)
					canvas.setFillColor(new DeviceGray(0.5f));
				else
					canvas.setStrokeColor(new DeviceGray(0.5f));
			}
		}
	}

	/**
	 * @see Graphics2D#getComposite()
	 */
	@Override
	public Composite getComposite() {
		return composite;
	}

	/**
	 * Method contributed by Alexej Suchov
	 *
	 * @see Graphics2D#setComposite(Composite)
	 */
	@Override
	public void setComposite(Composite comp) {

		if (comp instanceof AlphaComposite) {

			AlphaComposite composite = (AlphaComposite) comp;

			if (composite.getRule() == 3) {

				alpha = composite.getAlpha();
				this.composite = composite;

				if (realPaint instanceof Color) {

					Color c = (Color) realPaint;
					paint = new Color(c.getRed(), c.getGreen(), c.getBlue(),
							(int) (c.getAlpha() * alpha));
				}
				return;
			}
		}

		this.composite = comp;
		alpha = 1.0F;

	}

	/**
	 * @see Graphics2D#getBackground()
	 */
	@Override
	public Color getBackground() {
		return background;
	}

	/**
	 * @see Graphics2D#setBackground(Color)
	 */
	@Override
	public void setBackground(Color color) {
		background = color;
	}

	/**
	 * @see Graphics2D#getStroke()
	 */
	@Override
	public Stroke getStroke() {
		return originalStroke;
	}

	/**
	 * @see Graphics2D#setStroke(Stroke)
	 */
	@Override
	public void setStroke(Stroke s) {
		originalStroke = s;
		this.stroke = transformStroke(s);
	}

	/**
	 * @see Graphics2D#getFontRenderContext()
	 */
	@Override
	public FontRenderContext getFontRenderContext() {
		boolean antialias = RenderingHints.VALUE_TEXT_ANTIALIAS_ON.equals(getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING));
		boolean fractions = RenderingHints.VALUE_FRACTIONALMETRICS_ON.equals(getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS));
		return new FontRenderContext(new AffineTransform(), antialias, fractions);
	}

	/**
	 * @see Graphics#create()
	 */
	@Override
	public Graphics create() {
		PdfGraphics2D g2 = new PdfGraphics2D(canvas, x, y, width, height, onlyShapes, jpegQuality);
		g2.rhints.putAll(this.rhints);
//		g2.onlyShapes = this.onlyShapes;
		g2.transform = new AffineTransform(this.transform);
		g2.baseFonts = this.baseFonts;
		g2.paint = this.paint;
		g2.fillGState = this.fillGState;
		g2.currentFillGState = this.currentFillGState;
		g2.strokeGState = this.strokeGState;
		g2.background = this.background;
		g2.mediaTracker = this.mediaTracker;
//		g2.jpegQuality = this.jpegQuality;
		g2.setFont(this.font);
		g2.canvas.saveState();
//		g2.width = this.width;
//		g2.height = this.height;
		g2.followPath(new Area(new Rectangle2D.Float(0, 0, width, height)), CLIP);
		if (this.clip != null)
			g2.clip = new Area(this.clip);
		g2.composite = composite;
		g2.stroke = stroke;
		g2.originalStroke = originalStroke;
		g2.strokeOne = (BasicStroke) g2.transformStroke(g2.strokeOne);
		g2.oldStroke = g2.strokeOne;
		g2.setStrokeDiff(g2.oldStroke, null);
		g2.canvas.saveState();
		if (g2.clip != null)
			g2.followPath(g2.clip, CLIP);
		return g2;
	}

	/**
	 * @see Graphics#getColor()
	 */
	@Override
	public Color getColor() {
		if (paint instanceof Color) {
			return (Color) paint;
		} else {
			return Color.black;
		}
	}

	/**
	 * @see Graphics#setColor(Color)
	 */
	@Override
	public void setColor(Color color) {
		setPaint(color);
	}

	/**
	 * @see Graphics#setPaintMode()
	 */
	@Override
	public void setPaintMode() {}

	/**
	 * @see Graphics#setXORMode(Color)
	 */
	@Override
	public void setXORMode(Color c1) {

	}

	/**
	 * @see Graphics#getFont()
	 */
	@Override
	public Font getFont() {
		return font;
	}

	/**
	 * Sets the current font.
	 *
	 * @see Graphics#setFont(Font)
	 */
	@Override
	public void setFont(Font f) {
		if (f == null)
			return;
		if (onlyShapes) {
			font = f;
			return;
		}
		if (f == font)
			return;
		font = f;
		fontSize = f.getSize2D();
		baseFont = getCachedBaseFont(f);
	}

	private synchronized PdfFont getCachedBaseFont(Font f) {
		return baseFonts.computeIfAbsent(f.getFontName(), s -> fontMapper.awtToPdf(f));
	}

	/**
	 * @see Graphics#getFontMetrics(Font)
	 */
	@Override
	public FontMetrics getFontMetrics(Font f) {
		return getDG2().getFontMetrics(f);
	}

	/**
	 * @see Graphics#getClipBounds()
	 */
	@Override
	public Rectangle getClipBounds() {
		if (clip == null)
			return null;
		return getClip().getBounds();
	}

	/**
	 * @see Graphics#clipRect(int, int, int, int)
	 */
	@Override
	public void clipRect(int x, int y, int width, int height) {
		Rectangle2D rect = new Rectangle2D.Double(x, y, width, height);
		clip(rect);
	}

	/**
	 * @see Graphics#setClip(int, int, int, int)
	 */
	@Override
	public void setClip(int x, int y, int width, int height) {
		Rectangle2D rect = new Rectangle2D.Double(x, y, width, height);
		setClip(rect);
	}

	/**
	 * @see Graphics2D#clip(Shape)
	 */
	@Override
	public void clip(Shape s) {
		if (s == null) {
			setClip(null);
			return;
		}
		s = transform.createTransformedShape(s);
		if (clip == null)
			clip = new Area(s);
		else
			clip.intersect(new Area(s));
		followPath(s, CLIP);
	}

	/**
	 * @see Graphics#getClip()
	 */
	@Override
	public Shape getClip() {
		try {
			return transform.createInverse().createTransformedShape(clip);
		} catch (NoninvertibleTransformException e) {
			return null;
		}
	}

	/**
	 * @see Graphics#setClip(Shape)
	 */
	@Override
	public void setClip(Shape s) {
		canvas.restoreState();
		canvas.saveState();
		if (s != null)
			s = transform.createTransformedShape(s);
		if (s == null) {
			clip = null;
		} else {
			clip = new Area(s);
			followPath(s, CLIP);
		}
		paintFill = paintStroke = null;
		currentFillGState = currentStrokeGState = -1;
		oldStroke = strokeOne;
	}

	/**
	 * @see Graphics#copyArea(int, int, int, int, int, int)
	 */
	@Override
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {

	}

	/**
	 * @see Graphics#drawLine(int, int, int, int)
	 */
	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		Line2D line = new Line2D.Double(x1, y1, x2, y2);
		draw(line);
	}

	/**
	 * @see Graphics#fillRect(int, int, int, int)
	 */
	@Override
	public void drawRect(int x, int y, int width, int height) {
		draw(new Rectangle(x, y, width, height));
	}

	/**
	 * @see Graphics#fillRect(int, int, int, int)
	 */
	@Override
	public void fillRect(int x, int y, int width, int height) {
		fill(new Rectangle(x, y, width, height));
	}

	/**
	 * @see Graphics#clearRect(int, int, int, int)
	 */
	@Override
	public void clearRect(int x, int y, int width, int height) {
		Paint temp = paint;
		setPaint(background);
		fillRect(x, y, width, height);
		setPaint(temp);
	}

	/**
	 * @see Graphics#drawRoundRect(int, int, int, int, int, int)
	 */
	@Override
	public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
		RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width, height, arcWidth, arcHeight);
		draw(rect);
	}

	/**
	 * @see Graphics#fillRoundRect(int, int, int, int, int, int)
	 */
	@Override
	public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
		RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width, height, arcWidth, arcHeight);
		fill(rect);
	}

	/**
	 * @see Graphics#drawOval(int, int, int, int)
	 */
	@Override
	public void drawOval(int x, int y, int width, int height) {
		Ellipse2D oval = new Ellipse2D.Float(x, y, width, height);
		draw(oval);
	}

	/**
	 * @see Graphics#fillOval(int, int, int, int)
	 */
	@Override
	public void fillOval(int x, int y, int width, int height) {
		Ellipse2D oval = new Ellipse2D.Float(x, y, width, height);
		fill(oval);
	}

	/**
	 * @see Graphics#drawArc(int, int, int, int, int, int)
	 */
	@Override
	public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		Arc2D arc = new Arc2D.Double(x, y, width, height, startAngle, arcAngle, Arc2D.OPEN);
		draw(arc);

	}

	/**
	 * @see Graphics#fillArc(int, int, int, int, int, int)
	 */
	@Override
	public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		Arc2D arc = new Arc2D.Double(x, y, width, height, startAngle, arcAngle, Arc2D.PIE);
		fill(arc);
	}

	/**
	 * @see Graphics#drawPolyline(int[], int[], int)
	 */
	@Override
	public void drawPolyline(int[] x, int[] y, int nPoints) {
		PolylineShape polyline = new PolylineShape(x, y, nPoints);
		draw(polyline);
	}

	/**
	 * @see Graphics#drawPolygon(int[], int[], int)
	 */
	@Override
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		Polygon poly = new Polygon(xPoints, yPoints, nPoints);
		draw(poly);
	}

	/**
	 * @see Graphics#fillPolygon(int[], int[], int)
	 */
	@Override
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		Polygon poly = new Polygon();
		for (int i = 0; i < nPoints; i++) {
			poly.addPoint(xPoints[i], yPoints[i]);
		}
		fill(poly);
	}

	/**
	 * @see Graphics#drawImage(Image, int, int, ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		return drawImage(img, x, y, null, observer);
	}

	/**
	 * @see Graphics#drawImage(Image, int, int, int, int, ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
		return drawImage(img, x, y, width, height, null, observer);
	}

	/**
	 * @see Graphics#drawImage(Image, int, int, Color, ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
		waitForImage(img);
		return drawImage(img, x, y, img.getWidth(observer), img.getHeight(observer), bgcolor, observer);
	}

	/**
	 * @see Graphics#drawImage(Image, int, int, int, int, Color, ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
		waitForImage(img);
		double scalex = width / (double) img.getWidth(observer);
		double scaley = height / (double) img.getHeight(observer);
		AffineTransform tx = AffineTransform.getTranslateInstance(x, y);
		tx.scale(scalex, scaley);
		return drawImage(img, null, tx, bgcolor, observer);
	}

	/**
	 * @see Graphics#drawImage(Image, int, int, int, int, int, int, int, int,
	 * ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
		return drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null, observer);
	}

	/**
	 * @see Graphics#drawImage(Image, int, int, int, int, int, int, int, int,
	 * Color, ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
		waitForImage(img);
		double dwidth = (double) dx2 - dx1;
		double dheight = (double) dy2 - dy1;
		double swidth = (double) sx2 - sx1;
		double sheight = (double) sy2 - sy1;

		//if either width or height is 0, then there is nothing to draw
		if (dwidth == 0 || dheight == 0 || swidth == 0 || sheight == 0)
			return true;

		double scalex = dwidth / swidth;
		double scaley = dheight / sheight;

		double transx = sx1 * scalex;
		double transy = sy1 * scaley;
		AffineTransform tx = AffineTransform.getTranslateInstance(dx1 - transx, dy1 - transy);
		tx.scale(scalex, scaley);

		BufferedImage mask = new BufferedImage(img.getWidth(observer), img.getHeight(observer), BufferedImage.TYPE_BYTE_BINARY);
		Graphics g = mask.getGraphics();
		g.fillRect(sx1, sy1, (int) swidth, (int) sheight);
		drawImage(img, mask, tx, null, observer);
		g.dispose();
		return true;
	}

	///////////////////////////////////////////////
	//
	//
	//		implementation specific methods
	//
	//

	/**
	 * @see Graphics#dispose()
	 */
	@Override
	public void dispose() {

	}

	private void followPath(Shape s, int drawType) {
		if (s == null) return;
		if (drawType == STROKE) {
			if (!(stroke instanceof BasicStroke)) {
				s = stroke.createStrokedShape(s);
				followPath(s, FILL);
				return;
			}
		}
		if (drawType == STROKE) {
			setStrokeDiff(stroke, oldStroke);
			oldStroke = stroke;
			setStrokePaint();
		} else if (drawType == FILL)
			setFillPaint();
		PathIterator points;
		int traces = 0;
		if (drawType == CLIP)
			points = s.getPathIterator(IDENTITY);
		else
			points = s.getPathIterator(transform);
		float[] coords = new float[6];
		double[] dcoords = new double[6];
		while (!points.isDone()) {
			++traces;
			// Added by Peter Harvey (start)
			int segtype = points.currentSegment(dcoords);
			int numpoints = (segtype == PathIterator.SEG_CLOSE ? 0
					: (segtype == PathIterator.SEG_QUADTO ? 2
					: (segtype == PathIterator.SEG_CUBICTO ? 3
					: 1)));
			for (int i = 0; i < numpoints * 2; i++) {
				coords[i] = (float) dcoords[i];
			}
			// Added by Peter Harvey (end)
			normalizeY(coords);
			switch (segtype) {
				case PathIterator.SEG_CLOSE:
					canvas.closePath();
					break;

				case PathIterator.SEG_CUBICTO:
					canvas.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
					break;

				case PathIterator.SEG_LINETO:
					canvas.lineTo(coords[0], coords[1]);
					break;

				case PathIterator.SEG_MOVETO:
					canvas.moveTo(coords[0], coords[1]);
					break;

				case PathIterator.SEG_QUADTO:
					canvas.curveTo(coords[0], coords[1], coords[2], coords[3]);
					break;
			}
			points.next();
		}
		switch (drawType) {
			case FILL:
				if (traces > 0) {
					if (points.getWindingRule() == PathIterator.WIND_EVEN_ODD)
						canvas.eoFill();
					else
						canvas.fill();
				}
				break;
			case STROKE:
				if (traces > 0)
					canvas.stroke();
				break;
			default: //drawType==CLIP
				if (traces == 0)
					canvas.rectangle(0, 0, 0, 0);
				if (points.getWindingRule() == PathIterator.WIND_EVEN_ODD)
					canvas.eoClip();
				else
					canvas.clip();
				canvas.newPath();
		}
	}

	private float normalizeY(float y) {
		return this.height - y;
	}

	private void normalizeY(float[] coords) {
		coords[1] = normalizeY(coords[1]);
		coords[3] = normalizeY(coords[3]);
		coords[5] = normalizeY(coords[5]);
	}

	private AffineTransform normalizeMatrix() {
		double[] mx = new double[6];
		AffineTransform result = AffineTransform.getTranslateInstance(0, 0);
		result.getMatrix(mx);
		mx[3] = -1;
		mx[5] = height;
		result = new AffineTransform(mx);
		result.concatenate(transform);
		return result;
	}

	private boolean drawImage(Image img, Image mask, AffineTransform xform, Color bgColor, ImageObserver obs) {
		if (xform == null)
			xform = new AffineTransform();
		else
			xform = new AffineTransform(xform);
		xform.translate(0, img.getHeight(obs));
		xform.scale(img.getWidth(obs), img.getHeight(obs));

		AffineTransform inverse = this.normalizeMatrix();
		AffineTransform flipper = AffineTransform.getScaleInstance(1, -1);
		inverse.concatenate(xform);
		inverse.concatenate(flipper);

		double[] mx = new double[6];
		inverse.getMatrix(mx);
		if (currentFillGState != 255) {
			PdfExtGState gs = fillGState[255];
			if (gs == null) {
				gs = new PdfExtGState();
				gs.setFillOpacity(1);
				fillGState[255] = gs;
			}
			canvas.setExtGState(gs);
		}

		try {
			final ImageData image = ImageDataFactory.create(img, bgColor);
			if (mask != null) {
				ImageDataFactory.create(mask, null, true);
			}
			canvas.addImage(image, (float) mx[0], (float) mx[1], (float) mx[2], (float) mx[3], (float) mx[4], (float) mx[5]);
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex);
		}
		if (currentFillGState >= 0 && currentFillGState != 255) {
			PdfExtGState gs = fillGState[currentFillGState];
			canvas.setExtGState(gs);
		}
		return true;
	}

	private boolean checkNewPaint(Paint oldPaint) {
		if (paint == oldPaint)
			return false;
		return !(paint instanceof Color && paint.equals(oldPaint));
	}

	private void setFillPaint() {
		if (checkNewPaint(paintFill)) {
			paintFill = paint;
			setPaint(true);
		}
	}

	private void setStrokePaint() {
		if (checkNewPaint(paintStroke)) {
			paintStroke = paint;
			setPaint(false);
		}
	}

	private synchronized void waitForImage(Image image) {
		if (mediaTracker == null)
			mediaTracker = new MediaTracker(new PdfGraphics2D.FakeComponent());
		mediaTracker.addImage(image, 0);
		try {
			mediaTracker.waitForID(0);
		} catch (InterruptedException e) {
			// empty on purpose
		}
		mediaTracker.removeImage(image);
	}

	static private class FakeComponent extends Component {

		private static final long serialVersionUID = 6450197945596086638L;
	}

	class AxialShading extends PdfShading {

		AxialShading(PdfColorSpace cs, float x0, float y0, float x1, float y1, float[][] colors) {
			super(new PdfDictionary(), 2, cs);
			getPdfObject().put(PdfName.Coords, new PdfArray(new float[]{x0, y0, x1, y1}));
			final List<PdfObject> functions = new ArrayList<>(colors.length - 1);
			final PdfNumber n = new PdfNumber(2);
			final float[] encode = new float[2 * (colors.length - 1)];
			for (int i = 0; i < colors.length - 1; i++) {
				final PdfArray domain = new PdfArray(new float[]{0, 1});
				final PdfArray c0 = new PdfArray(colors[i]);
				final PdfArray c1 = new PdfArray(colors[i + 1]);
				functions.add(new PdfFunction.Type2(domain, null, c0, c1, n).getPdfObject());
				encode[2 * i] = 0;
				encode[2 * i + 1] = 1;
			}

			final float[] bounds = new float[colors.length - 2];
			for (int i = 0; i < colors.length - 2; i++)
				bounds[i] = (float) (i + 1) / (colors.length - 1);
			final PdfDictionary function = new PdfDictionary();
			function.put(PdfName.Functions, new PdfArray(functions));
			function.put(PdfName.FunctionType, new PdfNumber(3));
			function.put(PdfName.Domain, new PdfArray(new float[]{0, 1}));
			function.put(PdfName.Bounds, new PdfArray(bounds));
			function.put(PdfName.Encode, new PdfArray(encode));
			getPdfObject().put(PdfName.Function, function);
			setModified();
		}
	}

}
