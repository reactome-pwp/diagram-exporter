package org.reactome.server.tools.diagram.exporter.raster.renderers.layers;

import java.awt.*;

public interface LineLayer extends Layer {

	void add(String color, Stroke stroke, Shape shape);
}
