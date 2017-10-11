package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import java.awt.*;

public class StrokeProperties {

	public static Stroke DASHED_BORDER_STROKE;
	public static Stroke DASHED_SEGMENT_STROKE;
	public static Stroke DASHED_SELECTION_STROKE;
	public static Stroke DASHED_HALO_STROKE;
	public static Stroke DASHED_FLAG_STROKE;

	public static Stroke BORDER_STROKE;
	public static Stroke SEGMENT_STROKE;
	public static Stroke SELECTION_STROKE;
	public static Stroke HALO_STROKE;
	public static Stroke FLAG_STROKE;

	public static void setFactor(double factor) {
		final int end = BasicStroke.CAP_BUTT;
		final int join = BasicStroke.JOIN_MITER;

		final float segmentWidth = scale(1, factor);
		final float borderWidth = scale(2, factor);
		final float selectionWidth = scale(3, factor);
		final float haloWidth = scale(5, factor);
		final float flagWidth = scale(6, factor);

		final float dashSize = scale(5, factor);
		final float dashSpace = scale(5, factor);

		SEGMENT_STROKE = new BasicStroke(segmentWidth, end, join);
		BORDER_STROKE = new BasicStroke(borderWidth, end, join);
		SELECTION_STROKE = new BasicStroke(selectionWidth, end, join);
		HALO_STROKE = new BasicStroke(haloWidth, end, join);
		FLAG_STROKE = new BasicStroke(flagWidth, end, join);

		DASHED_SEGMENT_STROKE = new BasicStroke(segmentWidth, end, join, dashSize,
				new float[]{dashSize}, dashSize);
		DASHED_BORDER_STROKE = new BasicStroke(borderWidth, end, join, dashSize,
				new float[]{dashSize, dashSpace}, dashSize);
		DASHED_SELECTION_STROKE = new BasicStroke(selectionWidth, end, join, dashSize,
				new float[]{dashSize, dashSpace}, dashSize);
		DASHED_HALO_STROKE = new BasicStroke(haloWidth, end, join, dashSize,
				new float[]{dashSize, dashSpace}, dashSize);
		DASHED_FLAG_STROKE = new BasicStroke(flagWidth, end, join, dashSize,
				new float[]{dashSize, dashSpace}, dashSize);


	}

	private static float scale(double val, double factor) {
		return (float) (Math.max(1, val * factor));
	}

}
