package org.reactome.server.tools.diagram.exporter.raster.color;

import java.awt.*;

public interface NodeColorSheet {

	Color getFill();

	Color getFadeOutFill();

	Color getLighterFill();

	Color getStroke();

	Color getFadeOutStroke();

	Color getLighterStroke();

	Color getText();

	Color getFadeOutText();

	Color getLighterText();

	Double getLineWidth();
}
