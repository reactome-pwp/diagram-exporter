package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.FoundEntity;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.*;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers.DrawLayer;
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
public abstract class NodeAbstractRenderer extends AbstractRenderer {

	@Override
	public void draw(DiagramCanvas canvas, DiagramObject item, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		final NodeCommon node = (NodeCommon) item;
		final NodeRenderInfo info = new NodeRenderInfo(node, index, colorProfiles, canvas, this);
		flag(info);
		halo(info);
		// color profiles for the gradients
		// index for the analysis max and min
		double splitText = analysis(colorProfiles, index, info, t);
		background(info);
		foreground(info);
		attachment(info);
		border(info);
		text(info, splitText);
		cross(info);
	}

	protected void flag(NodeRenderInfo info) {
		if (info.getDecorator().isFlag())
			info.getFlagLayer().add(info.getFlagColor(), info.getFlagStroke(), info.getBackgroundShape());
	}

	protected void halo(NodeRenderInfo info) {
		if (info.getDecorator().isHalo())
			info.getHaloLayer().add(info.getHaloColor(), info.getHaloStroke(), info.getBackgroundShape());
	}

	private void background(NodeRenderInfo info) {
		if (info.getBackgroundArea() != null) {
			info.getBgLayer().add(info.getBackgroundColor(), info.getBackgroundArea());
		}
	}

	private double analysis(ColorProfiles colorProfiles, DiagramIndex index, NodeRenderInfo info, int t) {
		switch (index.getAnalysis().getType()) {
			case SPECIES_COMPARISON:
			case OVERREPRESENTATION:
				enrichment(colorProfiles, info);
				return 0.0;
			case EXPRESSION:
				return expression(colorProfiles, info, index, t);
			case NONE:
			default:
				return 0.0;
		}
	}

	public void foreground(NodeRenderInfo info) {
		if (info.getForegroundShape() != null) {
			info.getFgLayer().add(info.getForegroundColor(), info.getForegroundShape());
		}
	}

	protected void border(NodeRenderInfo info) {
		final DrawLayer layer = info.getBorderLayer();
		final Stroke borderStroke = info.getBorderStroke();
		final Color borderColor = info.getBorderColor();
		if (info.getBackgroundShape() != null)
			layer.add(borderColor, borderStroke, info.getBackgroundShape());
		if (info.getForegroundShape() != null)
			layer.add(borderColor, borderStroke, info.getForegroundShape());
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
				info.getTextLayer().add(shape.getS(), text, limits, 1, 0, FontProperties.DEFAULT_FONT);
			}
		});
	}

	public void cross(NodeRenderInfo info) {
		if (info.isCrossed()) {
			final Color crossColor = info.getCrossColor();
			final List<Shape> cross = ShapeFactory.cross(info.getNode().getProp());
			cross.forEach(line -> info.getCrossLayer().add(crossColor, info.getBorderStroke(), line));
		}
	}

	protected void text(NodeRenderInfo info, double splitText) {
		if (info.getForegroundShape() != null) {
			final NodeProperties prop = info.getNode().getProp();
			final Rectangle2D bounds = info.getForegroundShape().getBounds2D();
			final NodeProperties limits = NodePropertiesFactory.get(
					bounds.getX(), bounds.getY(), bounds.getWidth(),
					bounds.getHeight());
			// as splitText is in background dimensions,
			// we need to change it to foreground percentage
			splitText = (prop.getX() + splitText * prop.getWidth() - limits.getX()) / limits.getWidth();
			info.getTextLayer().add(info.getNode().getDisplayName(), info.getTextColor(), limits, 1, splitText, FontProperties.DEFAULT_FONT);
		} else
			info.getTextLayer().add(info.getNode().getDisplayName(), info.getTextColor(),
					info.getNode().getProp(), RendererProperties.NODE_TEXT_PADDING, splitText, FontProperties.DEFAULT_FONT);
	}

	public void enrichment(ColorProfiles colorProfiles, NodeRenderInfo info) {
		final Double percentage = info.getDecorator().getEnrichment();
		final NodeProperties prop = info.getNode().getProp();
		if (percentage != null && percentage > 0) {
			final Color color = colorProfiles.getAnalysisSheet().getEnrichment().getGradient().getMax();
			final Area enrichmentArea = new Area(info.getBackgroundShape());
			final Rectangle2D clip = new Rectangle2D.Double(
					prop.getX(),
					prop.getY(),
					prop.getWidth() * percentage,
					prop.getHeight());
			enrichmentArea.intersect(new Area(clip));
			info.getBackgroundArea().subtract(enrichmentArea);
			info.getAnalysisLayer().add(color, enrichmentArea);
		}
	}

	/**
	 * Adds expression strips for the node in info.
	 *
	 * @return a number, between 0 and 1 indicating where to split the text for
	 * this node. If 0, text will not be modified. If 1, all the text will be
	 * white.
	 */
	public double expression(ColorProfiles colorProfiles, NodeRenderInfo info, DiagramIndex index, int t) {
		final List<FoundEntity> expressions = info.getDecorator().getHitExpressions();
		double splitText = 0.0;
		if (expressions != null) {
			final List<Double> values = expressions.stream()
					.map(participant -> participant.getExp().get(t))
					.collect(Collectors.toList());
			final int size = info.getDecorator().getTotalExpressions();

			final NodeProperties prop = info.getNode().getProp();
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
				final Area fillArea = new Area(rect);
				fillArea.intersect(new Area(info.getBackgroundShape()));
				info.getAnalysisLayer().add(color, fillArea);
				info.getBackgroundArea().subtract(fillArea);
			}
		}
		return splitText;
	}

	/**
	 * Returns the proper java shape for a Node. By default creates a rectangle.
	 * Override it when you have a different shape.
	 */
	public Shape backgroundShape(NodeCommon node) {
		final NodeProperties properties = node.getProp();
		return new Rectangle2D.Double(properties.getX(), properties.getY(),
				properties.getWidth(), properties.getHeight());
	}

	/**
	 * Get the foreground shape. The foreground shape is rendered over the
	 * background and the analysis.
	 */
	public Shape foregroundShape(NodeCommon node) {
		return null;
	}

	/**
	 * It's responsibility of the renderer to determine the color of the
	 * foreground, as it is not in the stylesheet. This color is only needed if
	 * foregroundShape is not null. By default returns a WHITE with alpha 0.
	 */
	public Color getForegroundFill(ColorProfiles colors, DiagramIndex index) {
		return new Color(255, 255, 255, 0);
	}
}
