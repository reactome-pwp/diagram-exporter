package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfileNode;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.*;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.LineLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.TextLayer;

import java.awt.*;

/**
 * Sets add an inner border.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class SetRenderer extends NodeAbstractRenderer {

	@Override
	protected void border(LineLayer lineLayer, DiagramObject item, String border, DiagramProfile diagramProfile, DiagramIndex index, Shape shape, DiagramProfileNode clas, double factor) {
		final Node node = (Node) item;
//		final String border = computeBorderColor(node, diagramProfile, index, clas);
		final Stroke stroke = isDashed(node)
				? StrokeProperties.DASHED_BORDER_STROKE
				: StrokeProperties.BORDER_STROKE;
		lineLayer.add(border, stroke, shape);
		final Shape innerShape = innerShape(factor, node);
		lineLayer.add(border, stroke, innerShape);
	}

	@Override
	protected Shape shape(double factor, DiagramObject item) {
		final Node node = (Node) item;
		final NodeProperties prop = new ScaledNodeProperties(node.getProp(), factor);
		return ShapeFactory.roundedRectangle(
				prop.getX(), prop.getY(), prop.getWidth(), prop.getHeight());
	}

	private Shape innerShape(double factor, Node node) {
		final NodeProperties prop = new ScaledNodeProperties(node.getProp(), factor);
		return ShapeFactory.roundedRectangle(
				prop.getX(),
				prop.getY(),
				prop.getWidth(),
				prop.getHeight(), RendererProperties.SEPARATION);
	}

	@Override
	protected void text(TextLayer textLayer, NodeCommon node, double factor, DiagramProfileNode clas) {
		final NodeProperties properties = new ScaledNodeProperties(node.getProp(), factor);
//		// Adds 1*factor padding to the inner rectangle
		final double padding = RendererProperties.SEPARATION + factor;
		textLayer.add(clas.getText(), node.getDisplayName(), properties, padding);
	}

}
