package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

import java.awt.*;
import java.util.Collection;

/**
 * Any renderer should accept a list of DiagramObjects and be able to render
 * them with one pass (segments, fill, border and text).
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public interface Renderer {

	/**
	 * Render the list of DiagramObjects. They all have to share the same fill
	 * color, the same border color and the same text color.
	 *
	 * @param graphics     where to render
	 * @param items        list of DiagramObjects
	 * @param fillColor    color for filling
	 * @param lineColor    color for borders and segments
	 * @param textColor    color for texts
	 * @param borderStroke stroke for borders
	 */
	void draw(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items, Paint fillColor, Paint lineColor, Paint textColor, Stroke borderStroke);
}
