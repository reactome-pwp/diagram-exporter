package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import java.awt.*;

public class StrokeProperties {

	public static Stroke DASHED_BORDER_STROKE;
	public static Stroke DASHED_SEGMENT_STROKE;
	public static Stroke DASHED_SELECTION_STROKE;
	public static Stroke DASHED_HALO_STROKE;
	public static Stroke BORDER_STROKE;
	public static Stroke SEGMENT_STROKE;
	public static Stroke SELECTION_STROKE;
	public static Stroke HALO_STROKE;

	public static void setFactor(double factor) {
		final int end = BasicStroke.CAP_BUTT;
		final int join = BasicStroke.JOIN_MITER;
		final float segmentWidth = (float) (Math.max(1, 1 * factor));
		final float borderWidth = (float) (Math.max(1, 2 * factor));
		final float selectionWidth = (float) (Math.max(1,3 * factor));
		final float haloWidth = (float) (Math.max(1, 5 * factor));
		final float dashSize = (float) (Math.max(1, 5 * factor));
		final float dashSpace = (float) (Math.max(1, 5 * factor));

		SEGMENT_STROKE = new BasicStroke(segmentWidth, end, join);
		BORDER_STROKE = new BasicStroke(borderWidth, end, join);
		SELECTION_STROKE = new BasicStroke(selectionWidth, end, join);
		HALO_STROKE = new BasicStroke(haloWidth, end, join);

		DASHED_SEGMENT_STROKE = new BasicStroke(segmentWidth, end, join, dashSize,
				new float[]{dashSize}, dashSize);
		DASHED_BORDER_STROKE = new BasicStroke(borderWidth, end, join, dashSize,
				new float[]{dashSize, dashSpace}, dashSize);
		DASHED_SELECTION_STROKE = new BasicStroke(selectionWidth, end, join, dashSize,
				new float[]{dashSize, dashSpace}, dashSize);
		DASHED_HALO_STROKE = new BasicStroke(haloWidth, end, join, dashSize,
				new float[]{dashSize, dashSpace}, dashSize);


	}

}
