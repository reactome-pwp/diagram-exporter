package org.reactome.server.tools.diagram.exporter.raster.renderers.layers;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class DrawLayerImpl implements DrawLayer {

	private Map<Color, Map<Stroke, List<Shape>>> shapes = new HashMap<>();

	@Override
	public void add(Color color, Stroke stroke, Shape shape) {
		shapes.computeIfAbsent(color, k -> new HashMap<>())
				.computeIfAbsent(stroke, k -> new LinkedList<>())
				.add(shape);
	}

	@Override
	public void render(Graphics2D graphics) {
		shapes.forEach((color, strokes) -> {
			graphics.setPaint(color);
			strokes.forEach((stroke, shapes) -> {
				graphics.setStroke(stroke);
				shapes.forEach(graphics::draw);
			});
		});
	}

	@Override
	public void clear() {
		shapes.clear();
	}
}
