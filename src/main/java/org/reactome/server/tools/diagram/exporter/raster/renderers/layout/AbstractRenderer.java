package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

/**
 *
 *
 */
public abstract class AbstractRenderer implements Renderer {

	/**
	 * Renders a specific node. This method must be overridden by subclasses.
	 *
	 * @param item item to render
	 */
	public void drawBorder(AdvancedGraphics2D graphics, DiagramObject item) {
	}

	/**
	 * Renders the text of a specific node
	 *
	 * @param item item whose text to render
	 */
	@Override
	public void drawText(AdvancedGraphics2D graphics, DiagramObject item) {
	}

	@Override
	public void drawEnrichments(AdvancedGraphics2D graphics, DiagramObject item) {

	}

	@Override
	public void drawExpression(AdvancedGraphics2D graphics, DiagramObject item) {

	}

	@Override
	public void drawHitInteractors(AdvancedGraphics2D graphics, DiagramObject item) {

	}

	@Override
	public void fill(AdvancedGraphics2D graphics, DiagramObject item) {

	}
}
