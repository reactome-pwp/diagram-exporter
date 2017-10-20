package org.reactome.server.tools.diagram.exporter.raster.color;

import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;

import java.awt.*;

public class AnalysisSheetImpl implements AnalysisSheet {
	private String name;
	private ExpressionSheetImpl expression;
	private EnrichmentSheetImpl enrichment;
	private Color ribbon;

	@Override
	public ExpressionSheet getExpression() {
		return expression;
	}

	@Override
	public EnrichmentSheet getEnrichment() {
		return enrichment;
	}

	@Override
	public Color getRibbon() {
		return ribbon;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setRibbon(String color) {
		this.ribbon = ColorFactory.parseColor(color);
	}
}
