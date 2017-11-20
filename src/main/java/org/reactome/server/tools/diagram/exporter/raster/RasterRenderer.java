package org.reactome.server.tools.diagram.exporter.raster;

import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDException;

import java.awt.image.BufferedImage;

public interface RasterRenderer {
	BufferedImage render() throws EHLDException;
}
