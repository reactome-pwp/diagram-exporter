package org.reactome.server.tools.diagram.exporter.raster.renderers.layers;

import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FillLayerImpl implements FillLayer {

	private Map<String, List<Shape>> shapes = new HashMap<>();

	@Override
	public void add(String color, Shape shape) {
		shapes.computeIfAbsent(color, k -> new LinkedList<>())
				.add(shape);
	}

	@Override
	public void render(Graphics2D graphics) {
		shapes.forEach((color, shapes) -> {
			graphics.setPaint(ColorFactory.parseColor(color));
			shapes.forEach(graphics::fill);
		});
	}
}
