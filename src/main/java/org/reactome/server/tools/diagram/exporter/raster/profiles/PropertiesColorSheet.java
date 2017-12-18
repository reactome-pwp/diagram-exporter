package org.reactome.server.tools.diagram.exporter.raster.profiles;

import java.awt.*;

public class PropertiesColorSheet {
	private Color halo;
	private Color flag;
	private Color disease;
	private Color selection;
	private Color hovering;
	private Color highlight;
	private Color text;
	private Color button;
	private Color trigger;

	
	public Color getHalo() {
		return halo;
	}

	
	public Color getFlag() {
		return flag;
	}

	
	public Color getDisease() {
		return disease;
	}

	
	public Color getSelection() {
		return selection;
	}

	
	public Color getHovering() {
		return hovering;
	}

	
	public Color getHighlight() {
		return highlight;
	}

	
	public Color getText() {
		return text;
	}

	
	public Color getButton() {
		return button;
	}

	
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
