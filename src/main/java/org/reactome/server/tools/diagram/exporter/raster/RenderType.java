package org.reactome.server.tools.diagram.exporter.raster;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public enum RenderType {
	FADE_OUT,

	NORMAL,
	DISEASE,

	HIT_INTERACTORS,

	NOT_HIT_BY_ANALYSIS_NORMAL,
	NOT_HIT_BY_ANALYSIS_DISEASE,

	HIT_BY_ENRICHMENT_NORMAL,
	HIT_BY_ENRICHMENT_DISEASE,

	HIT_BY_EXPRESSION_NORMAL,
	HIT_BY_EXPRESSION_DISEASE
}
