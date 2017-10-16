package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfileNode;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.awt.*;

/**
 * Sets add an inner border.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class SetRenderer extends NodeAbstractRenderer {

	@Override
	protected Shape backgroundShape(DiagramObject item) {
		final Node node = (Node) item;
		return ShapeFactory.roundedRectangle(node.getProp());
	}

	@Override
	protected Shape foregroundShape(NodeCommon node) {
		return ShapeFactory.roundedRectangle(node.getProp(), RendererProperties.SEPARATION);
	}

	@Override
	protected String getFgFill(AnalysisType analysisType, DiagramProfileNode profile) {
		return "rgba(0,0,0,0)";
	}
}
