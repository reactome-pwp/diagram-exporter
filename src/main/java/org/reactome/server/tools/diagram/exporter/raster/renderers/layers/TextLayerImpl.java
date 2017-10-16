package org.reactome.server.tools.diagram.exporter.raster.renderers.layers;

import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.TextRenderer;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class TextLayerImpl implements TextLayer {

	private Map<String, Collection<RenderableText>> texts = new HashMap<>();

	@Override
	public void add(String color, String text, NodeProperties limits, double padding, double splitText) {
		texts.computeIfAbsent(color, k -> new LinkedList<>())
				.add(new RenderableText(text, limits, padding, splitText));
	}

	@Override
	public void add(String color, String text, Coordinate position) {
		texts.computeIfAbsent(color, k -> new LinkedList<>())
				.add(new RenderableText(text, position));
	}

	@Override
	public void render(Graphics2D graphics) {
		texts.forEach((color, renderableTexts) -> {
			graphics.setPaint(ColorFactory.parseColor(color));
			renderableTexts.forEach(text -> {
				if (text.limits != null)
					TextRenderer.drawText(graphics, text.text, text.limits, text.padding, text.splitText);
				else if (text.position != null) {
					TextRenderer.drawTextSingleLine(graphics, text.text, text.position);
				}
			});
		});
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
