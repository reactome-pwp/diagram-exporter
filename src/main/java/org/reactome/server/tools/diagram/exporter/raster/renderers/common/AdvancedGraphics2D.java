package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Renders Reactome elements over a Graphics object. As Reactome elements use
 * shared figures, such as corned rectangles or ovals, this class offers common
 * methods to easily draw them. It has to be initialized with a factor, so every
 * time a draw method is used, shapes are properly scaled.
 */
public class AdvancedGraphics2D {

	private static final List<Character> WORD_SPLIT_CHARS = Arrays.asList(':', '.', '-', ',', ')', '/', '+');
	private final Graphics2D graphics;
	private final double factor;
	private final BufferedImage image;

	/**
	 * Creates a AdvancedGraphics2D that will draw into graphics using factor*
	 */
	public AdvancedGraphics2D(double width, double height, double factor, double x, double y, double margin) {
		this.image = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
		this.graphics = image.createGraphics();
		graphics.translate(margin, margin);
		graphics.translate((int) -x, (int) -y);
		this.graphics.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		this.factor = factor;
		getGraphics().setFont(ColorProfile.DEFAULT_FONT);
	}

	/**
	 * Gets the inner Graphics element. This will allow you to directly draw
	 * into the image. Be careful, you have to manually apply the factor.
	 * Usually you can factor NodeProperties by using ScaledNodeProperties:
	 * <code>
	 * <pre>
	 * ScaledNodeProperties prop = new ScaledNodeProperties(properties, factor)
	 * </pre>
	 * </code>
	 * And, as Graphics only work with ints, you should also cast values to int.
	 * We provide a decorator for that, the IntNodeProperties:
	 * <code>
	 * <pre>
	 * IntNodeProperties prop = new IntNodeProperties(properties)
	 * int x = prop.intX();
	 * </pre>
	 * </code>
	 * You can nest both decorators:
	 * <code>
	 * <pre>
	 * IntNodeProperties prop = new IntNodeProperties(new ScaledNodeProperties(properties, factor));
	 * int scaledX = prop.intX;
	 * </pre>
	 * </code>
	 *
	 * @return the inner Graphics
	 */
	public Graphics2D getGraphics() {
		return graphics;
	}

	public BufferedImage getImage() {
		return image;
	}

	/**
	 * Gets the factor at which elements are being resized
	 *
	 * @return
	 */
	public double getFactor() {
		return factor;
	}

	/**
	 * Draws a cross.
	 *
	 * @param prop node properties
	 */
	public void drawCross(NodeProperties prop) {
		drawCross(prop.getX(), prop.getY(), prop.getWidth(), prop.getHeight());
	}

	/**
	 * Draws a cross.
	 *
	 * @param x      top left x coordinate
	 * @param y      top left y coordinate
	 * @param width  cross width
	 * @param height cross height
	 */
	public void drawCross(double x, double y, double width, double height) {
		final int intX = Double.valueOf(factor * x).intValue();
		final int intY = Double.valueOf(factor * y).intValue();
		final int intW = Double.valueOf(factor * width).intValue();
		final int intH = Double.valueOf(factor * height).intValue();
		getGraphics().drawLine(intX, intY, intX + intW, intY + intH);
		getGraphics().drawLine(intX, intY + intH, intX + intW, intY);

	}

	public void drawRectangle(double x, double y, double width, double height) {
		final int intX = Double.valueOf(factor * x).intValue();
		final int intY = Double.valueOf(factor * y).intValue();
		final int intW = Double.valueOf(factor * width).intValue();
		final int intH = Double.valueOf(factor * height).intValue();
		graphics.drawRect(intX, intY, intW, intH);
	}

	/**
	 * Draws a text with the default font, using position as the top left corner
	 * of text. Text will be drawn in 1 line.
	 *
	 * @param text     text to display
	 * @param position top left coordinate
	 */
	public void drawTextSingleLine(String text, Coordinate position) {
		final int leftX = Double.valueOf(RendererProperties.NODE_TEXT_PADDING + factor * (position.getX())).intValue();
		final int topY = Double.valueOf(RendererProperties.NODE_TEXT_PADDING + factor * position.getY()).intValue();

		final int height = getGraphics().getFontMetrics().getHeight();
		final int baseY = topY + height;

		getGraphics().drawString(text, leftX, baseY);
	}

	public void drawText(String text, double x, double y, double width, double height) {
		drawText(text, x, y, width, height, RendererProperties.NODE_TEXT_PADDING);
	}

	public void drawText(String text, double x, double y, double width, double height, double padding) {
		final double scaledX = factor * (x + padding);
		final double scaledY = factor * (y + padding);
		final double scaledW = factor * (width - 2 * padding);
		final double scaledH = factor * (height - 2 * padding);
		final double centerX = scaledX + scaledW * 0.5;

		Font font = getGraphics().getFont();
		List<String> lines;
		while ((lines = fit(scaledW, scaledH, text, font)) == null)
			font = font.deriveFont((float) font.getSize() - 1);
		final Font old = graphics.getFont();
		graphics.setFont(font);
		final int textHeight = lines.size() * getGraphics().getFontMetrics().getHeight();
		final double yOffset = scaledY + (scaledH - textHeight) * 0.5 + graphics.getFontMetrics().getAscent();
		for (int i = 0; i < lines.size(); i++) {
			final String line = lines.get(i);
			final int lineWidth = getGraphics().getFontMetrics().charsWidth(line.toCharArray(), 0, line.length());
			final int base = (int) (yOffset + i * getGraphics().getFontMetrics().getHeight());
			int left = (int) (centerX - 0.5 * lineWidth);
			getGraphics().drawString(line, left, base);
		}
		graphics.setFont(old);
//		}
	}

	/**
	 * Draws text using all available space in node.getProp()
	 *
	 * @param node
	 */
	public void drawText(Node node) {
		drawText(node.getDisplayName(), node.getProp().getX(),
				node.getProp().getY(), node.getProp().getWidth(),
				node.getProp().getHeight());
	}

	public void drawText(Node node, double padding) {
		drawText(node.getDisplayName(), node.getProp().getX(),
				node.getProp().getY(), node.getProp().getWidth(),
				node.getProp().getHeight(), padding);
	}

	/**
	 * @param availableWidth
	 * @param availableHeight
	 * @param text
	 * @param font
	 * @return
	 */
	private List<String> fit(double availableWidth, double availableHeight, String text, Font font) {
		final List<String> lines = new LinkedList<>();
		final String[] words = text.trim().split(" ");
		StringBuilder line = new StringBuilder();
		for (String word : words) {
			String temp = line.toString() + " " + word;
			if (getGraphics().getFontMetrics(font).charsWidth(temp.toCharArray(), 0, temp.length()) < availableWidth)
				line.append(" ").append(word);
			else {
				final List<String> parts = splitWord(word);
				boolean first = true;
				for (String part : parts) {
					if (first && line.length() > 0)
						temp = line.toString() + " " + part;
					else temp = line.toString() + part;
					if (getGraphics().getFontMetrics(font).charsWidth(temp.toCharArray(), 0, temp.length()) < availableWidth) {
						if (first) {
							line.append(" ");
							first = false;
						}
						line.append(part);
					} else {
						lines.add(line.toString().trim());
						line = new StringBuilder(part);
					}
				}
			}
		}
		if (!line.toString().trim().isEmpty())
			lines.add(line.toString().trim());
		if (lines.size() * getGraphics().getFontMetrics(font).getHeight() <= availableHeight)
			return lines;
		else return null;
	}

	/**
	 * Will split a word by any character in WORD_SPLIT_CHARS
	 * <pre>{':', '.', '-', ',', ')', '/', '+'}</pre>
	 * the split characters are inserted as the last character of the fragment
	 *
	 * For instance <pre>splitWord("p-T402-PAK2(213-524))</pre> will result in
	 * <pre>{"p-", "T402-", "PAK2(", "213-", "524)"}</pre>
	 *
	 * @param word
	 * @return
	 */
	private List<String> splitWord(String word) {
		final List<String> parts = new LinkedList<>();
		int start = 0;
		for (int i = 0; i < word.length(); i++) {
			if (WORD_SPLIT_CHARS.contains(word.charAt(i))) {
				parts.add(word.substring(start, i + 1));
				start = i + 1;
			}
		}
		final String end = word.substring(start);
		if (!end.isEmpty()) parts.add(end);
		return parts;
	}

	public void fillRect(double x, double y, double width, double height) {
		getGraphics().fillRect(
				(int) (factor * x),
				(int) (factor * y),
				(int) (factor * width),
				(int) (factor * height)
		);
	}

}
