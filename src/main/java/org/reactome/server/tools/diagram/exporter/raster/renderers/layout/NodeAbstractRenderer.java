package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.NodeRenderInfo;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.DrawLayer;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Basic node renderer. All renderers that render nodes should override it. To
 * modify the behaviour of rendering, you can override any protected method.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public abstract class NodeAbstractRenderer extends AbstractRenderer {

	private static final int COL = 0;

	@Override
	public void draw(DiagramCanvas canvas, DiagramObject item, ColorProfiles colorProfiles, DiagramIndex index) {
		final NodeCommon node = (NodeCommon) item;
		final Shape bgShape = backgroundShape(node);
		final Shape fgShape = foregroundShape(node);
		final NodeRenderInfo info = new NodeRenderInfo(node, index, colorProfiles, canvas, bgShape, fgShape, getFgFill(colorProfiles, index));
		flag(canvas, info);
		halo(canvas, info);

		// 3 fill (bg, analysis, fg)
		background(info);
		double splitText = analysis(canvas, colorProfiles, index, info);
		foreground(info);
		attachment(info);
		border(info);
		text(info, splitText);
		cross(canvas, colorProfiles, info);
	}

	private double analysis(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, NodeRenderInfo info) {
		double splitText = 0.0;
		if (index.getAnalysisType() == AnalysisType.EXPRESSION)
			splitText = expression(canvas, colorProfiles, index, info);
		else if (index.getAnalysisType() == AnalysisType.OVERREPRESENTATION)
			enrichment(canvas, colorProfiles, info);
		return splitText;
	}

	protected void foreground(NodeRenderInfo info) {
		if (info.getForegroundShape() != null) {
			info.getFgLayer().add(info.getForegroundColor(), info.getForegroundShape());
		}
	}

	private void cross(DiagramCanvas canvas, ColorProfiles colors, NodeRenderInfo info) {
		if (info.isCrossed()) {
			final Color crossColor = colors.getDiagramSheet().getProperties().getDisease();
			final List<Shape> cross = ShapeFactory.cross(info.getNode().getProp());
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

	protected void attachment(NodeRenderInfo info) {
		if (!(info.getNode() instanceof Node)) return;
		final Node node = (Node) info.getNode();
		if (node.getNodeAttachments() == null) return;
		final Color fill = info.getAttachmentsFill();
		final Color border = info.getAttachmentsBorder();
		final Color text = info.getAttachmentsText();
		final Stroke stroke = info.getAttachmentStroke();
		node.getNodeAttachments().forEach(nodeAttachment -> {
			final org.reactome.server.tools.diagram.data.layout.Shape shape = nodeAttachment.getShape();
			final List<Shape> shapes = ShapeFactory.getShapes(shape);
			shapes.forEach(sh -> info.getAttachmentsLayer().add(fill, border, stroke, sh));
			if (shape.getS() != null && !shape.getS().isEmpty()) {
				final NodeProperties limits = NodePropertiesFactory.get(
						shape.getA().getX(), shape.getA().getY(),
						shape.getB().getX() - shape.getA().getX(),
						shape.getB().getY() - shape.getA().getY());
				info.getTextLayer().add(text, shape.getS(), limits, 1, 0);
			}
		});

	}

	private void flag(DiagramCanvas canvas, NodeRenderInfo info) {
		if (info.getDecorator().isFlag())
			canvas.getFlags().add(info.getFlagColor(), info.getFlagStroke(), info.getBackgroundShape());
	}

	protected void border(NodeRenderInfo info) {
		// 4 border
		DrawLayer layer = info.getBorderLayer();
		Stroke borderStroke = info.getBorderStroke();
		Color borderColor = info.getBorderColor();
		if (info.getBackgroundShape() != null)
			layer.add(borderColor, borderStroke, info.getBackgroundShape());
		if (info.getForegroundShape() != null)
			layer.add(borderColor, borderStroke, info.getForegroundShape());
	}

	private void text(NodeRenderInfo info, double splitText) {
		if (info.getForegroundShape() != null) {
			final NodeProperties prop = info.getNode().getProp();
			final Rectangle2D bounds = info.getForegroundShape().getBounds2D();
			final NodeProperties limits = NodePropertiesFactory.get(
					bounds.getX(), bounds.getY(), bounds.getWidth(),
					bounds.getHeight());
			// as splitText is in background dimensions,
			// we need to change to foreground percentage
			splitText = (prop.getX() + splitText * prop.getWidth() - limits.getX()) / limits.getWidth();
			info.getTextLayer().add(info.getTextColor(), info.getNode().getDisplayName(), limits, 1, splitText);
		} else
			info.getTextLayer().add(info.getTextColor(), info.getNode().getDisplayName(),
					info.getNode().getProp(), RendererProperties.NODE_TEXT_PADDING, splitText);
	}

	private void enrichment(DiagramCanvas canvas, ColorProfiles colorProfiles, NodeRenderInfo info) {
		final Double percentage = info.getDecorator().getEnrichment();
		final NodeProperties prop = info.getNode().getProp();
		if (percentage != null && percentage > 0) {
			final Color analysisColor = colorProfiles.getAnalysisSheet().getEnrichment().getGradient().getMax();
			final Area enrichment = new Area(info.getBackgroundShape());
			final Rectangle2D rectangle = new Rectangle2D.Double(
					prop.getX(),
					prop.getY(),
					prop.getWidth() * percentage,
					prop.getHeight());
			enrichment.intersect(new Area(rectangle));
			info.getBackgroundArea().subtract(enrichment);
			canvas.getNodeAnalysis().add(analysisColor, enrichment);
		}
	}

	private double expression(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, NodeRenderInfo info) {
		final List<DiagramIndex.Participant> expressions = info.getDecorator().getExpressions();
		final NodeProperties prop = info.getNode().getProp();
		double splitText = 0.0;
		if (expressions != null) {
			final List<DiagramIndex.Participant> withExpression = expressions.stream()
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			withExpression.sort(Comparator.comparing(DiagramIndex.Participant::getIdentifier));
			final List<Double> values = withExpression.stream()
					.map(participant -> participant.getExp().get(COL))
					.collect(Collectors.toList());
			final int size = expressions.size();
			final double x = prop.getX();
			final double y = prop.getY();
			final double height = prop.getHeight();
			final double partSize = prop.getWidth() / size;
			splitText = (double) values.size() / expressions.size();

			final double max = index.getMaxExpression();
			final double min = index.getMinExpression();
			final double delta = 1 / (max - min);  // only one division
			for (int i = 0; i < values.size(); i++) {
				final double val = values.get(i);
				final double scale = (val - min) * delta;
				final Color color = ColorFactory.interpolate(colorProfiles.getAnalysisSheet().getExpression().getGradient(), scale);
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
	protected Shape backgroundShape(NodeCommon node) {
		final NodeProperties properties = node.getProp();
		return new Rectangle2D.Double(properties.getX(), properties.getY(),
				properties.getWidth(), properties.getHeight());
	}

	protected Shape foregroundShape(NodeCommon node) {
		return null;
	}

	protected Color getFgFill(ColorProfiles colorProfiles, DiagramIndex index) {
		return new Color(255, 255, 255);
	}
}
