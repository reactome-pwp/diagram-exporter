package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class RnaRenderer extends NodeAbstractRenderer {

//	@Override
//	public void drawBorder(AdvancedGraphics2D graphics, DiagramObject item) {
//		graphics.drawBone(((Node) item).getProp());
//
//	}
//
//	@Override
//	public void fill(AdvancedGraphics2D graphics, DiagramObject item) {
//		graphics.fillBone(((Node) item).getProp());
//	}


	@Override
	public void draw(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items, Paint fillColor, Paint lineColor, Paint textColor, Stroke segmentStroke, Stroke borderStroke) {
		final Collection<Node> nodes = (Collection<Node>) items;
		final List<Shape> shapes = nodes.stream()
				.map(node -> shape(graphics, node))
				.collect(Collectors.toList());
		graphics.getGraphics().setPaint(fillColor);
		shapes.forEach(shape -> graphics.getGraphics().fill(shape));

		graphics.getGraphics().setPaint(lineColor);
		shapes.forEach(shape -> graphics.getGraphics().draw(shape));

		graphics.getGraphics().setPaint(textColor);
		nodes.forEach(node -> graphics.drawText(node, RendererProperties.RNA_LOOP_WIDTH));
	}

	@Override
	protected Shape shape(AdvancedGraphics2D graphics, Node node) {
		final NodeProperties properties = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		final double loopWidth = RendererProperties.RNA_LOOP_WIDTH;
		final double x = properties.getX();
		final double y = properties.getY();
		final double width = properties.getWidth();
		final double height = properties.getHeight();
		double right = x + width;
		double bottom = y + height;
		final Path2D path = new GeneralPath();

		double xAux = x + loopWidth;
		double yAux = y + loopWidth / 2;
		path.moveTo(xAux, yAux);
		xAux = right - loopWidth;
		path.lineTo(xAux, yAux);
		yAux = y + height / 2;
		path.quadTo(right, y, right, yAux);

		xAux = right - loopWidth;
		yAux = bottom - loopWidth / 2;
		path.quadTo(right, bottom, xAux, yAux);

		xAux = x + loopWidth;
		path.lineTo(xAux, yAux);
		yAux = y + height / 2;
		path.quadTo(x, bottom, x, yAux);

		xAux = x + loopWidth;
		yAux = y + loopWidth / 2;
		path.quadTo(x, y, xAux, yAux);
		path.closePath();
		return path;
	}

}
