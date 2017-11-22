package org.reactome.server.tools.diagram.exporter.raster.renderers.layers;

import org.reactome.server.tools.diagram.data.layout.NodeProperties;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class ImageLayer extends CommonLayer {

	private List<ImageLayout> images = new ArrayList<>();

	public void add(Image image, NodeProperties bounds) {
		images.add(new ImageLayout(image, bounds));
		super.addShape(new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()));
	}

	@Override
	public void render(Graphics2D graphics) {
		images.forEach(imageLayout -> {
//			final AffineTransform oldTransform = graphics.getTransform();
//			final AffineTransform transform = new AffineTransform();
//			transform.translate(oldTransform.getTranslateX(), transform.getTranslateY());
//			graphics.setTransform(transform);
//			graphics.drawImage(imageLayout.image, imageLayout.coordinate.getX().intValue(), imageLayout.coordinate.getY().intValue(), null);
//			graphics.setTransform(oldTransform);
			graphics.drawImage(imageLayout.image,
					imageLayout.bounds.getX().intValue(),
					imageLayout.bounds.getY().intValue(),
					imageLayout.bounds.getWidth().intValue(),
					imageLayout.bounds.getHeight().intValue(),
					null);
				}
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