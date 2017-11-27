package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.EdgeCommon;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class InteractionRenderer extends LinkRenderer {

	@Override
	protected boolean dashed(EdgeCommon edge) {
		return false;
	}
}
