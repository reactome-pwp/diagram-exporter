package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.*;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;
import java.awt.geom.Rectangle2D;

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
	protected void halo(NodeRenderInfo info) {
		if (info.getDecorator().isHalo()) {
			final Shape line = line(info.getNode().getProp());
			info.getHaloLayer().add(info.getHaloColor(), info.getHaloStroke(), line);
			final Shape arrow = arrow(info.getNode().getProp());
			info.getHaloLayer().add(info.getHaloColor(), info.getHaloStroke(), arrow);
		}
	}

	@Override
	protected void flag(NodeRenderInfo info) {
		if (info.getDecorator().isFlag()) {
			final Shape line = line(info.getNode().getProp());
			info.getHaloLayer().add(info.getFlagColor(), info.getFlagStroke(), line);
			final Shape arrow = arrow(info.getNode().getProp());
			info.getHaloLayer().add(info.getFlagColor(), info.getFlagStroke(), arrow);
		}
	}

	@Override
	public Color getForegroundFill(ColorProfiles colors, DiagramIndex index) {
		return index.getAnalysis().getType() == AnalysisType.NONE
				? colors.getDiagramSheet().getGene().getFill()
				: colors.getDiagramSheet().getGene().getLighterFill();
	}

	@Override
	protected void text(NodeRenderInfo info, double splitText) {
		final Rectangle2D bounds = info.getForegroundShape().getBounds2D();
		if (bounds.getHeight() > FontProperties.DEFAULT_FONT.getSize()) {
			final NodeProperties limits = NodePropertiesFactory.get(
					bounds.getX(), bounds.getY(), bounds.getWidth(),
					bounds.getHeight());
			info.getTextLayer().add(info.getNode().getDisplayName(), info.getTextColor(), limits, RendererProperties.NODE_TEXT_PADDING, splitText, FontProperties.DEFAULT_FONT);
		} else
			info.getTextLayer().add(info.getNode().getDisplayName(), info.getTextColor(), info.getNode().getProp(), RendererProperties.NODE_TEXT_PADDING, splitText, FontProperties.DEFAULT_FONT);
	}
}
