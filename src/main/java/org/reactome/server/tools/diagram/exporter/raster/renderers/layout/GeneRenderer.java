package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
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
		final Shape line = line(info.getNode().getProp());
		info.getBorderLayer().add(info.getBorderColor(), info.getBorderStroke(), line);
		final Shape arrow = arrow(info.getNode().getProp());
		info.getBorderLayer().add(info.getBorderColor(), info.getBorderStroke(), arrow);
	}

	@Override
	public void foreground(NodeRenderInfo info) {
		super.foreground(info);
		final Shape arrow = arrow(info.getNode().getProp());
		info.getFgLayer().add(info.getForegroundColor(), arrow);
	}

	@Override
	public Shape backgroundShape(NodeCommon item) {
		return null;
	}

	@Override
	public Shape foregroundShape(NodeCommon node) {
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
	public Color getForegroundFill(ColorProfiles colors, DiagramIndex index) {
		return index.getAnalysisType() == AnalysisType.NONE
				? colors.getDiagramSheet().getGene().getFill()
				: colors.getDiagramSheet().getGene().getLighterFill();
	}
}
