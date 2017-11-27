package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class FillLayer extends CommonLayer {

	private Map<Paint, List<Shape>> shapes = new HashMap<>();

	public void add(Paint color, Shape shape) {
		addShape(shape);
		shapes.computeIfAbsent(color, k -> new LinkedList<>())
				.add(shape);
	}

	@Override
	public void render(Graphics2D graphics) {
		shapes.forEach((paint, shapes) -> {
			graphics.setPaint(paint);
			shapes.forEach(graphics::fill);
		});
	}

	@Override
	public void clear() {
		shapes.clear();
	}
}
