package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

import java.util.Collection;

/**
 * empty renderer. Drawing with it will no have effects in the graphics.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public abstract class AbstractRenderer implements Renderer {


	@Override
	public void cross(AdvancedGraphics2D graphics, Collection<Node> nodes) {

	}

	@Override
	public void fill(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {

	}

	@Override
	public void border(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {

	}

	@Override
	public void text(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {

	}

	@Override
	public void segments(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {

	}

	@Override
	public void highlight(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {

	}
}
