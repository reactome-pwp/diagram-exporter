package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers;

import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout.TextRenderer;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class TextLayer extends CommonLayer {

	private Map<Font, Map<Color, Collection<RenderableText>>> texts = new HashMap<>();

	public void add(String text, Color color, NodeProperties limits, double padding, double splitText, Font font) {
		texts
				.computeIfAbsent(font, k -> new HashMap<>())
				.computeIfAbsent(color, k -> new LinkedList<>())
				.add(new RenderableText(text, limits, padding, splitText));
		addShape(new Rectangle2D.Double(limits.getX(), limits.getY(), limits.getWidth(), limits.getHeight()));
	}

	public void add(Color color, String text, Coordinate position, Font font) {
		texts
				.computeIfAbsent(font, k -> new HashMap<>())
				.computeIfAbsent(color, k -> new LinkedList<>())
				.add(new RenderableText(text, position));
	}

	@Override
	public void render(Graphics2D graphics) {
		texts.forEach((font, items) -> {
			graphics.setFont(font);
			items.forEach((color, renderableTexts) -> {
				graphics.setPaint(color);
				renderableTexts.forEach(text -> {
					if (text.limits != null)
						TextRenderer.drawText(graphics, text.text, text.limits, text.padding, text.splitText);
					else if (text.position != null)
						TextRenderer.drawTextSingleLine(graphics, text.text, text.position);
				});
			});
		});
	}

	@Override
	public void clear() {
		texts.clear();
	}

	private class RenderableText {

		private final String text;
		private final Coordinate position;
		private final NodeProperties limits;
		private double padding;
		private double splitText;

		RenderableText(String text, NodeProperties limits, double padding, double splitText) {
			this.text = text;
			this.limits = limits;
			this.padding = padding;
			this.splitText = splitText;
			this.position = null;
		}

		RenderableText(String text, Coordinate position) {
			this.text = text;
			this.position = position;
			this.limits = null;
		}
	}
}
