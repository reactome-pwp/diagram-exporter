package org.reactome.server.tools.diagram.exporter.raster.color;

import java.awt.*;

public interface ThumbnailColorSheet {
	Color getNode();

	Color getEdge();

	Color getHovering();

	Color getHighlight();

	Color getSelection();
}
