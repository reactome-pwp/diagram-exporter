package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import org.reactome.server.tools.diagram.data.layout.Bound;
import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.shapes.ShapeFactory;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.Arrays;
import java.util.Collections;
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
		this.image = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_4BYTE_ABGR);
		this.graphics = image.createGraphics();
		graphics.translate(margin, margin);
		graphics.translate((int) -x, (int) -y);
		this.graphics.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		this.factor = factor;
		getGraphics().setFont(new Font("arial", Font.BOLD, (int) (9 * this.factor)));
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
	 * Draws a scaled rounded rectangle. If fill is not null, rectangle will be
	 * filled. If border is not null, border will be drawn.
	 *
	 * @param properties node properties, as in the diagram
	 * @param arcWidth   corner width
	 * @param arcHeight  corner height
	 */
	public void drawRoundedRectangle(NodeProperties properties,
	                                 double arcWidth, double arcHeight) {
		drawRoundedRectangle(properties.getX(), properties.getY(),
				properties.getWidth(), properties.getHeight(),
				arcWidth, arcHeight);
	}

	/**
	 * Draws a scaled rounded rectangle. If fill is not null, rectangle will be
	 * filled. If border is not null, border will be drawn.
	 *
	 * @param bound node bound
	 * @param arcW  corner width
	 * @param arcH  corner height
	 */
	public void drawRoundedRectangle(Bound bound, double arcW, double arcH) {
		drawRoundedRectangle(bound.getX(), bound.getY(),
				bound.getWidth(), bound.getHeight(),
				arcW, arcH);
	}

	/**
	 * Draws a scaled rounded rectangle
	 *
	 * @param x      top left x coordinate
	 * @param y      top left y coordinate
	 * @param width  rectangle width
	 * @param height rectangle height
	 * @param arcW   corner width
	 * @param arcH   corner height
	 */
	public void drawRoundedRectangle(double x, double y,
	                                 double width, double height,
	                                 double arcW, double arcH) {
		final int intX = Double.valueOf(factor * x).intValue();
		final int intY = Double.valueOf(factor * y).intValue();
		final int intW = Double.valueOf(factor * width).intValue();
		final int intH = Double.valueOf(factor * height).intValue();
		final int intAW = Double.valueOf(factor * arcW).intValue();
		final int intAH = Double.valueOf(factor * arcH).intValue();
		getGraphics().drawRoundRect(intX, intY, intW, intH, intAW, intAH);
	}

	/**
	 * Draws a scaled corned rectangle. If fill is not null, rectangle will be
	 * filled. If border is not null, border will be drawn.
	 *
	 * @param properties   node properties
	 * @param cornerHeight height of corners
	 * @param cornerWidth  width of corners
	 */
	public void drawCornedRectangle(NodeProperties properties,
	                                double cornerWidth, double cornerHeight) {
		drawCornedRectangle(properties.getX(), properties.getY(), properties.getWidth(),
				properties.getHeight(), cornerWidth, cornerHeight);
	}

	/**
	 * Draws a scaled corned rectangle. If fill is not null, rectangle will be
	 * filled. If border is not null, border will be drawn.
	 *
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @param cornerWidth
	 * @param cornerHeight
	 */
	public void drawCornedRectangle(double x, double y, double w, double h,
	                                double cornerWidth, double cornerHeight) {
		final int intX = Double.valueOf(x * factor).intValue();
		final int intY = Double.valueOf(y * factor).intValue();
		final int intW = Double.valueOf(w * factor).intValue();
		final int intH = Double.valueOf(h * factor).intValue();
		final int intCW = Double.valueOf(cornerWidth * factor).intValue();
		final int intCH = Double.valueOf(cornerHeight * factor).intValue();
		final Polygon rectangle = ShapeFactory.cornedRectangle(intX, intY,
				intW, intH, intCW, intCH);
		getGraphics().drawPolygon(rectangle);
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

	/**
	 * Draws an oval
	 *
	 * @param properties node properties
	 */
	public void drawOval(NodeProperties properties) {
		drawOval(properties.getX(), properties.getY(), properties.getWidth(),
				properties.getHeight());
	}

	/**
	 * Draws an oval
	 */
	public void drawOval(double x, double y, double width, double height) {
		final int intX = Double.valueOf(factor * x).intValue();
		final int intY = Double.valueOf(factor * y).intValue();
		final int intW = Double.valueOf(factor * width).intValue();
		final int intH = Double.valueOf(factor * height).intValue();
		getGraphics().drawOval(intX, intY, intW, intH);
	}

	public void fillOval(double x, double y, double width, double height) {
		final int intX = Double.valueOf(factor * x).intValue();
		final int intY = Double.valueOf(factor * y).intValue();
		final int intW = Double.valueOf(factor * width).intValue();
		final int intH = Double.valueOf(factor * height).intValue();
		getGraphics().fillOval(intX, intY, intW, intH);
	}

	public void drawRectangle(NodeProperties prop) {
		drawRectangle(prop.getX(), prop.getY(), prop.getWidth(), prop.getHeight());
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
		final double scaledX = factor * x;
		final double scaledY = factor * y;
		final double scaledW = factor * width;
		final double scaledH = factor * height;
		final double centerX = scaledX + scaledW * 0.5;
		final double centerY = scaledY + scaledH * 0.5;

		double availableWidth = scaledW;
		availableWidth -= 2 * padding;

		final int textWidth = getGraphics().getFontMetrics().charsWidth(text.toCharArray(), 0, text.length());

		if (textWidth <= availableWidth) {
			// This height ensures that text is centered at hyphens
			final double textHeight = getGraphics().getFontMetrics().getAscent() - getGraphics().getFontMetrics().getDescent();
			double space = 0.5 * textWidth;
			final int left = (int) (centerX - space);
			final int base = (int) (centerY + 0.5 * (textHeight));
			getGraphics().drawString(text, left, base);
		} else {
			Font font = getGraphics().getFont();
			List<String> lines;
			while ((lines = fit(availableWidth, scaledH, text, font)) == null)
				font = font.deriveFont((float) font.getSize() - 1);
			final Font old = graphics.getFont();
			graphics.setFont(font);
			final int textHeight = lines.size() * getGraphics().getFontMetrics().getHeight();
			final double yOffset = (scaledH - textHeight) * 0.5;
			for (int i = 0; i < lines.size(); i++) {
				final String line = lines.get(i);
				final int lineWidth = getGraphics().getFontMetrics().charsWidth(line.toCharArray(), 0, line.length());
				final int base = (int) (yOffset + scaledY + (i + 1) * getGraphics().getFontMetrics().getHeight());
				int left = (int) (centerX - 0.5 * lineWidth);
				getGraphics().drawString(line, left, base);
			}
			graphics.setFont(old);
		}
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
				node.getProp().getHeight(), factor * padding);

	}

	/**
	 * @param availableWidth
	 * @param availableHeight
	 * @param text
	 * @param font
	 * @return
	 */
	private List<String> fit(double availableWidth, double availableHeight, String text, Font font) {
		final int width = getGraphics().getFontMetrics(font).charsWidth(text.toCharArray(), 0, text.length());
		if (width <= availableWidth)
			return Collections.singletonList(text);
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

	private List<String> splitWord(String word) {
		final List<String> parts = new LinkedList<>();
		int start = 0;
		for (int i = 0; i < word.length(); i++) {
			if (WORD_SPLIT_CHARS.contains(word.charAt(i))) {
				parts.add(word.substring(start, i + 1));
				start = i + 1;
			}
		}
		parts.add(word.substring(start));
		return parts;
	}

	public void drawLine(Coordinate from, Coordinate to) {
		drawLine(from.getX(), from.getY(), to.getX(), to.getY());
	}


	public void drawLine(double x, double y, double x1, double y1) {
		final int x0 = (int) (factor * x);
		final int y0 = (int) (factor * y);
		final int x2 = (int) (factor * x1);
		final int y2 = (int) (factor * y1);
		getGraphics().drawLine(x0, y0, x2, y2);
	}

	public void drawPolygon(double[] xs, double[] ys) {
		final int[] intX = new int[xs.length];
		final int[] intY = new int[xs.length];
		for (int i = 0; i < xs.length; i++) {
			intX[i] = (int) (factor * xs[i]);
			intY[i] = (int) (factor * ys[i]);
		}
		final Polygon polygon = new Polygon(intX, intY, intX.length);
		getGraphics().fillPolygon(polygon);
		getGraphics().drawPolygon(polygon);
	}

	public void fillRoundedRectangle(Bound insets, double arcWidth, double arcHeight) {
		fillRoundedRectangle(insets.getX(), insets.getY(), insets.getWidth(), insets.getHeight(), arcWidth, arcHeight);
	}

	public void fillRoundedRectangle(NodeProperties properties, double arcWidth, double arcHeight) {
		fillRoundedRectangle(properties.getX(), properties.getY(), properties.getWidth(), properties.getHeight(), arcWidth, arcHeight);
	}

	public void fillRoundedRectangle(Double x, double y, double width, double height, double arcWidth, double arcHeight) {
		final int intX = Double.valueOf(factor * x).intValue();
		final int intY = Double.valueOf(factor * y).intValue();
		final int intW = Double.valueOf(factor * width).intValue();
		final int intH = Double.valueOf(factor * height).intValue();
		final int intAW = Double.valueOf(factor * arcWidth).intValue();
		final int intAH = Double.valueOf(factor * arcHeight).intValue();
		getGraphics().fillRoundRect(intX, intY, intW, intH, intAW, intAH);
	}

	public void fillCornedRectangle(NodeProperties prop, double arcWidth, double arcHeight) {
		fillCornedRectangle(prop.getX(), prop.getY(), prop.getWidth(), prop.getHeight(), arcWidth, arcHeight);
	}

	private void fillCornedRectangle(Double x, double y, double width, double height, double arcWidth, double arcHeight) {
		final int intX = Double.valueOf(x * factor).intValue();
		final int intY = Double.valueOf(y * factor).intValue();
		final int intW = Double.valueOf(width * factor).intValue();
		final int intH = Double.valueOf(height * factor).intValue();
		final int intCW = Double.valueOf(arcWidth * factor).intValue();
		final int intCH = Double.valueOf(arcHeight * factor).intValue();
		final Polygon rectangle = ShapeFactory.cornedRectangle(intX, intY,
				intW, intH, intCW, intCH);
		getGraphics().fillPolygon(rectangle);
	}

	public void fillOval(NodeProperties prop) {
		fillOval(prop.getX(), prop.getY(), prop.getWidth(), prop.getHeight());
	}

	public void fillRectangle(NodeProperties prop) {
		fillRect(prop.getX(), prop.getY(), prop.getWidth(), prop.getHeight());
	}

	private void fillRect(double x, double y, double width, double height) {
		getGraphics().fillRect(
				(int) (factor * x),
				(int) (factor * y),
				(int) (factor * width),
				(int) (factor * height)
		);
	}

	public void drawGene(Node node) {
		final NodeProperties prop = new ScaledNodeProperties(node.getProp(), factor);
		// Horizontal line
		final double x = prop.getX();
		final double y1 = prop.getY() + 0.5 * RendererProperties.GENE_SYMBOL_WIDTH;
		final double right = prop.getX() + prop.getWidth();
		final Path2D path = new GeneralPath();
		path.moveTo(x, y1);
		path.lineTo(right, y1);
		// Vertical line
		final double x1 = right - RendererProperties.GENE_SYMBOL_PAD;
		final double y2 = y1 - 0.5 * RendererProperties.GENE_SYMBOL_WIDTH;
		path.moveTo(x1, y1);
		path.lineTo(x1, y2);
		// another very short horizontal line
		path.lineTo(right, y2);
		graphics.draw(path);

//		// draw the arrow
//		drawArrow(x1, y2, x1 + arrowLength, y2, arrowLength, arrowAngle);
		// Get the angle of the line segment
		final double toX = right + RendererProperties.ARROW_LENGTH;
		final Path2D triangle = new GeneralPath();
		triangle.moveTo(toX, y2);
		final double ay = y2 + 0.5 * RendererProperties.ARROW_LENGTH;
		final double by = y2 - 0.5 * RendererProperties.ARROW_LENGTH;
		triangle.lineTo(right, ay);
		triangle.lineTo(right, by);
		triangle.closePath();
		graphics.draw(triangle);
	}

	public void fillGene(Node node) {
		final NodeProperties prop = new ScaledNodeProperties(node.getProp(), factor);
		final GeneralPath path = new GeneralPath();
		final double y1 = prop.getY() + 0.5 * RendererProperties.GENE_SYMBOL_WIDTH;
		final double right = prop.getX() + prop.getWidth();
		final double bottom = prop.getY() + prop.getHeight();
		final double arcWidth = RendererProperties.ROUND_RECT_ARC_WIDTH;

		path.moveTo(prop.getX(), y1);
		path.lineTo(right, y1);
		path.lineTo(right, bottom - arcWidth);
		path.quadTo(right, bottom, right - arcWidth, bottom);
		path.lineTo(prop.getX() + arcWidth, bottom);
		path.quadTo(prop.getX(), bottom, prop.getX(), bottom - arcWidth);
		path.closePath();
		graphics.fill(path);

		final double y2 = y1 - 0.5 * RendererProperties.GENE_SYMBOL_WIDTH;
		final double toX = right + RendererProperties.ARROW_LENGTH;
		final Path2D triangle = new GeneralPath();
		triangle.moveTo(toX, y2);
		final double ay = y2 + 0.5 * RendererProperties.ARROW_LENGTH;
		final double by = y2 - 0.5 * RendererProperties.ARROW_LENGTH;
		triangle.lineTo(right, ay);
		triangle.lineTo(right, by);
		triangle.closePath();
		graphics.fill(triangle);
	}

	public void fillRectangle(NodeProperties prop, double padding) {
		final double x = factor * (prop.getX() + padding);
		final double y = factor * (prop.getY() + padding);
		final double w = factor * (prop.getWidth() - 2 * padding);
		final double h = factor * (prop.getHeight() - 2 * padding);
		graphics.fillRect((int) x, (int) y, (int) w, (int) h);
	}

	public void drawRectangle(NodeProperties prop, double padding) {
		final double x = factor * (prop.getX() + padding);
		final double y = factor * (prop.getY() + padding);
		final double w = factor * (prop.getWidth() - 2 * padding);
		final double h = factor * (prop.getHeight() - 2 * padding);
		graphics.drawRect((int) x, (int) y, (int) w, (int) h);
	}

}
