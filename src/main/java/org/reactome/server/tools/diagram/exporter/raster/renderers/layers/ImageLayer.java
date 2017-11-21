package org.reactome.server.tools.diagram.exporter.raster.renderers.layers;

import org.reactome.server.tools.diagram.data.layout.Coordinate;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class ImageLayer extends CommonLayer {

	private List<ImageLayout> images = new ArrayList<>();

	@Override
	public void render(Graphics2D graphics) {
		images.forEach(imageLayout ->
				graphics.drawImage(imageLayout.image,
						imageLayout.coordinate.getX().intValue(),
						imageLayout.coordinate.getY().intValue(), null));
	}

	@Override
	public void clear() {
		images.clear();
	}

	public void add(Image image, Coordinate coordinate) {
		images.add(new ImageLayout(image, coordinate));
		final Shape bounds = new Rectangle2D.Double(coordinate.getX(), coordinate.getY(),
				image.getWidth(null), image.getHeight(null));
		super.addShape(bounds);
	}

	private class ImageLayout {

		private final Image image;
		private final Coordinate coordinate;

		ImageLayout(Image image, Coordinate coordinate) {
			this.image = image;
			this.coordinate = coordinate;
		}
	}
}
