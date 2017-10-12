package org.reactome.server.tools.diagram.exporter.raster.renderers.layers;

import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;

public interface TextLayer extends Layer {

	void add(String color, String text, NodeProperties limits, double padding);

	void add(String color, String text, Coordinate position);
}
