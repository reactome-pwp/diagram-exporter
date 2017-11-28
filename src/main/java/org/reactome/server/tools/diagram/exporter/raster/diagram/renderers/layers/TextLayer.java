package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers;

import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout.TextRenderer;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class TextLayer extends CommonLayer {

	private List<RenderableText> objects = new LinkedList<>();

	public void add(String text, Color color, NodeProperties limits, double padding, double splitText, Font font) {
		objects.add(new RenderableText(text, limits, padding, splitText, color, font));
		addShape(new Rectangle2D.Double(limits.getX(), limits.getY(), limits.getWidth(), limits.getHeight()));
	}

	public void add(Color color, String text, Coordinate position, Font font) {
		objects.add(new RenderableText(text, position, color, font));
	}

	@Override
	public void render(Graphics2D graphics) {
		objects.forEach(text -> {
			graphics.setFont(text.font);
			graphics.setPaint(text.color);
			if (text.limits == null) {
				TextRenderer.drawTextSingleLine(graphics, text.text, text.position);
			} else {
				TextRenderer.drawText(graphics, text.text, text.limits, text.padding, text.splitText);
			}
		});
	}

	@Override
	public void clear() {
		super.clear();
		objects.clear();
	}

	private class RenderableText {

		private final String text;
		private final Coordinate position;
		private final NodeProperties limits;
		private double padding;
		private double splitText;
		private final Color color;
		private final Font font;

		RenderableText(String text, NodeProperties limits, double padding, double splitText, Color color, Font font) {
			this.text = text;
			this.limits = limits;
			this.padding = padding;
			this.splitText = splitText;
			this.color = color;
			this.font = font;
			this.position = null;
		}

		RenderableText(String text, Coordinate position, Color color, Font font) {
			this.text = text;
			this.position = position;
			this.color = color;
			this.font = font;
			this.limits = null;
		}
	}

}
