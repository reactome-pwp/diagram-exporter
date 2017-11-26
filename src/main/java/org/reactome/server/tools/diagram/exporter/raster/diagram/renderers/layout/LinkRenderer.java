package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.EdgeCommon;
import org.reactome.server.tools.diagram.data.layout.Shape;

import java.util.Collections;
import java.util.List;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class LinkRenderer extends EdgeRenderer {

	@Override
	protected List<Shape> renderableShapes(EdgeCommon edge) {
		return Collections.singletonList(edge.getEndShape());
	}

	@Override
	protected boolean dashed(EdgeCommon edge) {
		return true;
	}
}
