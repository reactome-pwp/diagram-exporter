package org.reactome.server.tools.diagram.exporter.raster.renderers.layers;

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
public class FillDrawLayer extends CommonLayer {

	private Map<Paint, Map<Paint, Map<Stroke, Collection<Shape>>>> shapes = new HashMap<>();

	public void add(Paint fillColor, Paint borderColor, Stroke borderStroke, Shape shape) {
		addShape(shape);
		shapes
				.computeIfAbsent(fillColor, k -> new HashMap<>())
				.computeIfAbsent(borderColor, k -> new HashMap<>())
				.computeIfAbsent(borderStroke, k -> new LinkedList<>())
				.add(shape);
	}

	@Override
	public void render(Graphics2D graphics) {
		shapes.forEach((fillColor, items) ->
				items.forEach((borderColor, subitems) -> {
					subitems.forEach((stroke, shapes) -> {
						graphics.setStroke(stroke);
						shapes.forEach(shape -> {
							graphics.setPaint(fillColor);
							graphics.fill(shape);
							graphics.setPaint(borderColor);
							graphics.draw(shape);
						});
					});
				}));
	}

	@Override
	public void clear() {
		shapes.clear();
	}
}
