package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfileNode;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.NodeRenderInfo;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.awt.*;

/**
 * Renderer for genes. These ones are a little bit more complex than the rest.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class GeneRenderer extends NodeAbstractRenderer {

	@Override
	protected void border(NodeRenderInfo info) {
		final Shape line = line(info.getProp());
		info.getBorderLayer().add(info.getBorderColor(), info.getBorderStroke(), line);
		final Shape arrow = arrow(info.getProp());
		info.getBorderLayer().add(info.getBorderColor(), info.getBorderStroke(), arrow);
	}

	@Override
	protected void foreground(DiagramIndex index, NodeRenderInfo info, String fgFill) {
		super.foreground(index, info, fgFill);
		final Shape arrow = arrow(info.getProp());
		info.getFgLayer().add(fgFill, arrow);
	}

	@Override
	protected Shape backgroundShape(DiagramObject item) {
		return null;
	}

	@Override
	protected Shape foregroundShape(NodeCommon node) {
		final NodeProperties prop = node.getProp();
		final double x = prop.getX();
		final double y = prop.getY();
		final double width = prop.getWidth();
		final double height = prop.getHeight();
		return ShapeFactory.getGeneFillShape(x, y, width, height);
	}

	private Shape line(NodeProperties prop) {
		final double x = prop.getX();
		final double y = prop.getY();
		final double width = prop.getWidth();
		return ShapeFactory.getGeneLine(x, y, width);
	}

	private Shape arrow(NodeProperties prop) {
		final double x = prop.getX();
		final double y = prop.getY();
		final double width = prop.getWidth();
		return ShapeFactory.getGeneArrow(x, y, width);
	}

	@Override
	protected String getFgFill(AnalysisType analysisType, DiagramProfileNode profile) {
		return analysisType == AnalysisType.NONE
				? profile.getFill()
				: profile.getLighterFill();
	}
}
