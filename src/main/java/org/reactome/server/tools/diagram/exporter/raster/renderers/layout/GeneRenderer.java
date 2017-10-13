package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfileNode;
import org.reactome.server.tools.diagram.exporter.raster.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.FillLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.LineLayer;

import java.awt.*;

/**
 * Renderer for genes. These ones are a little bit more complex than the rest.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class GeneRenderer extends NodeAbstractRenderer {

	@Override
	protected void border(LineLayer layer, NodeCommon node, Shape backgroundShape, Shape foregroundShape, Stroke borderStroke, String borderColor) {
		final Shape line = line(node);
		layer.add(borderColor, borderStroke, line);
		final Shape arrow = arrow(node);
		layer.add(borderColor, borderStroke, arrow);
	}

	@Override
	protected void foreground(FillLayer canvas, NodeCommon node, Shape foregroundShape, String fgFill) {
		super.foreground(canvas, node, foregroundShape, fgFill);
		final Shape arrow = arrow(node);
		canvas.add(fgFill, arrow);
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

	private Shape line(NodeCommon node) {
		final NodeProperties prop = node.getProp();
		final double x = prop.getX();
		final double y = prop.getY();
		final double width = prop.getWidth();
		return ShapeFactory.getGeneLine(x, y, width);
	}

	private Shape arrow(NodeCommon node) {
		final NodeProperties prop = node.getProp();
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
