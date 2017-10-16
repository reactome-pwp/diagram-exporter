package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfileNode;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.DrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.FillLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.TextLayer;

import java.awt.*;
import java.awt.geom.Area;

import static org.reactome.server.tools.diagram.exporter.raster.renderers.common.StrokeProperties.get;

public class NodeRenderInfo extends DiagramObjectInfo {

	private final boolean disease;
	private final boolean crossed;
	private final boolean dashed;
	private final boolean fadeOut;
	private final boolean flag;
	private final boolean selected;
	private final boolean halo;

	private final Area backgroundArea;
	private final DiagramProfileNode profile;
	private final NodeProperties prop;

	private final Stroke borderStroke;
	private final Stroke flagStroke;
	private final Stroke haloStroke;

	private final String backgroundColor;
	private final String borderColor;
	private final String textColor;
	private final String flagColor;
	private final String haloColor;

	private final FillLayer bgLayer;
	private final FillLayer fgLayer;
	private final DrawLayer borderLayer;
	private final TextLayer textLayer;
	private Shape backgroundShape;
	private Shape foregroundShape;

	public NodeRenderInfo(NodeCommon node, DiagramIndex index, DiagramProfile diagramProfile, DiagramCanvas canvas, Shape background, Shape foreground) {
		disease = node.getIsDisease() != null && node.getIsDisease();
		crossed = node.getIsCrossed() != null && node.getIsCrossed();
		dashed = node.getNeedDashedBorder() != null && node.getNeedDashedBorder();
		fadeOut = node.getIsFadeOut() != null && node.getIsFadeOut();
		flag = index.getFlags().contains(node.getId());
		selected = index.getSelected().contains(node.getId());
		halo = index.getHaloed().contains(node.getId());
		// An area can be clip
		this.backgroundShape = background;
		this.foregroundShape = foreground;
		backgroundArea = background == null ? null : new Area(background);
		profile = getDiagramProfileNode(node.getRenderableClass(), diagramProfile);
		prop = node.getProp();

		borderStroke = selected
				? StrokeProperties.StrokeStyle.SELECTION.getStroke(dashed)
				: StrokeProperties.StrokeStyle.BORDER.getStroke(dashed);


		if (fadeOut) {
			backgroundColor = profile.getFadeOutFill();
			borderColor = profile.getFadeOutStroke();
			textColor = profile.getFadeOutText();
		} else {
			if (index.getAnalysisType() == AnalysisType.NONE) {
				backgroundColor = profile.getFill();
				borderColor = selected
						? diagramProfile.getProperties().getSelection()
						: disease
						? diagramProfile.getProperties().getDisease()
						: profile.getStroke();
				textColor = profile.getText();
			} else {
				backgroundColor = profile.getLighterFill();
				borderColor = selected
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
		flagColor = diagramProfile.getProperties().getFlag();
		flagStroke = get(StrokeProperties.StrokeStyle.FLAG, dashed);
		haloColor = diagramProfile.getProperties().getHalo();
		haloStroke = StrokeProperties.StrokeStyle.HALO.getStroke(dashed);

	}

	public TextLayer getTextLayer() {
		return textLayer;
	}

	public DiagramProfileNode getProfile() {
		return profile;
	}

	public Area getBackgroundArea() {
		return backgroundArea;
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

	public boolean isDisease() {
		return disease;
	}

	public boolean isCrossed() {
		return crossed;
	}

	public boolean isDashed() {
		return dashed;
	}

	public boolean isFadeOut() {
		return fadeOut;
	}

	public boolean isFlag() {
		return flag;
	}

	public boolean isSelected() {
		return selected;
	}

	public boolean isHalo() {
		return halo;
	}

	public NodeProperties getProp() {
		return prop;
	}

	public Stroke getBorderStroke() {
		return borderStroke;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public String getBorderColor() {
		return borderColor;
	}

	public String getTextColor() {
		return textColor;
	}

	public String getFlagColor() {
		return flagColor;
	}

	public Stroke getFlagStroke() {
		return flagStroke;
	}

	public Shape getBackgroundShape() {
		return backgroundShape;
	}

	public Stroke getHaloStroke() {
		return haloStroke;
	}

	public String getHaloColor() {
		return haloColor;
	}

	public Shape getForegroundShape() {
		return foregroundShape;
	}
}
