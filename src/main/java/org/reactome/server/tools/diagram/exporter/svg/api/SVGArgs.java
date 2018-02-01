package org.reactome.server.tools.diagram.exporter.svg.api;

import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;

public class SVGArgs extends RasterArgs {

	public SVGArgs(String stId) {
		super(stId, "png");
	}

}
