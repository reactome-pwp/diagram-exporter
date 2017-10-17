package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.data.profile.analysis.AnalysisProfile;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfileNode;
import org.reactome.server.tools.diagram.data.profile.interactors.InteractorProfile;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.NodeRenderInfo;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.DrawLayer;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Basic node renderer. All Renderers that render nodes should override it. The
 * default behaviour consists on 3 steps: filling, drawing borders and drawing
 * texts.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public abstract class NodeAbstractRenderer extends AbstractRenderer {

	private static final int COL = 0;

	@Override
	public void draw(DiagramCanvas canvas, DiagramObject item, DiagramProfile diagramProfile, AnalysisProfile analysisProfile, InteractorProfile interactorProfile, DiagramIndex index) {
		final NodeCommon node = (NodeCommon) item;
		final Shape bgShape = backgroundShape(node);
		final Shape fgShape = foregroundShape(node);
		final NodeRenderInfo info = new NodeRenderInfo(node, index, diagramProfile, canvas, bgShape, fgShape);
		flag(canvas, info);
		halo(canvas, info);

		// 3 fill (bg, analysis, fg)
		background(info);

		double splitText = 0.0;
		if (index.getAnalysisType() == AnalysisType.EXPRESSION)
			splitText = expression(canvas, analysisProfile, index, info);
		else if (index.getAnalysisType() == AnalysisType.OVERREPRESENTATION)
			enrichment(canvas, analysisProfile, node, bgShape, info);
		final String fgFill = getFgFill(index.getAnalysisType(), info.getProfile());
		foreground(index, info, fgFill);

		border(info);
		text(node, info, splitText);
		cross(canvas, diagramProfile, info);
	}

	protected void foreground(DiagramIndex index, NodeRenderInfo info, String fgFill) {
		if (info.getForegroundShape() != null) {
			info.getFgLayer().add(fgFill, info.getForegroundShape());
		}
	}

	private void cross(DiagramCanvas canvas, DiagramProfile diagramProfile, NodeRenderInfo info) {
		if (info.isCrossed()) {
			final String crossColor = diagramProfile.getProperties().getDisease();
			final List<Shape> cross = ShapeFactory.cross(info.getProp());
			cross.forEach(line -> canvas.getCross().add(crossColor, info.getBorderStroke(), line));

		}
	}

	private void background(NodeRenderInfo info) {
		if (info.getBackgroundArea() != null) {
			info.getBgLayer().add(info.getBackgroundColor(), info.getBackgroundArea());
		}
	}

	private void halo(DiagramCanvas canvas, NodeRenderInfo info) {
		if (info.getDecorator().isHalo())
			canvas.getHalo().add(info.getHaloColor(), info.getHaloStroke(), info.getBackgroundShape());
	}

	private void flag(DiagramCanvas canvas, NodeRenderInfo info) {
		if (info.getDecorator().isFlag())
			canvas.getFlags().add(info.getFlagColor(), info.getFlagStroke(), info.getBackgroundShape());
	}

	protected void border(NodeRenderInfo info) {
		// 4 border
		DrawLayer layer = info.getBorderLayer();
		Stroke borderStroke = info.getBorderStroke();
		String borderColor = info.getBorderColor();
		if (info.getBackgroundShape() != null)
			layer.add(borderColor, borderStroke, info.getBackgroundShape());
		if (info.getForegroundShape() != null)
			layer.add(borderColor, borderStroke, info.getForegroundShape());
	}

	private void text(NodeCommon node, NodeRenderInfo info, double splitText) {
		if (info.getForegroundShape() != null) {
			final Rectangle2D bounds = info.getForegroundShape().getBounds2D();
			final NodeProperties limits = NodePropertiesFactory.get(
					bounds.getX(), bounds.getY(), bounds.getWidth(),
					bounds.getHeight());
			// as splitText is in background dimensions,
			// we need to change to foreground percentage
			splitText = (node.getProp().getX() + splitText * node.getProp().getWidth() - limits.getX()) / limits.getWidth();
			info.getTextLayer().add(info.getTextColor(), node.getDisplayName(), limits, 1, splitText);
		} else
			info.getTextLayer().add(info.getTextColor(), node.getDisplayName(), node.getProp(), RendererProperties.NODE_TEXT_PADDING, splitText);
	}

	private void enrichment(DiagramCanvas canvas, AnalysisProfile analysisProfile, NodeCommon node, Shape bgShape, NodeRenderInfo info) {
		final Double percentage = info.getDecorator().getEnrichment();
		if (percentage != null && percentage > 0) {
			final String analysisColor = analysisProfile.getEnrichment().getGradient().getMax();
			final Area enrichment = new Area(bgShape);
			final Rectangle2D rectangle = new Rectangle2D.Double(node.getProp().getX(),
					node.getProp().getY(), node.getProp().getWidth() * percentage, node.getProp().getHeight());
			enrichment.intersect(new Area(rectangle));
			info.getBackgroundArea().subtract(enrichment);
			canvas.getNodeAnalysis().add(analysisColor, enrichment);
		}
	}

	private double expression(DiagramCanvas canvas, AnalysisProfile analysisProfile, DiagramIndex index, NodeRenderInfo info) {
		final List<List<Double>> expressions = info.getDecorator().getExpressions();
		double splitText = 0.0;
		if (expressions != null) {
			final List<Double> values = expressions.stream()
					.filter(Objects::nonNull)
					.map(doubles -> doubles.get(COL))
					.collect(Collectors.toList());
			values.sort(Collections.reverseOrder());
			final int size = expressions.size();
			final double x = info.getProp().getX();
			final double y = info.getProp().getY();
			final double height = info.getProp().getHeight();
			final double partSize = info.getProp().getWidth() / size;
			splitText = (double) values.size() / expressions.size();

			final double max = index.getMaxExpression();
			final double min = index.getMinExpression();
			for (int i = 0; i < values.size(); i++) {
				final double val = values.get(i);
				final double scale = (val - min) / (max - min);
				final String color = ColorFactory.interpolate(analysisProfile.getExpression().getGradient(), scale);
				final Rectangle2D rect = new Rectangle2D.Double(
						x + i * partSize, y, partSize, height);
				final Area fillArea = new Area(rect);
				fillArea.intersect(new Area(info.getBackgroundShape()));
				canvas.getNodeAnalysis().add(color, fillArea);
			}
		}
		return splitText;
	}

	/**
	 * Returns the proper java shape for a Node. By default creates a rectangle.
	 * Override it when you have a different shape.
	 *
	 * @return a Shape in the graphics scale
	 */
	protected Shape backgroundShape(DiagramObject item) {
		final Node node = (Node) item;
		final NodeProperties properties = node.getProp();
		return new Rectangle2D.Double(properties.getX(), properties.getY(),
				properties.getWidth(), properties.getHeight());
	}

	protected Shape foregroundShape(NodeCommon node) {
		return null;
	}

	protected String getFgFill(AnalysisType analysisType, DiagramProfileNode profile) {
		return "#FFFFFF";
	}
}
