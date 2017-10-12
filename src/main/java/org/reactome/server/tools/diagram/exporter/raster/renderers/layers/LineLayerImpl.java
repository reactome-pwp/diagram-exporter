package org.reactome.server.tools.diagram.exporter.raster.renderers.layers;

import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LineLayerImpl implements LineLayer {

	private Map<String, Map<Stroke, List<Shape>>> shapes = new HashMap<>();

	@Override
	public void add(String color, Stroke stroke, Shape shape) {
		shapes.computeIfAbsent(color, k -> new HashMap<>())
				.computeIfAbsent(stroke, k -> new LinkedList<>())
				.add(shape);
	}

	@Override
	public void render(Graphics2D graphics) {
		shapes.forEach((color, strokes) -> {
			graphics.setPaint(ColorFactory.parseColor(color));
			strokes.forEach((stroke, shapes) -> {
				graphics.setStroke(stroke);
				shapes.forEach(graphics::draw);
			});
		});
	}
}
