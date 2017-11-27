package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class DrawLayer extends CommonLayer {

	private List<DrawObject> objects = new LinkedList<>();

	public void add(Color color, Stroke stroke, Shape shape) {
		addShape(shape);
		objects.add(new DrawObject(shape, color, stroke));
	}

	@Override
	public void render(Graphics2D graphics) {
		objects.forEach(object -> {
			graphics.setPaint(object.color);
			graphics.setStroke(object.stroke);
			graphics.draw(object.shape);
		});
	}

	@Override
	public void clear() {
		objects.clear();
	}

	private class DrawObject {

		private final Shape shape;
		private final Color color;
		private final Stroke stroke;

		DrawObject(Shape shape, Color color, Stroke stroke) {
			this.shape = shape;
			this.color = color;
			this.stroke = stroke;
		}
	}
}
