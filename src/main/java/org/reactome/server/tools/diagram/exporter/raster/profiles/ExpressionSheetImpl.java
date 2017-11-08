package org.reactome.server.tools.diagram.exporter.raster.profiles;

public class ExpressionSheetImpl extends EnrichmentSheetImpl implements ExpressionSheet {
	private LegendSheetImpl legend;

	@Override
	public LegendSheet getLegend() {
		return legend;
	}
}
