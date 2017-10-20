package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.exporter.raster.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;

/**
 * Any renderer should accept a list of DiagramObjects and be able to render
 * them with one pass (segments, fill, border and text).
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public interface Renderer {

	void draw(DiagramCanvas canvas, DiagramObject item, ColorProfiles colorProfiles, DiagramIndex index);
}
