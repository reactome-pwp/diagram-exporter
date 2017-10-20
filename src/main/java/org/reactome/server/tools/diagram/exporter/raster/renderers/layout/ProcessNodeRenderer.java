package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.awt.*;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ProcessNodeRenderer extends NodeAbstractRenderer {

	// When it comes to analysis, alpha must be 0.75
	private static final Color ANALYSIS_INNER_COLOR = new Color(254, 253, 255, 191);
	private static final Color INNER_COLOR = new Color(254, 253, 255);

	@Override
	protected Shape foregroundShape(NodeCommon node) {
		return ShapeFactory.rectangle(node.getProp(), RendererProperties.PROCESS_NODE_PADDING);
	}

	@Override
	protected Color getFgFill(ColorProfiles colorProfiles, DiagramIndex index) {
		return index.getAnalysisType() == AnalysisType.NONE
				? INNER_COLOR
				: ANALYSIS_INNER_COLOR;
	}
}
