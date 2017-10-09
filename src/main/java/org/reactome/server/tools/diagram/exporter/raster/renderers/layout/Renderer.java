package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
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

	void cross(AdvancedGraphics2D graphics, Collection<Node> nodes);

	void fill(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items);

	void border(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items);

	void text(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items);

	void segments(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items);
}
