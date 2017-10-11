package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
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
	 * Draws a text with the default font, using position as the top left corner
	 * of text. Text will be drawn in 1 line.
	 *
	 * @param text     text to display
	 * @param position top left coordinate
	 */
	static void drawTextSingleLine(AdvancedGraphics2D graphics, String text, Coordinate position) {
		final double x = graphics.getFactor() * position.getX() + RendererProperties.NODE_TEXT_PADDING;
		final double y = graphics.getFactor() * position.getY() + RendererProperties.NODE_TEXT_PADDING;

		final int height = graphics.getGraphics().getFontMetrics().getHeight();
		final int baseY = (int) (y + height);

		graphics.getGraphics().drawString(text, (int) x, baseY);
	}

	/**
	 * Magic method that displays text in the assigned space. If the text does
	 * not fit in one line, it is split in several lines. If the font size is
	 * very large, it is lower until the text fits. Each line is centered to the
	 * box and the whole text is vertically centered. By default the vertical
	 * centering is natural, i.e. text is centered at hyphens (-) height the
	 * center of the box. This is a little big higher than strict center and is
	 * visually better for large texts. A padding of RenderedProperties.NODE_TEXT_PADDING
	 * is added by default.
	 *
	 * @param text   text to write
	 * @param x      top left corner x coordinate
	 * @param y      top left corner y coordinate
	 * @param width  max width for text. A 2 * padding will be subtracted to
	 *               this width
	 * @param height max height for text. A 2 * padding will be subtracted to
	 *               this height
	 */
	static void drawText(AdvancedGraphics2D graphics, String text, double x, double y, double width, double height) {
		drawText(graphics, text, x, y, width, height, RendererProperties.NODE_TEXT_PADDING, true);
	}

	/**
	 * Displays text in the assigned space. If the text does not fit in one
	 * line, it is split in several lines. If the font size is very large, it is
	 * lower until the text fits. Each line is centered to the box and the whole
	 * text is vertically centered. By default the vertical centering is
	 * natural, i.e. text is centered at hyphens (-) height the center of the
	 * box. This is a little big higher than strict center and is visually
	 * better for large texts.
	 *
	 * @param text    text to write
	 * @param x       top left corner x coordinate
	 * @param y       top left corner y coordinate
	 * @param width   max width for text. A 2 * padding will be subtracted to
	 *                this width
	 * @param height  max height for text. A 2 * padding will be subtracted to
	 *                this height
	 * @param padding minimum space from borders to text
	 */
	private static void drawText(AdvancedGraphics2D graphics, String text, double x, double y, double width, double height, double padding) {
		drawText(graphics, text, x, y, width, height, padding, true);
	}

	/**
	 * Draws text using all available space in node.getProp()
	 */
	static void drawText(AdvancedGraphics2D graphics, NodeCommon node) {
		drawText(graphics, node.getDisplayName(), node.getProp().getX(),
				node.getProp().getY(), node.getProp().getWidth(),
				node.getProp().getHeight(), RendererProperties.NODE_TEXT_PADDING);
	}


	/**
	 * Displays text in the assigned space. If the text does not fit in one
	 * line, it is split in several lines. If the font size is very large, it is
	 * lower until the text fits. Each line is centered to the box and the whole
	 * text is vertically centered. By default the vertical centering is
	 * natural, i.e. text is centered at hyphens (-) height. For smaller texts,
	 * such us symbols or numbers, you can set naturalCentering to false.
	 *
	 * @param graphics        where to render
	 * @param text            text to write
	 * @param x               top left corner x coordinate
	 * @param y               top left corner y coordinate
	 * @param width           max width for text. A 2 * padding will be
	 *                        subtracted to this width
	 * @param height          max height for text. A 2 * padding will be
	 *                        subtracted to this height
	 * @param padding         absolute minimum space from borders to text
	 * @param naturalCentered if true, text is vertically aligned to hyphens
	 *                        (-), otherwise, it is centered to text center
	 */
	static void drawText(AdvancedGraphics2D graphics, String text, double x, double y, double width, double height, double padding, boolean naturalCentered) {
		final double scaledX = graphics.getFactor() * (x) + padding;
		final double scaledY = graphics.getFactor() * (y) + padding;
		final double scaledW = graphics.getFactor() * (width) - 2 * padding;
		final double scaledH = graphics.getFactor() * (height) - 2 * padding;
		final double centerX = scaledX + scaledW * 0.5;

		Font font = graphics.getGraphics().getFont();
		List<String> lines;
		while ((lines = fit(graphics, scaledW, scaledH, text, font)) == null)
			font = font.deriveFont((float) font.getSize() - 1);
		// Impossible to fit even at font size 1
		if (lines.isEmpty()) return;
		final Font old = graphics.getGraphics().getFont();
		graphics.getGraphics().setFont(font);
		final int textHeight = lines.size() * graphics.getGraphics().getFontMetrics().getHeight();
		double yOffset = scaledY + (scaledH - textHeight) * 0.5;
		if (naturalCentered)
			yOffset += graphics.getGraphics().getFontMetrics().getAscent();
		else yOffset += (graphics.getGraphics().getFontMetrics().getHeight());
//		else yOffset += 0.5 * (graphics.getFontMetrics().getAscent() + graphics.getFontMetrics().getDescent());
		for (int i = 0; i < lines.size(); i++) {
			final String line = lines.get(i);
			final int lineWidth = graphics.getGraphics().getFontMetrics().charsWidth(line.toCharArray(), 0, line.length());
			final int base = (int) (yOffset + i * graphics.getGraphics().getFontMetrics().getHeight());
			int left = (int) (centerX - 0.5 * lineWidth);
			graphics.getGraphics().drawString(line, left, base);
		}
		graphics.getGraphics().setFont(old);
//		}
	}

	static void drawText(AdvancedGraphics2D graphics, NodeCommon node, double padding) {
		drawText(graphics, node.getDisplayName(), node.getProp().getX(),
				node.getProp().getY(), node.getProp().getWidth(),
				node.getProp().getHeight(), padding);
	}

	/**
	 * @return a list of lines if text can be fit inside availableWidth and
	 * availableHeight. If text does not fit, returns null. If font size is less
	 * than 1, then it returns an empty list to indicate that text is impossible
	 * to be shown, probably because image size is too small.
	 */
	private static List<String> fit(AdvancedGraphics2D graphics, double availableWidth, double availableHeight, String text, Font font) {
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

	private static int computeHeight(AdvancedGraphics2D graphics, Font font, List<String> lines) {
		return lines.size() * graphics.getGraphics().getFontMetrics(font).getHeight();
	}

	private static int computeWidth(AdvancedGraphics2D graphics2D, Font font, String temp) {
		return graphics2D.getGraphics().getFontMetrics(font).charsWidth(temp.toCharArray(), 0, temp.length());
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

	static void drawText(AdvancedGraphics2D graphics, String text, NodeProperties props) {
		drawText(graphics, text, props.getX(), props.getY(), props.getWidth(), props.getHeight());
	}
}
