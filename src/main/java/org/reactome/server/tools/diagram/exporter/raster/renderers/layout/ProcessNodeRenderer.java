package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfileNode;
import org.reactome.server.tools.diagram.exporter.raster.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.awt.*;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ProcessNodeRenderer extends NodeAbstractRenderer {

	// When it comes to analysis, alpha must be 0.75
	private static final String ANALYSIS_INNER_COLOR = "rgba(254, 253, 255, 0.75)";
	private static final String INNER_COLOR = "#fefdff";

	@Override
	protected Shape foregroundShape(NodeCommon node) {
		return ShapeFactory.rectangle(node.getProp(), RendererProperties.PROCESS_NODE_INSET_WIDTH);
	}

	@Override
	protected String getFgFill(AnalysisType analysisType, DiagramProfileNode profile) {
		return analysisType == AnalysisType.NONE
				? INNER_COLOR
				: ANALYSIS_INNER_COLOR;
	}
}
