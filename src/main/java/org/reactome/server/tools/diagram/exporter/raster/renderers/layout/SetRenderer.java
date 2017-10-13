package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfileNode;
import org.reactome.server.tools.diagram.exporter.raster.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.awt.*;

/**
 * Sets add an inner border.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class SetRenderer extends NodeAbstractRenderer {

	@Override
	protected Shape backgroundShape(double factor, DiagramObject item) {
		final Node node = (Node) item;
		final NodeProperties prop = new ScaledNodeProperties(node.getProp(), factor);
		return ShapeFactory.roundedRectangle(
				prop.getX(), prop.getY(), prop.getWidth(), prop.getHeight());
	}

	@Override
	protected Shape foregroundShape(double factor, NodeCommon node) {
		final NodeProperties prop = new ScaledNodeProperties(node.getProp(), factor);
		return ShapeFactory.roundedRectangle(
				prop.getX(),
				prop.getY(),
				prop.getWidth(),
				prop.getHeight(), RendererProperties.SEPARATION);
	}

	@Override
	protected String getFgFill(AnalysisType analysisType, DiagramProfileNode profile) {
		return "rgba(0,0,0,0)";
//		return analysisType == AnalysisType.NONE
//				? profile.getFill()
//				: profile.getLighterFill();
	}
}
