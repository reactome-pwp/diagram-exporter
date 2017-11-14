package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.EdgeCommon;

/**
 * Renderer for flow lines.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class FlowLineRenderer extends LinkRenderer {

	@Override
	protected boolean dashed(EdgeCommon edge) {
		return false;
	}
}
