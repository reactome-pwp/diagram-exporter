package org.reactome.server.tools.diagram.exporter.raster.renderers.layers;

import java.awt.*;

/**
 * Layer for filling
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail)
 */
public interface FillLayer extends Layer {

	void add(Paint color, Shape shape);
}
