package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;

import java.awt.*;
import java.util.Arrays;
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
	public static void drawTextSingleLine(AdvancedGraphics2D graphics, String text, Coordinate position) {
		final int leftX = Double.valueOf(RendererProperties.NODE_TEXT_PADDING + graphics.getFactor() * (position.getX())).intValue();
		final int topY = Double.valueOf(RendererProperties.NODE_TEXT_PADDING + graphics.getFactor() * position.getY()).intValue();

		final int height = graphics.getGraphics().getFontMetrics().getHeight();
		final int baseY = topY + height;

		graphics.getGraphics().drawString(text, leftX, baseY);
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
	public static void drawText(AdvancedGraphics2D graphics, String text, double x, double y, double width, double height) {
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
	public static void drawText(AdvancedGraphics2D graphics, String text, double x, double y, double width, double height, double padding) {
		drawText(graphics, text, x, y, width, height, padding, true);
	}

	/**
	 * Draws text using all available space in node.getProp()
	 *
	 * @param node
	 */
	public static void drawText(AdvancedGraphics2D graphics, NodeCommon node) {
		drawText(graphics, node.getDisplayName(), node.getProp().getX(),
				node.getProp().getY(), node.getProp().getWidth(),
				node.getProp().getHeight());
	}


	/**
	 * Displays text in the assigned space. If the text does not fit in one
	 * line, it is split in several lines. If the font size is very large, it is
	 * lower until the text fits. Each line is centered to the box and the whole
	 * text is vertically centered. By default the vertical centering is
	 * natural, i.e. text is centered at hyphens (-) height the center of the
	 * box. For smaller texts, such us symbols or numbers, you can set
	 * naturalCentering to false.
	 *
	 * @param graphics
	 * @param text            text to write
	 * @param x               top left corner x coordinate
	 * @param y               top left corner y coordinate
	 * @param width           max width for text. A 2 * padding will be
	 *                        subtracted to this width
	 * @param height          max height for text. A 2 * padding will be
	 *                        subtracted to this height
	 * @param padding         minimum space from borders to text
	 * @param naturalCentered if true, text is vertically aligned to hyphens
	 *                        (-), otherwise, it is centered to text center
	 */
	public static void drawText(AdvancedGraphics2D graphics, String text, double x, double y, double width, double height, double padding, boolean naturalCentered) {
		final double scaledX = graphics.getFactor() * (x) + padding;
		final double scaledY = graphics.getFactor() * (y) + padding;
		final double scaledW = graphics.getFactor() * (width) - 2 * padding;
		final double scaledH = graphics.getFactor() * (height) - 2 * padding;
		final double centerX = scaledX + scaledW * 0.5;

		Font font = graphics.getGraphics().getFont();
		List<String> lines;
		while ((lines = fit(graphics, scaledW, scaledH, text, font)) == null)
			font = font.deriveFont((float) font.getSize() - 1);
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

	public static void drawText(AdvancedGraphics2D graphics, NodeCommon node, double padding) {
		drawText(graphics, node.getDisplayName(), node.getProp().getX(),
				node.getProp().getY(), node.getProp().getWidth(),
				node.getProp().getHeight(), padding);
	}

	/**
	 * @param graphics2D
	 * @param availableWidth
	 * @param availableHeight
	 * @param text
	 * @param font
	 *
	 * @return
	 */
	private static List<String> fit(AdvancedGraphics2D graphics2D, double availableWidth, double availableHeight, String text, Font font) {
		final List<String> lines = new LinkedList<>();
		final String[] words = text.trim().split(" ");
		StringBuilder line = new StringBuilder();
		for (String word : words) {
			String temp = line.toString() + " " + word;
			if (graphics2D.getGraphics().getFontMetrics(font).charsWidth(temp.toCharArray(), 0, temp.length()) < availableWidth)
				line.append(" ").append(word);
			else {
				final List<String> parts = splitWord(word);
				boolean first = true;
				for (String part : parts) {
					if (first && line.length() > 0)
						temp = line.toString() + " " + part;
					else temp = line.toString() + part;
					if (graphics2D.getGraphics().getFontMetrics(font).charsWidth(temp.toCharArray(), 0, temp.length()) < availableWidth) {
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
		if (lines.size() * graphics2D.getGraphics().getFontMetrics(font).getHeight() <= availableHeight)
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
	 *
	 * @return
	 */
	private static List<String> splitWord(String word) {
		final List<String> parts = new LinkedList<>();
		int start = 0;
		for (int i = 0; i < word.length(); i++) {
			if (TextRenderer.WORD_SPLIT_CHARS.contains(word.charAt(i))) {
				parts.add(word.substring(start, i + 1));
				start = i + 1;
			}
		}
		final String end = word.substring(start);
		if (!end.isEmpty()) parts.add(end);
		return parts;
	}

	public static void drawText(AdvancedGraphics2D graphics, String text, NodeProperties props) {
		drawText(graphics, text, props.getX(), props.getY(), props.getWidth(), props.getHeight());
	}
}
