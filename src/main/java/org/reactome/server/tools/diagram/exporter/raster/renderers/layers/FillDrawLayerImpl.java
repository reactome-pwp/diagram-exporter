package org.reactome.server.tools.diagram.exporter.raster.renderers.layers;

import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Renders a shape with a fill and a border.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class FillDrawLayerImpl implements FillDrawLayer {

	private Map<String, Map<String, Map<Stroke, Collection<Shape>>>> shapes = new HashMap<>();

	@Override
	public void add(String fillColor, String borderColor, Stroke borderStroke, Shape shape) {
		shapes
				.computeIfAbsent(fillColor, k -> new HashMap<>())
				.computeIfAbsent(borderColor, k -> new HashMap<>())
				.computeIfAbsent(borderStroke, k -> new LinkedList<>())
				.add(shape);
	}

	@Override
	public void render(Graphics2D graphics) {
		shapes.forEach((fillColor, items) -> {
			final Color fill = ColorFactory.parseColor(fillColor);
			items.forEach((borderColor, subitems) -> {
				final Color border = ColorFactory.parseColor(borderColor);
				subitems.forEach((stroke, shapes) -> {
					graphics.setStroke(stroke);
					shapes.forEach(shape -> {
						graphics.setPaint(fill);
						graphics.fill(shape);
						graphics.setPaint(border);
						graphics.draw(shape);
					});
				});
			});
		});
	}
}
