package org.reactome.server.tools.diagram.exporter.raster.profiles;

import java.awt.*;

public interface GradientSheet {

	Color getMin();

	Color getStop();

	Color getMax();

}
