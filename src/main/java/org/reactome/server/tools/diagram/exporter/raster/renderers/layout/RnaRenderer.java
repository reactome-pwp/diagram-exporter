package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

public class RnaRenderer extends NodeAbstractRenderer {

	@Override
	public void drawBorder(AdvancedGraphics2D graphics, DiagramObject item) {
		graphics.drawBone(((Node) item).getProp());

	}

	@Override
	public void fill(AdvancedGraphics2D graphics, DiagramObject item) {
		graphics.fillBone(((Node) item).getProp());
	}

}
