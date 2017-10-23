package org.reactome.server.tools.diagram.exporter.raster.color;

import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;

import java.awt.*;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
class NodeColorSheetImpl implements NodeColorSheet {

	private Color fill;
	private Color fadeOutFill;
	private Color lighterFill;
	private Color stroke;
	private Color fadeOutStroke;
	private Color lighterStroke;
	private Color text;
	private Color fadeOutText;
	private Color lighterText;

	private Double lineWidth;

	@Override
	public Color getFill() {
		return fill;
	}

	public void setFill(String color) {
		this.fill = ColorFactory.parseColor(color);
	}

	@Override
	public Color getFadeOutFill() {
		return fadeOutFill;
	}

	public void setFadeOutFill(String color) {
		this.fadeOutFill = ColorFactory.parseColor(color);
	}

	@Override
	public Color getLighterFill() {
		return lighterFill;
	}

	public void setLighterFill(String color) {
		this.lighterFill = ColorFactory.parseColor(color);
	}

	@Override
	public Color getStroke() {
		return stroke;
	}

	public void setStroke(String color) {
		this.stroke = ColorFactory.parseColor(color);
	}

	@Override
	public Color getFadeOutStroke() {
		return fadeOutStroke;
	}

	public void setFadeOutStroke(String color) {
		this.fadeOutStroke = ColorFactory.parseColor(color);
	}

	@Override
	public Color getLighterStroke() {
		return lighterStroke;
	}

	public void setLighterStroke(String color) {
		this.lighterStroke = ColorFactory.parseColor(color);
	}

	@Override
	public Color getText() {
		return text;
	}

	public void setText(String color) {
		this.text = ColorFactory.parseColor(color);
	}

	@Override
	public Color getFadeOutText() {
		return fadeOutText;
	}

	public void setFadeOutText(String color) {
		this.fadeOutText = ColorFactory.parseColor(color);
	}

	@Override
	public Color getLighterText() {
		return lighterText;
	}

	public void setLighterText(String color) {
		this.lighterText = ColorFactory.parseColor(color);
	}

	@Override
	public Double getLineWidth() {
		return lineWidth;
	}


}