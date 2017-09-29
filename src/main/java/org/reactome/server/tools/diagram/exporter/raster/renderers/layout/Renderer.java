package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

public interface Renderer {

	/**
	 * Draws shapes
	 *
	 * @param graphics where to draw
	 * @param item     what to draw
	 */
	void drawBorder(AdvancedGraphics2D graphics, DiagramObject item);

	/**
	 * Draws text
	 *
	 * @param graphics
	 * @param item
	 */
	void drawText(AdvancedGraphics2D graphics, DiagramObject item);

	void drawEnrichments(AdvancedGraphics2D graphics, DiagramObject item);

	void drawHitInteractors(AdvancedGraphics2D graphics, DiagramObject item);

	void drawExpression(AdvancedGraphics2D graphics, DiagramObject item);

	/**
	 * Fills shapes. It should be called always before the draw
	 *
	 * @param graphics
	 * @param item
	 */
	void fill(AdvancedGraphics2D graphics, DiagramObject item);
}
