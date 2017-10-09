package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Edge;
import org.reactome.server.tools.diagram.data.layout.EdgeCommon;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.awt.*;
import java.util.Collection;
import java.util.List;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class EdgeRenderer extends AbstractRenderer {

	@Override
	public void text(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		final Collection<Edge> edges = (Collection<Edge>) items;
		edges.forEach(edgeCommon -> drawReactionText(graphics, edgeCommon));
	}
	private void drawReactionText(AdvancedGraphics2D graphics, Edge edge) {
		if (edge.getReactionShape().getS() == null)
			return;
		final Shape shape = edge.getReactionShape();
		TextRenderer.drawText(graphics, shape.getS(),
				shape.getA().getX(), shape.getA().getY(),
				shape.getB().getX() - shape.getA().getX(),
				shape.getB().getY() - shape.getA().getY(),
				graphics.getFactor(), true);
	}
	@Override
	public void segments(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		final Collection<EdgeCommon> edges = (Collection<EdgeCommon>) items;
		edges.stream().map(EdgeCommon::getSegments)
				.flatMap(Collection::stream)
				.map(segment -> ShapeFactory.line(graphics, segment.getFrom(), segment.getTo()))
				.forEach(shape -> graphics.getGraphics().draw(shape));
	}
}
