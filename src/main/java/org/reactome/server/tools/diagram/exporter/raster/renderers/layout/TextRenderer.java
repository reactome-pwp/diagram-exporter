package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class TextRenderer {

	private static final List<Character> WORD_SPLIT_CHARS = Arrays.asList(':', '.', '-', ',', ')', '/', '+');

	/**
	 * Magic method that displays text in the assigned space. If the text does
	 * not fit in one line, it is split in several lines. If the font size is
	 * very large, it is lower until the text fits. Each line is centered to the
	 * box and the whole text is vertically centered. By default the vertical
	 * centering is natural, i.e. text is centered at hyphens (-) height the
	 * center of the box. This is a little big higher than strict center and is
	 * visually better for large texts. A padding of RenderedProperties.NODE_TEXT_PADDING
	 * is added by default.
	 */
	private static void drawText(Graphics2D graphics2D, String text, NodeProperties limits) {
		drawText(graphics2D, text, limits.getX(), limits.getY(), limits.getWidth(), limits.getHeight());
	}

	private static void drawText(Graphics2D graphics2D, String text, double x, double y, double width, double height) {
		final double centerX = x + width * 0.5;
		Font font = graphics2D.getFont();
		List<String> lines;
		while ((lines = fit(graphics2D, width, height, text, font)) == null)
			font = font.deriveFont((float) font.getSize() - 1);
		// Impossible to fit even at font size 1
		if (lines.isEmpty()) return;
		final Font old = graphics2D.getFont();
		graphics2D.setFont(font);
		final int textHeight = lines.size() * graphics2D.getFontMetrics().getHeight();
		double yOffset = y + (height - textHeight) * 0.5;
		yOffset += graphics2D.getFontMetrics().getAscent();
//		else yOffset += 0.5 * (graphics.getFontMetrics().getAscent() + graphics.getFontMetrics().getDescent());
		for (int i = 0; i < lines.size(); i++) {
			final String line = lines.get(i);
			final int lineWidth = graphics2D.getFontMetrics().charsWidth(line.toCharArray(), 0, line.length());
			final int base = (int) (yOffset + i * graphics2D.getFontMetrics().getHeight());
			int left = (int) (centerX - 0.5 * lineWidth);
			graphics2D.drawString(line, left, base);
		}
		graphics2D.setFont(old);
	}

	/**
	 * @return a list of lines if text can be fit inside availableWidth and
	 * availableHeight. If text does not fit, returns null. If font size is less
	 * than 1, then it returns an empty list to indicate that text is impossible
	 * to be shown, probably because image size is too small.
	 */
	private static List<String> fit(Graphics2D graphics, double availableWidth, double availableHeight, String text, Font font) {
		// Reached the limit of font size
		if (font.getSize() < 1) return Collections.emptyList();
		final List<String> lines = new LinkedList<>();
		final String[] words = text.trim().split(" ");
		String line = "";
		String temp;
		for (String word : words) {
			temp = line.isEmpty() ? word : line + " " + word;
			if (computeWidth(graphics, font, temp) < availableWidth)
				line = temp;
			else {
				// Split word in smaller parts and add as much parts as possible
				// to the line
				final List<String> parts = splitWord(word);
				boolean firstPart = true;
				for (String part : parts) {
					// If the part can't fit a line, the text won't fit
					if (computeWidth(graphics, font, part) > availableWidth)
						return null;
					if (line.isEmpty()) temp = part;
					else if (firstPart) temp = line + " " + part;
					else temp = line + part;
					if (computeWidth(graphics, font, temp) < availableWidth) {
						line = temp;
					} else {
						// Start a new line with part
						lines.add(line);
						line = part;
						if (computeHeight(graphics, font, lines) > availableHeight)
							return null;
					}
					firstPart = false;
				}
			}
		}
		if (!line.isEmpty()) lines.add(line);
		if (computeHeight(graphics, font, lines) > availableHeight)
			return null;
		else return lines;
	}

	private static int computeHeight(Graphics2D graphics, Font font, List<String> lines) {
		return lines.size() * graphics.getFontMetrics(font).getHeight();
	}

	private static int computeWidth(Graphics2D graphics, Font font, String temp) {
		return graphics.getFontMetrics(font).charsWidth(temp.toCharArray(), 0, temp.length());
	}

	/**
	 * Will split a word by any character in WORD_SPLIT_CHARS
	 * <pre>:.-,)/+</pre>
	 * The split characters are inserted as the last character of the fragment
	 *
	 * For instance <pre>splitWord("p-T402-PAK2(213-524)")</pre> will result in
	 * <pre>{"p-", "T402-", "PAK2(", "213-", "524)"}</pre>
	 */
	private static List<String> splitWord(String word) {
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

	public static void drawTextSingleLine(Graphics2D graphics, String text, Coordinate position) {
		drawTextSingleLine(graphics, text, position.getX(), position.getY());
	}

	private static void drawTextSingleLine(Graphics2D graphics, String text, double x, double y) {
		final int height = graphics.getFontMetrics().getHeight();
		final int baseY = (int) (y + 2 * height);
		graphics.drawString(text, (int) (x + RendererProperties.NODE_TEXT_PADDING), baseY);
	}

	public static void drawText(Graphics2D graphics, String text, NodeProperties limits, double padding) {
		NodeProperties newLimits = NodePropertiesFactory.get(
				limits.getX() + padding,
				limits.getY() + padding,
				limits.getWidth() - 2 * padding,
				limits.getHeight() - 2 * padding);
		drawText(graphics, text, newLimits);
	}
}
