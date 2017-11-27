package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Renders a shape with a fill and a border.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class FillDrawLayer extends CommonLayer {

	private List<DrawObject> objects = new LinkedList<>();

	public void add(Paint fillColor, Paint borderColor, Stroke borderStroke, Shape shape) {
		addShape(shape);
		objects.add(new DrawObject(shape, fillColor, borderColor, borderStroke));
	}

	@Override
	public void render(Graphics2D graphics) {
		objects.forEach(object -> {
			graphics.setPaint(object.fill);
			graphics.fill(object.shape);
			graphics.setPaint(object.border);
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
		private final Paint fill;
		private final Paint border;
		private final Stroke stroke;

		DrawObject(Shape shape, Paint fill, Paint border, Stroke stroke) {
			this.shape = shape;
			this.fill = fill;
			this.border = border;
			this.stroke = stroke;
		}
	}
}
