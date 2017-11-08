package org.reactome.server.tools.diagram.exporter.raster.profiles;

import java.awt.*;

/**
 * Implementation of AnalysisSheet tested with JacksonXML.
 */
@SuppressWarnings("unused")
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

	public void setRibbon(String color) {
		this.ribbon = ColorFactory.parseColor(color);
	}

	@Override
	public String getName() {
		return name;
	}
}
