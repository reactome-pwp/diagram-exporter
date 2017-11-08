package org.reactome.server.tools.diagram.exporter.raster.profiles;

import java.awt.*;

public interface ThumbnailColorSheet {
	Color getNode();

	Color getEdge();

	Color getHovering();

	Color getHighlight();

	Color getSelection();
}
