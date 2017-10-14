package org.reactome.server.tools.diagram.exporter.raster.renderers.layers;

import java.awt.*;

/**
 * Layer for drawings, like lines or borders.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public interface DrawLayer extends Layer {

	void add(String color, Stroke stroke, Shape shape);
}
