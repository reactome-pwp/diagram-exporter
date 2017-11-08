package org.reactome.server.tools.diagram.exporter.raster.profiles;

import java.awt.*;

/**
 * Color profile for Analysis. Matches a JSON with analysis structure:
 * enrichment and expression.
 */
public interface AnalysisSheet extends ColorSheet {

	/**
	 * Color profile for expressions
	 */
	ExpressionSheet getExpression();

	/**
	 * Color profile for enrichment
	 */
	EnrichmentSheet getEnrichment();

	/**
	 * Color for ribbons
	 */
	Color getRibbon();

}
