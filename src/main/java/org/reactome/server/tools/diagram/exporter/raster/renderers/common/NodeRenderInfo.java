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

import java.awt.*;
import java.awt.geom.Area;

import static org.reactome.server.tools.diagram.exporter.raster.renderers.common.StrokeProperties.get;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class NodeRenderInfo extends DiagramObjectInfo {

	private final boolean crossed;

	private final Area backgroundArea;
	private final NodeColorSheet profile;

	private final Stroke borderStroke;
	private final Stroke flagStroke;
	private final Stroke haloStroke;

	private final Color backgroundColor;
	private final Color borderColor;
	private final Color textColor;
	private final Color flagColor;
	private final Color haloColor;

	private final FillLayer bgLayer;
	private final FillLayer fgLayer;
	private final DrawLayer borderLayer;
	private final TextLayer textLayer;
	private final DiagramIndex.NodeDecorator decorator;
	private final NodeCommon node;
	private Shape backgroundShape;
	private Shape foregroundShape;
	private DrawLayer flagLayer;
	private DrawLayer haloLayer;
	private Color foregroundColor;
	private double textPadding;
	private double textSplit;
	private DrawLayer crossLayer;
	private Color crossColor;
	private FillDrawLayer attachmentsLayer;
	private Color attachmentsFill;
	private Color attachmentsBorder;
	private Color attachmentsText;
	private Stroke attachmentStroke;

	/**
	 * Creates a NodeRenderInfo with all the info about how to render a node.
	 *
	 * @param node           node
	 * @param index          the diagram index
	 * @param colors         color profile
	 * @param canvas         image canvas
	 * @param background     backgroundShape
	 * @param foreground     foregroundShape
	 * @param foregroundFill
	 */
	public NodeRenderInfo(NodeCommon node, DiagramIndex index, ColorProfiles colors, DiagramCanvas canvas, Shape background, Shape foreground, Color foregroundFill) {
		this.decorator = index.getNodeDecorator(node.getId());
		this.foregroundShape = foreground;
		this.backgroundShape = background;
		this.node = node;
		final DiagramSheet diagramSheet = colors.getDiagramSheet();
		flagLayer = canvas.getFlags();
		haloLayer = canvas.getHalo();
		textPadding = RendererProperties.NODE_TEXT_PADDING;
		textSplit = 0.0;

		this.foregroundColor = foregroundFill;

		crossed = node.getIsCrossed() != null && node.getIsCrossed();
		profile = getDiagramProfileNode(node.getRenderableClass(), diagramSheet);

		backgroundArea = background == null ? null : new Area(background);

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
			borderColor = profile.getFadeOutStroke();
			textColor = profile.getFadeOutText();

		} else {
			bgLayer = canvas.getNodeBackground();
			fgLayer = canvas.getNodeForeground();
			borderLayer = canvas.getNodeBorder();
			attachmentsLayer = canvas.getAttachments();
			textLayer = canvas.getText();
			if (index.getAnalysisType() == AnalysisType.NONE) {
				attachmentsFill = colors.getDiagramSheet().getAttachment().getFill();
				attachmentsText = colors.getDiagramSheet().getAttachment().getText();
				textColor = profile.getText();
				backgroundColor = profile.getFill();

				if (decorator.isSelected()) {
					borderColor = diagramSheet.getProperties().getSelection();
					attachmentsBorder = diagramSheet.getProperties().getSelection();
				} else if (disease) {
					borderColor = diagramSheet.getProperties().getDisease();
					attachmentsBorder = diagramSheet.getProperties().getDisease();
				} else {
					borderColor = profile.getStroke();
					attachmentsBorder = colors.getDiagramSheet().getAttachment().getStroke();
				}
			} else {
				backgroundColor = profile.getLighterFill();
				attachmentsText = colors.getDiagramSheet().getAttachment().getLighterText();
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
		flagStroke = get(StrokeProperties.StrokeStyle.FLAG, dashed);
		haloColor = diagramSheet.getProperties().getHalo();
		haloStroke = StrokeProperties.StrokeStyle.HALO.getStroke(dashed);
		attachmentStroke = StrokeProperties.StrokeStyle.SEGMENT.getStroke(false);
		attachmentsBorder = borderColor;
		attachmentsText = textColor;
	}

	public TextLayer getTextLayer() {
		return textLayer;
	}

	public NodeColorSheet getProfile() {
		return profile;
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

	public double getTextPadding() {
		return textPadding;
	}

	public double getTextSplit() {
		return textSplit;
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
}
