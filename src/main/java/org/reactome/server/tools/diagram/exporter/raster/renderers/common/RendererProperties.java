package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

public class RendererProperties {

	public static double ARROW_ANGLE = Math.PI / 6;
	public static double ARROW_LENGTH;
	public static double[] DASHED_LINE_PATTERN;
	public static double EDGE_TYPE_WIDGET_WIDTH;
	public static double ROUND_RECT_ARC_WIDTH;
	public static double COMPLEX_RECT_ARC_WIDTH;
	public static double RNA_LOOP_WIDTH;
	public static double GENE_SYMBOL_PAD;
	public static double GENE_SYMBOL_WIDTH;
	public static double WIDGET_FONT_SIZE;
	public static double MAX_WIDGET_FONT_SIZE = 19;
	public static double NOTE_FONT_SIZE;
	public static double SEPARATION;
	public static double PROCESS_NODE_INSET_WIDTH;
	public static double NODE_TEXT_PADDING;
	public static double NODE_LINE_WIDTH;
	public static double INTERACTOR_FONT_SIZE;

	static {
		setFactor(1.0);
	}

	public static void setFactor(double factor) {
		ARROW_LENGTH = 8 * factor;
		DASHED_LINE_PATTERN = new double[]{5.0d * factor, 5.0d * factor};
		EDGE_TYPE_WIDGET_WIDTH = 12 * factor;
		ROUND_RECT_ARC_WIDTH = 5 * factor;
		COMPLEX_RECT_ARC_WIDTH = 3 * factor;
		RNA_LOOP_WIDTH = 16 * factor;
		GENE_SYMBOL_PAD = 4 * factor;
		GENE_SYMBOL_WIDTH = 50 * factor;
		SEPARATION = 2 * factor;
		PROCESS_NODE_INSET_WIDTH = 10 * factor;
		NODE_TEXT_PADDING = 5 * factor;
		WIDGET_FONT_SIZE = 9 * factor;
		if (WIDGET_FONT_SIZE > MAX_WIDGET_FONT_SIZE) {
			WIDGET_FONT_SIZE = MAX_WIDGET_FONT_SIZE;
		}
		NOTE_FONT_SIZE = 10 * factor;
		NODE_LINE_WIDTH = 2 * factor;
		INTERACTOR_FONT_SIZE = 6.33 * factor;
	}

}
