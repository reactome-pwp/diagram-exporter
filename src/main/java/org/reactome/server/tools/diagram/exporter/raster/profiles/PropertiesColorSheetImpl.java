package org.reactome.server.tools.diagram.exporter.raster.profiles;

import java.awt.*;

public class PropertiesColorSheetImpl implements PropertiesColorSheet{
	private Color halo;
	private Color flag;
	private Color disease;
	private Color selection;
	private Color hovering;
	private Color highlight;
	private Color text;
	private Color button;
	private Color trigger;

	@Override
	public Color getHalo() {
		return halo;
	}

	@Override
	public Color getFlag() {
		return flag;
	}

	@Override
	public Color getDisease() {
		return disease;
	}

	@Override
	public Color getSelection() {
		return selection;
	}

	@Override
	public Color getHovering() {
		return hovering;
	}

	@Override
	public Color getHighlight() {
		return highlight;
	}

	@Override
	public Color getText() {
		return text;
	}

	@Override
	public Color getButton() {
		return button;
	}

	@Override
	public Color getTrigger() {
		return trigger;
	}

	public void setHalo(String color) {
		this.halo = ColorFactory.parseColor(color);
	}

	public void setFlag(String color) {
		this.flag = ColorFactory.parseColor(color);
	}

	public void setDisease(String color) {
		this.disease = ColorFactory.parseColor(color);
	}

	public void setSelection(String color) {
		this.selection = ColorFactory.parseColor(color);
	}

	public void setHovering(String color) {
		this.hovering = ColorFactory.parseColor(color);
	}

	public void setHighlight(String color) {
		this.highlight = ColorFactory.parseColor(color);
	}

	public void setText(String color) {
		this.text = ColorFactory.parseColor(color);
	}

	public void setButton(String color) {
		this.button = ColorFactory.parseColor(color);
	}

	public void setTrigger(String color) {
		this.trigger = ColorFactory.parseColor(color);
	}

}
