package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.EdgeCommon;
import org.reactome.server.tools.diagram.data.layout.Shape;

import java.util.Collections;
import java.util.List;

/**
 * Renderer for flow lines.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class FlowLineRenderer extends EdgeRenderer {

	@Override
	protected List<Shape> renderableShapes(EdgeCommon edge) {
		return Collections.singletonList(edge.getEndShape());
	}
}
