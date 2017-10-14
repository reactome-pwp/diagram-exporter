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
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.DrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.FillLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.TextLayer;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.List;

import static org.reactome.server.tools.diagram.exporter.raster.renderers.common.StrokeProperties.StrokeStyle;
import static org.reactome.server.tools.diagram.exporter.raster.renderers.common.StrokeProperties.get;

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
		// 1 take aaaaalll the decisions
		final NodeCommon node = (NodeCommon) item;
		final boolean disease = isDisease(node);
		final boolean crossed = isCrossed(node);
		final boolean dashed = isDashed(node);
		final boolean fadeOut = isFadeOut(node);
		final boolean isFlag = index.getFlags().contains(node.getId());
		final boolean isSelected = index.getSelected().contains(node.getId());
		final boolean isHalo = index.getHaloed().contains(node.getId());
		// An area can be clip
		final Shape bgShape = backgroundShape(node);
		final Area bgArea = bgShape == null ? null : new Area(bgShape);
		final Shape foregroundShape = foregroundShape(node);
		final DiagramProfileNode profile = getDiagramProfileNode(node.getRenderableClass(), diagramProfile);
		final NodeProperties prop = node.getProp();
		final FillLayer bgLayer;
		final DrawLayer borderLayer;
		final TextLayer textLayer;
		final FillLayer fgLayer;
		final Stroke borderStroke;
		borderStroke = isSelected
				? StrokeStyle.SELECTION.getStroke(dashed)
				: StrokeStyle.BORDER.getStroke(dashed);

		final String borderColor;
		final String textColor;
		final String bgFillColor;

		if (fadeOut) {
			bgFillColor = profile.getFadeOutFill();
			borderColor = profile.getFadeOutStroke();
			textColor = profile.getFadeOutText();
		} else {
			if (analysisType == AnalysisType.NONE) {
				bgFillColor = profile.getFill();
				borderColor = isSelected
						? diagramProfile.getProperties().getSelection()
						: disease
						? diagramProfile.getProperties().getDisease()
						: profile.getStroke();
				textColor = profile.getText();
			} else {
				bgFillColor = profile.getLighterFill();
				borderColor = isSelected
						? diagramProfile.getProperties().getSelection()
						: disease
						? diagramProfile.getProperties().getDisease()
						: profile.getLighterStroke();
				textColor = profile.getLighterText();
			}
		}

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
			final Stroke flagStroke = get(StrokeStyle.FLAG, dashed);
			flag(canvas.getFlags(), node, bgArea, flagColor, flagStroke);
		}
		// 2 halo
		if (isHalo) {
			final String haloColor = diagramProfile.getProperties().getHalo();
			final Stroke haloStroke = StrokeStyle.HALO.getStroke(dashed);
			halo(canvas.getHalo(), bgArea, haloColor, haloStroke);
		}
		// 3 fill
		// 3.1 background
		// This area can be modified later
		if (bgArea != null) {
			background(bgLayer, node, bgArea, bgFillColor, fadeOut);
		}
		// 3.2 enrichments
		final Double percentage = index.getAnalysisValue(node);
		if (percentage != null && percentage > 0) {
			final String analysisColor = analysisProfile.getEnrichment().getGradient().getMax();
			final Area enrichment = new Area(bgShape);
			final Rectangle2D rectangle = new Rectangle2D.Double(prop.getX(),
					prop.getY(), prop.getWidth() * percentage, prop.getHeight());
			enrichment.intersect(new Area(rectangle));
			bgArea.subtract(enrichment);
			enrichment(canvas.getNodeEnrichment(), analysisColor, enrichment, node, fadeOut);
		}
		// 3.3 foreground
		if (foregroundShape != null) {
			final String fgFill = getFgFill(analysisType, profile);
			foreground(fgLayer, node, foregroundShape, fgFill);
		}
		// 4 border
		border(borderLayer, node, bgShape, foregroundShape, borderStroke, borderColor);

		// 5 text
		text(textLayer, node, foregroundShape, prop, textColor);

		// 6 cross
		if (crossed) {
			final String crossColor = diagramProfile.getProperties().getDisease();
			final List<Shape> cross = ShapeFactory.cross(node.getProp());
			cross.forEach(line -> canvas.getCross().add(crossColor, borderStroke, line));

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

	protected void border(DrawLayer layer, NodeCommon node, Shape backgroundShape, Shape foregroundShape, Stroke borderStroke, String borderColor) {
		if (backgroundShape != null)
			layer.add(borderColor, borderStroke, backgroundShape);
		if (foregroundShape != null)
			layer.add(borderColor, borderStroke, foregroundShape);
	}

	protected void foreground(FillLayer layer, NodeCommon node, Shape foregroundShape, String fgFill) {
		layer.add(fgFill, foregroundShape);
	}

	private void enrichment(FillLayer layer, String analysisColor, Area analysisShape, NodeCommon node, boolean fadeout) {
		layer.add(analysisColor, analysisShape);
	}

	private void background(FillLayer layer, NodeCommon node, Shape backgroundShape, String bgFill, boolean fadeout) {
		layer.add(bgFill, backgroundShape);
	}

	private void halo(DrawLayer layer, Shape shape, String haloColor, Stroke haloStroke) {
		layer.add(haloColor, haloStroke, shape);
	}

	private void flag(DrawLayer layer, NodeCommon node, Shape shape, String flagColor, Stroke flagStroke) {
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
