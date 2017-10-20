package org.reactome.server.tools.diagram.exporter.raster.color;

import java.awt.*;

public interface PropertiesColorSheet {

	Color getHalo();

	Color getFlag();

	Color getDisease();

	Color getSelection();

	Color getHovering();

	Color getHighlight();

	Color getText();

	Color getButton();

	Color getTrigger();
}
