package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

/**
 * Any renderer should accept a list of DiagramObjects and be able to render
 * them with one pass (segments, fill, border and text).
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public interface Renderer {
	/**
	 * Renders an edge and the connectors associated to it.
	 *
	 * @param canvas where to render
	 * @param item   the edge to render
	 * @param index  the diagram index
	 * @param t      expression column
	 */
	void draw(DiagramCanvas canvas, DiagramObject item, ColorProfiles colorProfiles, DiagramIndex index, int t);
}
