package org.reactome.server.tools.diagram.exporter.raster.renderers.layers;

import java.awt.*;

/**
 * Virtual layer. At any time it is passed a graphics and has to render things
 * over it.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public interface Layer {

	void render(Graphics2D graphics);

}
