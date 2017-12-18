package org.reactome.server.tools.diagram.exporter.raster.diagram.common;

import java.awt.*;

/**
 * Choose among different stroke widths.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public enum StrokeStyle {
	/** width: 8 */
	FLAG {
		@Override
		protected float getWidth() {
			return 8;
		}
	},
	/** width: 5 */
	HALO {
		@Override
		protected float getWidth() {
			return 5;
		}
	},
	/** width: 3 */
	SELECTION {
		@Override
		protected float getWidth() {
			return 3;
		}
	},
	/** width: 1 */
	SEGMENT {
		@Override
		protected float getWidth() {
			return 1;
		}
	},
	/** width: 2 */
	BORDER {
		@Override
		protected float getWidth() {
			return 2;
		}
	};

	private final Stroke NORMAL = new BasicStroke(getWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	private final Stroke DASHED = new BasicStroke(getWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
			5f, new float[]{5f, 5f}, 5f);

	protected abstract float getWidth();

	public final Stroke get(boolean dashed) {
		return dashed ? DASHED : NORMAL;
	}
}
