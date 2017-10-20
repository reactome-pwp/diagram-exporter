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

	private Map<Color, List<Shape>> shapes = new HashMap<>();

	@Override
	public void add(Color color, Shape shape) {
		shapes.computeIfAbsent(color, k -> new LinkedList<>())
				.add(shape);
	}

	@Override
	public void render(Graphics2D graphics) {
		shapes.forEach((color, shapes) -> {
			graphics.setPaint(color);
			shapes.forEach(graphics::fill);
		});
	}
}
