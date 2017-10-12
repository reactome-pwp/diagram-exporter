package org.reactome.server.tools.diagram.exporter.raster.renderers.layers;

import java.awt.*;

public interface FillLayer extends Layer {

	void add(String color, Shape shape);
}
