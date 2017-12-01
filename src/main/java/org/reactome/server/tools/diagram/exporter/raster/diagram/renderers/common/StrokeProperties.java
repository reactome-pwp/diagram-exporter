package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common;

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
	private static final float SELECTION_WIDTH = 3;
	private static final float HALO_WIDTH = 5;
	private static final float FLAG_WIDTH = 8;

	private static final float DASH_SIZE = 5;
	private static final float DASH_SPACE = 5;

	public enum StrokeStyle {
		FLAG {
			@Override
			protected float getWidth() {
				return FLAG_WIDTH;
			}
		}, HALO {
			@Override
			protected float getWidth() {
				return HALO_WIDTH;
			}
		}, SELECTION {
			@Override
			protected float getWidth() {
				return SELECTION_WIDTH;
			}
		}, SEGMENT {
			@Override
			protected float getWidth() {
				return SEGMENT_WIDTH;
			}
		}, BORDER {
			@Override
			protected float getWidth() {
				return BORDER_WIDTH;
			}
		};
		private final Stroke NORMAL = new BasicStroke(getWidth(), END, JOIN);
		private final Stroke DASHED = new BasicStroke(getWidth(), END, JOIN,
				DASH_SIZE, new float[]{DASH_SIZE, DASH_SPACE}, DASH_SIZE);

		protected abstract float getWidth();

		public final Stroke getStroke(boolean dashed) {
			return dashed ? DASHED : NORMAL;
		}
	}
}

