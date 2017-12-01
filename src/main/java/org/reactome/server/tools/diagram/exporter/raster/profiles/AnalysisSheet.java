package org.reactome.server.tools.diagram.exporter.raster.profiles;

import java.awt.*;

/**
 * Implementation of AnalysisSheet tested with JacksonXML.
 */
@SuppressWarnings("unused")
public class AnalysisSheet extends ColorSheet {
	private ExpressionSheet expression;
	private EnrichmentSheet enrichment;
	private Color ribbon;

	public ExpressionSheet getExpression() {
		return expression;
	}

	public EnrichmentSheet getEnrichment() {
		return enrichment;
	}

	public Color getRibbon() {
		return ribbon;
	}

	public void setRibbon(String color) {
		this.ribbon = ColorFactory.parseColor(color);
	}

}
