package org.reactome.server.tools.diagram.exporter.raster.profiles;


import java.awt.*;

public class EnrichmentSheet {

	private GradientSheet gradient;
	private Color text;

	public Color getText() {
		return text;
	}

	public GradientSheet getGradient() {
		return gradient;
	}

	public void setText(String color) {
		this.text = ColorFactory.parseColor(color);
	}

}
