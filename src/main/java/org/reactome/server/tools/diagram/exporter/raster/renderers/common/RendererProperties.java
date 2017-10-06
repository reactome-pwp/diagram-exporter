package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

/**
 * General Properties for the RasterRenderer class and all of the Node
 * renderers.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class RendererProperties {

	static double ROUND_RECT_ARC_WIDTH;
	public static double COMPLEX_RECT_ARC_WIDTH;
	public static double RNA_LOOP_WIDTH;
	public static double GENE_SYMBOL_WIDTH;
	public static double SEPARATION;
	public static double PROCESS_NODE_INSET_WIDTH;
	public static double NODE_TEXT_PADDING;
	static double ARROW_LENGTH;
	static double GENE_SYMBOL_PAD;

	static {
		setFactor(1.0);
	}

	public static void setFactor(double factor) {
		ARROW_LENGTH = 8 * factor;
		ROUND_RECT_ARC_WIDTH = 5 * factor;
		COMPLEX_RECT_ARC_WIDTH = 3 * factor;
		RNA_LOOP_WIDTH = 16 * factor;
		GENE_SYMBOL_PAD = 4 * factor;
		GENE_SYMBOL_WIDTH = 50 * factor;
		SEPARATION = 4 * factor;
		PROCESS_NODE_INSET_WIDTH = 10 * factor;
		NODE_TEXT_PADDING = 5 * factor;
	}

}
