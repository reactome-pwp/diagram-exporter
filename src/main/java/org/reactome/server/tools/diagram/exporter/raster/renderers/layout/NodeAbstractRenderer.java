package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.profile.analysis.AnalysisProfile;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfileNode;
import org.reactome.server.tools.diagram.data.profile.interactors.InteractorProfile;
import org.reactome.server.tools.diagram.exporter.raster.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.*;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.FillLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.LineLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.TextLayer;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Basic node renderer. All Renderers that render nodes should override it. The
 * default behaviour consists on 3 steps: filling, drawing borders and drawing
 * texts.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public abstract class NodeAbstractRenderer extends AbstractRenderer {

//	/**
//	 * This method is called from <code>draw()</code> if fillColor is not null.
//	 * It calls <code>shape</code> for each node and fills those shapes with
//	 * fillColor. You only have to override it if filling has a different
//	 * behaviour.
//	 *
//	 * @param graphics where to render
//	 * @param items    list of nodes to fill
//	 */
//	@Override
//	public void fill(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
//		items.stream()
//				.map(node -> shape(graphics, node))
//				.forEach(graphics.getGraphics()::fill);
//	}
//
//	/**
//	 * This method is called from <code>draw()</code> if lineColor is not null.
//	 * It calls <code>shape</code> for each node and draws those shapes with
//	 * lineColor. After that, if any of the nodes is crossed, draws the cross.
//	 * You only have to override it if drawing borders has a different
//	 * behaviour.
//	 *
//	 * @param graphics where to render
//	 * @param items    list of nodes to draw
//	 */
//	@Override
//	public void border(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
//		items.stream()
//				.map(node -> shape(graphics, node))
//				.forEach(graphics.getGraphics()::draw);
//	}
//
//	/**
//	 * This method is called from <code>draw()</code> if textColor is not null.
//	 * It sets the color to textColor and calls <code>graphics.drawText(node)</code>
//	 * for each node. You only have to override it if drawing borders has a
//	 * different behaviour.
//	 *
//	 * @param graphics where to render
//	 * @param items    list of nodes to draw
//	 */
//
//	@Override
//	public void text(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
//		final Collection<? extends NodeCommon> nodes = (Collection<? extends NodeCommon>) items;
//		nodes.forEach(node -> TextRenderer.drawText(graphics, node));
//	}
//
//	@Override
//	public void cross(AdvancedGraphics2D graphics, Collection<Node> nodes) {
//		nodes.stream()
//				.filter(this::isCrossed)
//				.map(Node::getProp)
//				.map(properties -> ShapeFactory.cross(graphics, properties))
//				.flatMap(Collection::stream)
//				.forEach(graphics.getGraphics()::draw);
//	}


	@Override
	public void draw(DiagramCanvas canvas, DiagramObject item, DiagramProfile diagramProfile, AnalysisProfile analysisProfile, InteractorProfile interactorProfile, double factor, DiagramIndex index) {
		if (isFadeOut(item)) {
			asFadeOut(canvas, item, diagramProfile, index, factor);
		} else {
			asNormal(canvas, item, diagramProfile, analysisProfile, interactorProfile, index, factor);
		}
	}

	private boolean isFadeOut(DiagramObject node) {
		return node.getIsFadeOut() != null && node.getIsFadeOut();
	}

	private void asFadeOut(DiagramCanvas canvas, DiagramObject node, DiagramProfile diagramProfile, DiagramIndex index, double factor) {
		final Shape shape = shape(factor, node);
		final DiagramProfileNode clas = getDiagramProfileNode(node.getRenderableClass(), diagramProfile);
		fill(canvas.getFadeOutFills(), (NodeCommon) node, shape, clas, factor, diagramProfile);
		// Analysis

		final String border = computeBorderColor(node, diagramProfile, index, clas);
		border(canvas.getFadeOutBorders(), node, border, diagramProfile, index, shape, clas, factor);
		if (isCrossed((Node) node)) {
			final String disease = diagramProfile.getProperties().getDisease();
			final List<Shape> cross = ShapeFactory.cross(factor, ((Node) node).getProp());
			cross.forEach(line -> canvas.getCross().add(disease, StrokeProperties.BORDER_STROKE, line));
		}
		text(canvas.getFadeOutText(), (NodeCommon) node, factor, clas);
	}

	private void asNormal(DiagramCanvas canvas, DiagramObject node, DiagramProfile diagramProfile, AnalysisProfile analysisProfile, InteractorProfile interactorProfile, DiagramIndex index, double factor) {
		final Shape shape = shape(factor, node);
		flag(canvas, node, diagramProfile, index, shape);
		halo(canvas, node, diagramProfile, index, shape);
		final DiagramProfileNode clas = getDiagramProfileNode(node.getRenderableClass(), diagramProfile);
		fill(canvas.getFill(), (NodeCommon) node, shape, clas, factor, diagramProfile);
		// Analysis
		final String border = computeBorderColor(node, diagramProfile, index, clas);
		border(canvas.getBorder(), node, border, diagramProfile, index, shape, clas, factor);
		text(canvas.getText(), (NodeCommon) node, factor, clas);


	}

	protected void flag(DiagramCanvas canvas, DiagramObject node, DiagramProfile diagramProfile, DiagramIndex index, Shape shape) {
		if (index.getFlags().contains(node.getId())) {
			final String flag = diagramProfile.getProperties().getFlag();
			Stroke flagStroke = isDashed(node)
					? StrokeProperties.DASHED_FLAG_STROKE
					: StrokeProperties.FLAG_STROKE;
			canvas.getFlags().add(flag, flagStroke, shape);
		}
	}

	private void halo(DiagramCanvas canvas, DiagramObject node, DiagramProfile diagramProfile, DiagramIndex index, Shape shape) {
		if (index.getHaloed().contains(node.getId())) {
			final String color = diagramProfile.getProperties().getHalo();
			final Stroke stroke = isDashed(node)
					? StrokeProperties.DASHED_HALO_STROKE
					: StrokeProperties.HALO_STROKE;
			canvas.getHalos().add(color, stroke, shape);
		}
	}

	protected void fill(FillLayer fillLayer, NodeCommon node, Shape shape, DiagramProfileNode clas, double factor, DiagramProfile diagramProfile) {
		final String fill = clas.getFill();
		fillLayer.add(fill, shape);
	}

	private boolean isDisease(NodeCommon node) {
		return node.getIsDisease() != null && node.getIsDisease();
	}

	protected void border(LineLayer lineLayer, DiagramObject node, String border, DiagramProfile diagramProfile, DiagramIndex index, Shape shape, DiagramProfileNode clas, double factor) {
		final Stroke stroke = isDashed(node)
				? StrokeProperties.DASHED_BORDER_STROKE
				: StrokeProperties.BORDER_STROKE;
		lineLayer.add(border, stroke, shape);
	}

	private String computeBorderColor(DiagramObject node, DiagramProfile diagramProfile, DiagramIndex index, DiagramProfileNode clas) {
		return (index.getSelected().contains(node.getId()))
				? diagramProfile.getProperties().getSelection()
				: isDisease((NodeCommon) node)
				? diagramProfile.getProperties().getDisease()
				: clas.getStroke();
	}

	protected void text(TextLayer textLayer, NodeCommon node, double factor, DiagramProfileNode clas) {
		final NodeProperties properties = new ScaledNodeProperties(node.getProp(), factor);
		final String text = clas.getText();
		textLayer.add(text, node.getDisplayName(), properties, RendererProperties.NODE_TEXT_PADDING);
	}

	boolean isDashed(DiagramObject object) {
		if (object instanceof NodeCommon) {
			NodeCommon node = (NodeCommon) object;
			return node.getNeedDashedBorder() != null && node.getNeedDashedBorder();
		}
		return false;
	}

	private boolean isCrossed(Node node) {
		return node.getIsCrossed() != null && node.getIsCrossed();
	}

	/**
	 * Returns the proper java shape for a Node. By default creates a rectangle.
	 * Override it when you have a different shape.
	 *
	 * @param factor to take the factor
	 *
	 * @return a Shape in the graphics scale
	 */
	protected Shape shape(double factor, DiagramObject item) {
		final Node node = (Node) item;
		final NodeProperties properties = new ScaledNodeProperties(node.getProp(), factor);
		return new Rectangle2D.Double(properties.getX(), properties.getY(),
				properties.getWidth(), properties.getHeight());
	}
}
