package org.reactome.server.tools.diagram.exporter.raster.renderers.layers;

import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;

/**
 * This layer accepts two types of texts, boxed and free. Boxed are fit to a
 * rectangle by splitting the text and reducing the font size. Free are rendered
 * with the canvas font at the given position.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public interface TextLayer extends Layer {

	void add(String color, String text, NodeProperties limits, double padding);

	void add(String color, String text, Coordinate position);
}
