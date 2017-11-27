package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers;

import org.reactome.server.tools.diagram.data.layout.NodeProperties;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Layer to include raster images to the canvas.
 */
public class ImageLayer extends CommonLayer {

	private List<ImageLayout> images = new ArrayList<>();

	public void add(Image image, NodeProperties bounds) {
		images.add(new ImageLayout(image, bounds));
		super.addShape(new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()));
	}

	@Override
	public void render(Graphics2D graphics) {
		images.forEach(imageLayout ->
				graphics.drawImage(imageLayout.image,
						imageLayout.bounds.getX().intValue(),
						imageLayout.bounds.getY().intValue(),
						imageLayout.bounds.getWidth().intValue(),
						imageLayout.bounds.getHeight().intValue(),
						null)
		);
	}

	@Override
	public void clear() {
		images.clear();
	}

	private class ImageLayout {

		private final Image image;
		private final NodeProperties bounds;

		ImageLayout(Image image, NodeProperties bounds) {
			this.image = image;
			this.bounds = bounds;
		}
	}
}
