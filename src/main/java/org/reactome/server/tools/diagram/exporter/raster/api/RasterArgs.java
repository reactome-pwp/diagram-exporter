package org.reactome.server.tools.diagram.exporter.raster.api;


import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;
import java.util.Set;

/** Arguments for the diagram exporter */
public interface RasterArgs {

	String getStId();

	Double getFactor();

	String getFormat();

	String getToken();

	Set<String> getFlags();

	Set<String> getSelected();

	ColorProfiles getProfiles();

	Color getBackground();

	Integer getColumn();

}
