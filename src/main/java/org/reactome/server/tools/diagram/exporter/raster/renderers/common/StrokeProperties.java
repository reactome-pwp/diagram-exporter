package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import java.awt.*;

/**
 * Choose among 5 different stroke widths.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class StrokeProperties {

	private static final int END = BasicStroke.CAP_BUTT;
	private static final int JOIN = BasicStroke.JOIN_MITER;

	private static final float SEGMENT_WIDTH = 1;
	private static final float BORDER_WIDTH = 2;
	private static final float SELECTION_WIDTH = 2;
	private static final float HALO_WIDTH = 5;
	private static final float FLAG_WIDTH = 6;

	private static final float DASH_SIZE = 5;
	private static final float DASH_SPACE = 5;

	private static Stroke SEGMENT_STROKE = new BasicStroke(SEGMENT_WIDTH, END, JOIN);
	private static Stroke BORDER_STROKE = new BasicStroke(BORDER_WIDTH, END, JOIN);
	private static Stroke SELECTION_STROKE = new BasicStroke(SELECTION_WIDTH, END, JOIN);
	private static Stroke HALO_STROKE = new BasicStroke(HALO_WIDTH, END, JOIN);
	private static Stroke FLAG_STROKE = new BasicStroke(FLAG_WIDTH, END, JOIN);

	private static Stroke DASHED_SEGMENT_STROKE = new BasicStroke(SEGMENT_WIDTH, END, JOIN, DASH_SIZE, new float[]{DASH_SIZE}, DASH_SIZE);
	private static Stroke DASHED_BORDER_STROKE = new BasicStroke(BORDER_WIDTH, END, JOIN, DASH_SIZE, new float[]{DASH_SIZE, DASH_SPACE}, DASH_SIZE);
	private static Stroke DASHED_SELECTION_STROKE = new BasicStroke(SELECTION_WIDTH, END, JOIN, DASH_SIZE, new float[]{DASH_SIZE, DASH_SPACE}, DASH_SIZE);
	private static Stroke DASHED_HALO_STROKE = new BasicStroke(HALO_WIDTH, END, JOIN, DASH_SIZE, new float[]{DASH_SIZE, DASH_SPACE}, DASH_SIZE);
	private static Stroke DASHED_FLAG_STROKE = new BasicStroke(FLAG_WIDTH, END, JOIN, DASH_SIZE, new float[]{DASH_SIZE, DASH_SPACE}, DASH_SIZE);

	public enum StrokeStyle {
		FLAG {
			@Override
			public Stroke getStroke(boolean dashed) {
				return dashed ? DASHED_FLAG_STROKE : FLAG_STROKE;
			}
		}, HALO {
			@Override
			public Stroke getStroke(boolean dashed) {
				return dashed ? DASHED_HALO_STROKE : HALO_STROKE;
			}
		}, SELECTION {
			@Override
			public Stroke getStroke(boolean dashed) {
				return dashed ? DASHED_SELECTION_STROKE : SELECTION_STROKE;
			}
		}, SEGMENT {
			@Override
			public Stroke getStroke(boolean dashed) {
				return dashed ? DASHED_SEGMENT_STROKE : SEGMENT_STROKE;
			}
		}, BORDER {
			@Override
			public Stroke getStroke(boolean dashed) {
				return dashed ? DASHED_BORDER_STROKE : BORDER_STROKE;
			}
		};

		public abstract Stroke getStroke(boolean dashed);
	}
}

