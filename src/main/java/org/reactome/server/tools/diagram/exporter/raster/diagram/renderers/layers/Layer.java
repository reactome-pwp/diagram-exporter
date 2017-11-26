package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Virtual layer. At any time it is passed a graphics and has to render things
 * over it.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public interface Layer {

	void render(Graphics2D graphics);

	void clear();

	Rectangle2D getBounds();
}
