package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.FoundEntity;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableNode;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.*;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers.TextLayer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.GradientSheet;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Basic node renderer. All renderers that render nodes should override it. To
 * modify the behaviour of rendering, you can override any of its methods.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public abstract class NodeAbstractRenderer extends ObjectRenderer {

	public void draw(RenderableNode renderableNode, DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		if (renderableNode.isFlag())
			flag(renderableNode, canvas, colorProfiles);
		if (renderableNode.isHalo())
			halo(renderableNode, canvas, colorProfiles);
		background(renderableNode, canvas, index, colorProfiles);
		double textSplit = analysis(renderableNode, canvas, index, colorProfiles, t);
		text(renderableNode, canvas, colorProfiles, index, textSplit);
		if (renderableNode.isCrossed())
			cross(renderableNode, canvas, colorProfiles);

	}

	public void flag(RenderableNode renderableNode, DiagramCanvas canvas, ColorProfiles colorProfiles) {
		canvas.getFlags().add(renderableNode.getBackgroundShape(),
				colorProfiles.getDiagramSheet().getProperties().getFlag(),
				StrokeStyle.FLAG.get(renderableNode.isDashed()));
	}

	public void halo(RenderableNode renderableNode, DiagramCanvas canvas, ColorProfiles colorProfiles) {
		canvas.getHalo().add(renderableNode.getBackgroundShape(),
				colorProfiles.getDiagramSheet().getProperties().getHalo(),
				StrokeStyle.HALO.get(renderableNode.isDashed()));
	}

	public void background(RenderableNode renderableNode, DiagramCanvas canvas, DiagramIndex index, ColorProfiles colorProfiles) {
		final Color fill = getFillColor(renderableNode, colorProfiles, index);
		final Color border = getStrokeColor(renderableNode, colorProfiles, index);

		if (renderableNode.isFadeOut()) {
			canvas.getFadeOutNodeForeground().add(renderableNode.getBackgroundArea(), fill);
			canvas.getFadeOutNodeBorder().add(renderableNode.getBackgroundShape(), border, StrokeStyle.BORDER.get(renderableNode.isDashed()));
		} else {
			canvas.getNodeBackground().add(renderableNode.getBackgroundArea(), fill);
			canvas.getNodeBorder().add(renderableNode.getBackgroundShape(), border, StrokeStyle.BORDER.get(renderableNode.isDashed()));
		}
	}

	public double analysis(RenderableNode renderableNode, DiagramCanvas canvas, DiagramIndex index, ColorProfiles colorProfiles, int t) {
		switch (index.getAnalysis().getType()) {
			case SPECIES_COMPARISON:
			case OVERREPRESENTATION:
				return enrichment(renderableNode, canvas, colorProfiles);
			case EXPRESSION:
				return expression(renderableNode, canvas, index, colorProfiles, t);
			case NONE:
			default:
				return 0.0;
		}
	}

	public double enrichment(RenderableNode renderableNode, DiagramCanvas canvas, ColorProfiles colorProfiles) {
		final Double percentage = renderableNode.getEnrichment();
		final NodeProperties prop = renderableNode.getNode().getProp();
		if (percentage != null && percentage > 0) {
			final Color color = colorProfiles.getAnalysisSheet().getEnrichment().getGradient().getMax();
			final Area enrichmentArea = new Area(renderableNode.getBackgroundShape());
			final Rectangle2D clip = new Rectangle2D.Double(
					prop.getX(),
					prop.getY(),
					prop.getWidth() * percentage,
					prop.getHeight());
			enrichmentArea.intersect(new Area(clip));
			renderableNode.getBackgroundArea().subtract(enrichmentArea);
			canvas.getNodeAnalysis().add(enrichmentArea, color);
		}
		return 0.0;
	}

	/**
	 * Adds expression strips for the node in info.
	 *
	 * @return a number, between 0 and 1 indicating where to split the text for
	 * this node. If 0, text will not be modified. If 1, all the text will be
	 * white.
	 */
	public double expression(RenderableNode renderableNode, DiagramCanvas canvas, DiagramIndex index, ColorProfiles colorProfiles, int t) {
		final List<FoundEntity> expressions = renderableNode.getHitExpressions();
		double splitText = 0.0;
		if (expressions != null) {
			final List<Double> values = expressions.stream()
					.map(participant -> participant.getExp().get(t))
					.collect(Collectors.toList());
			final int size = renderableNode.getTotalExpressions();

			final NodeProperties prop = renderableNode.getNode().getProp();
			final double x = prop.getX();
			final double y = prop.getY();
			final double height = prop.getHeight();
			final double partSize = prop.getWidth() / size;
			splitText = (double) values.size() / size;

			final double max = index.getAnalysis().getResult().getExpression().getMax();
			final double min = index.getAnalysis().getResult().getExpression().getMin();
			final double delta = 1 / (max - min);  // only one division
			for (int i = 0; i < values.size(); i++) {
				final double val = values.get(i);
				final double scale = 1 - (val - min) * delta;
				final GradientSheet gradient = colorProfiles.getAnalysisSheet().getExpression().getGradient();
				final Color color = ColorFactory.interpolate(gradient, scale);
				final Rectangle2D rect = new Rectangle2D.Double(
						x + i * partSize, y, partSize, height);
				final Area expressionArea = new Area(rect);
				expressionArea.intersect(new Area(renderableNode.getBackgroundShape()));
				canvas.getNodeAnalysis().add(expressionArea, color);
				renderableNode.getBackgroundArea().subtract(expressionArea);
			}
		}
		return splitText;
	}

	public void cross(RenderableNode renderableNode, DiagramCanvas canvas, ColorProfiles colorProfiles) {
		final Color color = colorProfiles.getDiagramSheet().getProperties().getDisease();
		final List<Shape> cross = ShapeFactory.cross(renderableNode.getNode().getProp());
		cross.forEach(line -> canvas.getCross().add(line, color, StrokeStyle.BORDER.get(renderableNode.isDashed())));
	}

//	protected void text(NodeRenderInfo info, double splitText) {
//		if (info.getForegroundShape() != null) {
//			final NodeProperties prop = info.getNode().getProp();
//			final Rectangle2D bounds = info.getForegroundShape().getBounds2D();
//			final NodeProperties limits = NodePropertiesFactory.get(
//					bounds.getX(), bounds.getY(), bounds.getWidth(),
//					bounds.getHeight());
//			// as splitText is in background dimensions,
//			// we need to change it to foreground percentage
//			splitText = (prop.getX() + splitText * prop.getWidth() - limits.getX()) / limits.getWidth();
//			info.getTextLayer().add(info.getNode().getDisplayName(), info.getTextColor(), limits, 1, splitText, FontProperties.DEFAULT_FONT);
//		} else
//			info.getTextLayer().add(info.getNode().getDisplayName(), info.getTextColor(),
//					info.getNode().getProp(), RendererProperties.NODE_TEXT_PADDING, splitText, FontProperties.DEFAULT_FONT);
//	}

	public void text(RenderableNode renderableNode, DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, double textSplit) {
		final TextLayer layer = renderableNode.isFadeOut()
				? canvas.getFadeOutText()
				: canvas.getText();
		final Color color = getTextColor(renderableNode, colorProfiles, index);
		layer.add(renderableNode.getNode().getDisplayName(),
				color,
				renderableNode.getNode().getProp(),
				RendererProperties.NODE_TEXT_PADDING,
				textSplit,
				FontProperties.DEFAULT_FONT);
	}

}
