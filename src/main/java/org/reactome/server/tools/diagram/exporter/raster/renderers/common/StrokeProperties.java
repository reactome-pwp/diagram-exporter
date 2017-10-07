package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import java.awt.*;

public class StrokeProperties {

	public static Stroke DASHED_BORDER_STROKE;
	public static Stroke DASHED_LINE_STROKE;
	public static Stroke HALO_STROKE;
	public static Stroke SEGMENT_STROKE;
	public static Stroke BORDER_STROKE;
	public static Stroke SELECTION_STROKE;
	public static Stroke DASHED_SELECTION_STROKE;
	public static Stroke DASHED_HALO_STROKE;

	public static void setFactor(double factor) {
		final int end = BasicStroke.CAP_SQUARE;
		final int join = BasicStroke.JOIN_ROUND;
		final float segmentWidth = (float) (1 * factor);
		final float borderWidth = (float) (2 * factor);
		final float selectionWidth = (float) (3 * factor);
		final float haloWidth = (float) (5 * factor);
		final float dashSize = (float) (factor * 5);
		final float dashSpace = (float) (factor * 2);

		SEGMENT_STROKE = new BasicStroke(segmentWidth, end, join);
		BORDER_STROKE = new BasicStroke(borderWidth, end, join);
		SELECTION_STROKE = new BasicStroke(selectionWidth, end, join);
		HALO_STROKE = new BasicStroke(haloWidth, end, join);

		DASHED_LINE_STROKE = new BasicStroke(segmentWidth, end, join, dashSize,
				new float[]{dashSize}, dashSize);
		DASHED_BORDER_STROKE = new BasicStroke(borderWidth, end, join, dashSize,
				new float[]{dashSize, dashSpace}, dashSize);
		DASHED_SELECTION_STROKE = new BasicStroke(selectionWidth, end, join, dashSize,
				new float[]{dashSize, dashSpace}, dashSize);
		DASHED_HALO_STROKE = new BasicStroke(haloWidth, end, join, dashSize,
				new float[]{dashSize, dashSpace}, dashSize);


	}

}
