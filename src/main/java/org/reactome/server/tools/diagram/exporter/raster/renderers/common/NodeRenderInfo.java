package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.color.DiagramSheet;
import org.reactome.server.tools.diagram.exporter.raster.color.NodeColorSheet;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.DrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.FillDrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.FillLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.TextLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.NodeAbstractRenderer;

import java.awt.*;
import java.awt.geom.Area;

/**
 * When a node is renderer, there are a lot of decisions that must be made.
 * Colors, shapes, dashing... And these decisions depends on more than one
 * element: node, renderer, color profile, decorator, token. The
 * NodeRendererInfo takes all these decisions and give you basic getters
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class NodeRenderInfo extends DiagramObjectInfo {

	private final boolean crossed;

	private final DiagramIndex.NodeDecorator decorator;
	private final NodeCommon node;


	private final Stroke borderStroke;
	private final Stroke flagStroke;
	private final Stroke haloStroke;
	private final Stroke attachmentStroke;

	private final Color backgroundColor;
	private final Color borderColor;
	private final Color textColor;
	private final Color flagColor;
	private final Color haloColor;
	private final Color crossColor;
	private final Color attachmentsFill;
	private final Color attachmentsBorder;
	private final Color attachmentsText;
	private final Color foregroundColor;

	private final FillLayer bgLayer;
	private final FillLayer fgLayer;
	private final FillLayer analysisLayer;
	private final DrawLayer borderLayer;
	private final DrawLayer flagLayer;
	private final DrawLayer haloLayer;
	private final DrawLayer crossLayer;
	private final FillDrawLayer attachmentsLayer;
	private final TextLayer textLayer;

	private final Shape backgroundShape;
	private final Shape foregroundShape;
	private final Area backgroundArea;

	/**
	 * Creates a NodeRenderInfo with all the info about how to render a node.
	 *
	 * @param node     node
	 * @param index    the diagram index
	 * @param colors   color profile
	 * @param canvas   image canvas
	 * @param renderer associated renderer
	 */
	public NodeRenderInfo(NodeCommon node, DiagramIndex index, ColorProfiles colors, DiagramCanvas canvas, NodeAbstractRenderer renderer) {
		this.decorator = index.getNodeDecorator(node.getId());
		this.foregroundShape = renderer.foregroundShape(node);
		this.backgroundShape = renderer.backgroundShape(node);
		this.node = node;
		this.foregroundColor = renderer.getForegroundFill(colors, index);

		final DiagramSheet diagramSheet = colors.getDiagramSheet();
		flagLayer = canvas.getFlags();
		haloLayer = canvas.getHalo();

		crossed = node.getIsCrossed() != null && node.getIsCrossed();
		final NodeColorSheet profile = getNodeColorSheet(node.getRenderableClass(), diagramSheet);

		backgroundArea = backgroundShape == null ? null : new Area(backgroundShape);

		boolean disease = node.getIsDisease() != null && node.getIsDisease();
		boolean dashed = node.getNeedDashedBorder() != null && node.getNeedDashedBorder();
		boolean fadeOut = node.getIsFadeOut() != null && node.getIsFadeOut();

		borderStroke = decorator.isFlag()
				? StrokeProperties.StrokeStyle.SELECTION.getStroke(dashed)
				: StrokeProperties.StrokeStyle.BORDER.getStroke(dashed);

		if (fadeOut) {
			bgLayer = canvas.getFadeOutNodeBackground();
			fgLayer = canvas.getFadeOutNodeForeground();
			borderLayer = canvas.getFadeOutNodeBorder();
			textLayer = canvas.getFadeOutText();
			attachmentsLayer = canvas.getFadeOutAttachments();

			backgroundColor = profile.getFadeOutFill();
			attachmentsFill = profile.getFadeOutFill();
			borderColor = profile.getFadeOutStroke();
			textColor = profile.getFadeOutText();
		} else {
			bgLayer = canvas.getNodeBackground();
			fgLayer = canvas.getNodeForeground();
			borderLayer = canvas.getNodeBorder();
			textLayer = canvas.getText();
			attachmentsLayer = canvas.getAttachments();
			if (index.getAnalysisType() == AnalysisType.NONE) {
//				attachmentsFill = colors.getDiagramSheet().getAttachment().getFill();
				textColor = profile.getText();
				backgroundColor = profile.getFill();
				attachmentsFill = profile.getFill();

				if (decorator.isSelected()) {
					borderColor = diagramSheet.getProperties().getSelection();
				} else if (disease) {
					borderColor = diagramSheet.getProperties().getDisease();
				} else {
					borderColor = profile.getStroke();
				}
			} else {
				backgroundColor = profile.getLighterFill();
				textColor = profile.getLighterText();

				if (index.getAnalysisType() == AnalysisType.OVERREPRESENTATION
						&& decorator.getEnrichment() > 0)
					attachmentsFill = colors.getAnalysisSheet().getEnrichment().getGradient().getMax();
				else attachmentsFill = backgroundColor;

				if (decorator.isSelected()) {
					borderColor = diagramSheet.getProperties().getSelection();
				} else if (disease) {
					borderColor = diagramSheet.getProperties().getDisease();
				} else {
					borderColor = profile.getLighterStroke();
				}
			}
		}

		flagColor = diagramSheet.getProperties().getFlag();
		flagStroke = StrokeProperties.StrokeStyle.FLAG.getStroke(dashed);

		haloColor = diagramSheet.getProperties().getHalo();
		haloStroke = StrokeProperties.StrokeStyle.HALO.getStroke(dashed);

		attachmentStroke = StrokeProperties.StrokeStyle.SEGMENT.getStroke(false);
		attachmentsBorder = borderColor;
		attachmentsText = textColor;

		crossColor = diagramSheet.getProperties().getDisease();
		crossLayer = canvas.getCross();

		analysisLayer = canvas.getNodeAnalysis();
	}

	public TextLayer getTextLayer() {
		return textLayer;
	}

	public DrawLayer getBorderLayer() {
		return borderLayer;
	}

	public FillLayer getBgLayer() {
		return bgLayer;
	}

	public FillLayer getFgLayer() {
		return fgLayer;
	}

	public boolean isCrossed() {
		return crossed;
	}

	public Stroke getBorderStroke() {
		return borderStroke;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public Color getBorderColor() {
		return borderColor;
	}

	public Color getTextColor() {
		return textColor;
	}

	public Color getFlagColor() {
		return flagColor;
	}

	public Stroke getFlagStroke() {
		return flagStroke;
	}

	/** fixed shape of background */
	public Shape getBackgroundShape() {
		return backgroundShape;
	}

	/** modifiable shape of background. subtract here overlay shapes */
	public Area getBackgroundArea() {
		return backgroundArea;
	}

	public Stroke getHaloStroke() {
		return haloStroke;
	}

	public Color getHaloColor() {
		return haloColor;
	}

	public Shape getForegroundShape() {
		return foregroundShape;
	}

	public DiagramIndex.NodeDecorator getDecorator() {
		return decorator;
	}

	public DrawLayer getFlagLayer() {
		return flagLayer;
	}

	public DrawLayer getHaloLayer() {
		return haloLayer;
	}

	public Color getForegroundColor() {
		return foregroundColor;
	}

	public DrawLayer getCrossLayer() {
		return crossLayer;
	}

	public Color getCrossColor() {
		return crossColor;
	}

	public NodeCommon getNode() {
		return node;
	}

	public FillDrawLayer getAttachmentsLayer() {
		return attachmentsLayer;
	}

	public Color getAttachmentsFill() {
		return attachmentsFill;
	}

	public Color getAttachmentsBorder() {
		return attachmentsBorder;
	}

	public Color getAttachmentsText() {
		return attachmentsText;
	}

	public Stroke getAttachmentStroke() {
		return attachmentStroke;
	}

	public FillLayer getAnalysisLayer() {
		return analysisLayer;
	}
}
