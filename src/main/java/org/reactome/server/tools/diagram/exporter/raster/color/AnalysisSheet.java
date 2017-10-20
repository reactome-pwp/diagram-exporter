package org.reactome.server.tools.diagram.exporter.raster.color;

import java.awt.*;

public interface AnalysisSheet extends ColorSheet {

	ExpressionSheet getExpression();

	EnrichmentSheet getEnrichment();

	Color getRibbon();

}
