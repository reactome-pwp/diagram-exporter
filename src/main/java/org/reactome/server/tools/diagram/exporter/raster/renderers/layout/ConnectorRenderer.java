package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

public class ConnectorRenderer extends EdgeAbstractRenderer {

	public void draw(AdvancedGraphics2D graphics, Connector connector) {
		drawShape(graphics, connector.getEndShape());
	}

	public void drawSegments(AdvancedGraphics2D graphics, Connector connector) {
		connector.getSegments().forEach(segment -> draw(graphics, segment));
	}

	public void fill(AdvancedGraphics2D graphics, Connector connector) {
		fillShape(graphics, connector.getEndShape());
	}

	public void drawText(AdvancedGraphics2D graphics, Connector connector) {
		final Shape shape = connector.getEndShape();
		if (shape != null && shape.getS() != null)
			graphics.drawText(shape.getS(),
					shape.getA().getX(), shape.getA().getY(),
					shape.getB().getX() - shape.getA().getX(),
					shape.getB().getY() - shape.getA().getY(), 0.0);
	}

}
