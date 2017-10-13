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
import org.reactome.server.tools.diagram.exporter.raster.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.StrokeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.FillLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.LineLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.TextLayer;

import java.awt.*;
import java.awt.geom.Area;
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

	@Override
	public void draw(DiagramCanvas canvas, DiagramObject item, DiagramProfile diagramProfile, AnalysisProfile analysisProfile, InteractorProfile interactorProfile, DiagramIndex index, AnalysisType analysisType) {
		/*
		 * First part of method: take all the decisions
		 * Second part: render
		 */
		final NodeCommon node = (NodeCommon) item;
		final boolean disease = isDisease(node);
		final boolean crossed = isCrossed(node);
		final boolean dashed = isDashed(node);
		final boolean fadeOut = isFadeOut(node);
		final boolean isFlag = index.getFlags().contains(node.getId());
		final boolean isSelected = index.getSelected().contains(node.getId());
		final boolean isHalo = index.getHaloed().contains(node.getId());
		final Shape backgroundShape = backgroundShape(node);
		final Shape foregroundShape = foregroundShape(node);
		final DiagramProfileNode profile = getDiagramProfileNode(node.getRenderableClass(), diagramProfile);
		final NodeProperties prop = node.getProp();
		final FillLayer bgLayer;
		final LineLayer borderLayer;
		final TextLayer textLayer;
		final FillLayer fgLayer;
		if (fadeOut) {
			bgLayer = canvas.getFadeOutNodeBackground();
			fgLayer = canvas.getFadeOutNodeForeground();
			borderLayer = canvas.getFadeOutNodeBorder();
			textLayer = canvas.getFadeOutText();
		} else {
			bgLayer = canvas.getNodeBackground();
			fgLayer = canvas.getNodeForeground();
			borderLayer = canvas.getNodeBorder();
			textLayer = canvas.getText();
		}
		// 1 flag
		if (isFlag) {
			final String flagColor = diagramProfile.getProperties().getFlag();
			final Stroke flagStroke = dashed
					? StrokeProperties.DASHED_FLAG_STROKE
					: StrokeProperties.FLAG_STROKE;
			flag(canvas.getFlags(), node, backgroundShape, flagColor, flagStroke);
		}
		// 2 halo
		if (isHalo) {
			final String haloColor = diagramProfile.getProperties().getHalo();
			final Stroke haloStroke = dashed
					? StrokeProperties.DASHED_HALO_STROKE
					: StrokeProperties.HALO_STROKE;
			halo(canvas.getHalos(), backgroundShape, haloColor, haloStroke);
		}
		// 3 fill
		// 3.1 background
		if (backgroundShape != null) {
			final String bgFill;
			if (fadeOut) bgFill = profile.getFadeOutFill();
			else if (analysisType != AnalysisType.NONE)
				bgFill = profile.getLighterFill();
			else bgFill = profile.getFill();
			background(bgLayer, node, backgroundShape, bgFill, fadeOut);
		}
		// 3.2 enrichments
		final Double percentage = index.getAnalysisValue(node);
		if (percentage != null && percentage > 0) {
			final String analysisColor = analysisProfile.getEnrichment().getGradient().getMax();
			final Area analysisShape = new Area(backgroundShape);
			final Rectangle2D rectangle = new Rectangle2D.Double(prop.getX(),
					prop.getY(), prop.getWidth() * percentage, prop.getHeight());
			analysisShape.intersect(new Area(rectangle));
			analysis(canvas.getAnalysis(), analysisColor, analysisShape, node, fadeOut);
		}
		// 3.3 foreground
		if (foregroundShape != null) {
			final String fgFill = getFgFill(analysisType, profile);
			foreground(fgLayer, node, foregroundShape, fgFill);
		}
		// 4 border
		final Stroke borderStroke;
		borderStroke = isSelected
				? dashed
				? StrokeProperties.DASHED_SELECTION_STROKE
				: StrokeProperties.SELECTION_STROKE
				: dashed
				? StrokeProperties.DASHED_BORDER_STROKE
				: StrokeProperties.BORDER_STROKE;
		final String borderColor;
		if (fadeOut)
			borderColor = profile.getFadeOutStroke();
		else if (isSelected)
			borderColor = diagramProfile.getProperties().getSelection();
		else if (disease)
			borderColor = diagramProfile.getProperties().getDisease();
		else if (analysisType != AnalysisType.NONE)
			borderColor = profile.getLighterStroke();
		else borderColor = profile.getStroke();
		border(borderLayer, node, backgroundShape, foregroundShape, borderStroke, borderColor);

		// 5 text
		final String textColor;
		if (fadeOut) textColor = profile.getFadeOutText();
		else if (analysisType != AnalysisType.NONE)
			textColor = profile.getLighterText();
		else textColor = profile.getText();
		text(textLayer, node, foregroundShape, prop, textColor);

		// 6 cross
		if (crossed) {
			final String crossColor = diagramProfile.getProperties().getDisease();
			final List<Shape> cross = ShapeFactory.cross(node.getProp());
			cross.forEach(line -> canvas.getCross().add(crossColor, StrokeProperties.BORDER_STROKE, line));

		}
	}

	private void text(TextLayer layer, NodeCommon node, Shape foregroundShape, NodeProperties prop, String textColor) {
		if (foregroundShape != null) {
			final Rectangle2D bounds = foregroundShape.getBounds2D();
			final NodeProperties limits = NodePropertiesFactory.get(
					bounds.getX(), bounds.getY(), bounds.getWidth(),
					bounds.getHeight());
			layer.add(textColor, node.getDisplayName(), limits, 1);
		} else
			layer.add(textColor, node.getDisplayName(), prop, RendererProperties.NODE_TEXT_PADDING);
	}

	protected void border(LineLayer layer, NodeCommon node, Shape backgroundShape, Shape foregroundShape, Stroke borderStroke, String borderColor) {
		if (backgroundShape != null)
			layer.add(borderColor, borderStroke, backgroundShape);
		if (foregroundShape != null)
			layer.add(borderColor, borderStroke, foregroundShape);
	}

	protected void foreground(FillLayer layer, NodeCommon node, Shape foregroundShape, String fgFill) {
		layer.add(fgFill, foregroundShape);
	}

	private void analysis(FillLayer layer, String analysisColor, Area analysisShape, NodeCommon node, boolean fadeout) {
		layer.add(analysisColor, analysisShape);
	}

	private void background(FillLayer layer, NodeCommon node, Shape backgroundShape, String bgFill, boolean fadeout) {
		layer.add(bgFill, backgroundShape);
	}

	private void halo(LineLayer layer, Shape shape, String haloColor, Stroke haloStroke) {
		layer.add(haloColor, haloStroke, shape);
	}

	private void flag(LineLayer layer, NodeCommon node, Shape shape, String flagColor, Stroke flagStroke) {
		layer.add(flagColor, flagStroke, shape);
	}

	private boolean isFadeOut(DiagramObject node) {
		return node.getIsFadeOut() != null && node.getIsFadeOut();
	}

	private boolean isDisease(NodeCommon node) {
		return node.getIsDisease() != null && node.getIsDisease();
	}

	private boolean isDashed(DiagramObject object) {
		if (object instanceof NodeCommon) {
			NodeCommon node = (NodeCommon) object;
			return node.getNeedDashedBorder() != null && node.getNeedDashedBorder();
		}
		return false;
	}

	private boolean isCrossed(NodeCommon node) {
		return node.getIsCrossed() != null && node.getIsCrossed();
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
