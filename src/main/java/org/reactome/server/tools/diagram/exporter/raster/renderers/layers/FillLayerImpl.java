package org.reactome.server.tools.diagram.exporter.raster.renderers.layers;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class FillLayerImpl implements FillLayer {

	private Map<Paint, List<Shape>> shapes = new HashMap<>();

	@Override
	public void add(Paint color, Shape shape) {
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
}
