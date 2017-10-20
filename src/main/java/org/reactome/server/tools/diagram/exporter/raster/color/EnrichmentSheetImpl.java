package org.reactome.server.tools.diagram.exporter.raster.color;


import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;

import java.awt.*;

public class EnrichmentSheetImpl implements EnrichmentSheet {

	private GradientSheetImpl gradient;
	private Color text;

	@Override
	public Color getText() {
		return text;
	}

	@Override
	public GradientSheet getGradient() {
		return gradient;
	}

	public void setText(String color) {
		this.text = ColorFactory.parseColor(color);
	}

}
