package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

/**
 * General Properties for the RasterRenderer class and all of the Node
 * renderers.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class RendererProperties {

	public static double COMPLEX_RECT_ARC_WIDTH;
	public static double RNA_LOOP_WIDTH;
	public static double GENE_SYMBOL_WIDTH;
	public static double SEPARATION;
	public static double PROCESS_NODE_INSET_WIDTH;
	public static double NODE_TEXT_PADDING;
	static double ROUND_RECT_ARC_WIDTH;
	static double ARROW_LENGTH;
	static double GENE_SYMBOL_PAD;

	static {
		setFactor(1.0);
	}

	public static void setFactor(double factor) {
		ARROW_LENGTH = Math.max(1, 8 * factor);
		ROUND_RECT_ARC_WIDTH = Math.max(1, 6 * factor);
		COMPLEX_RECT_ARC_WIDTH = Math.max(1, 5 * factor);
		RNA_LOOP_WIDTH = Math.max(1, 16 * factor);
		GENE_SYMBOL_PAD = Math.max(1, 4 * factor);
		GENE_SYMBOL_WIDTH = Math.max(1, 50 * factor);
		SEPARATION = Math.max(1, 4 * factor);
		PROCESS_NODE_INSET_WIDTH = Math.max(1, 10 * factor);
		NODE_TEXT_PADDING = Math.max(1, 5 * factor);
	}

}
