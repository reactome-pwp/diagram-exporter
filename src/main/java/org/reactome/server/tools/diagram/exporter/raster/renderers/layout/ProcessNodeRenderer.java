package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

import java.awt.*;

public class ProcessNodeRenderer extends NodeAbstractRenderer {

	private static final double PADDING = 10;

	private static final Paint INNER_COLOR = new Color(254, 253, 255);

	@Override
	public void draw(AdvancedGraphics2D graphics, DiagramObject item) {
		final Node node = (Node) item;
		graphics.drawRectangle(node.getProp());

		graphics.drawRectangle(node.getProp(), PADDING);
	}

	@Override
	public void fill(AdvancedGraphics2D graphics, DiagramObject item) {
		final NodeProperties properties = ((Node) item).getProp();
		graphics.fillRectangle(properties);
		final Paint old = graphics.getGraphics().getPaint();
		graphics.getGraphics().setPaint(INNER_COLOR);
		graphics.fillRectangle(properties, PADDING);
		graphics.getGraphics().setPaint(old);
	}


	@Override
	public void drawText(AdvancedGraphics2D graphics, DiagramObject item) {
		graphics.drawText((Node) item, PADDING);
	}
}
