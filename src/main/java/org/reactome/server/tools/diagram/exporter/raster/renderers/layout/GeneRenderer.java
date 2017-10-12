package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfileNode;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.*;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.LineLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.TextLayer;

import java.awt.*;

/**
 * Renderer for genes. These ones are a little bit more complex than the rest.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class GeneRenderer extends NodeAbstractRenderer {

	@Override
	protected void border(LineLayer lineLayer, DiagramObject node, String border, DiagramProfile diagramProfile, DiagramIndex index, Shape shape, DiagramProfileNode clas, double factor) {
		final Shape line = line(factor, (Node) node);
		lineLayer.add(border, StrokeProperties.SEGMENT_STROKE, line);
		final Shape arrow = arrow(factor, (Node) node);
		lineLayer.add(border, StrokeProperties.SEGMENT_STROKE, arrow);
//		lineLayer.getFill().add(border, arrow);
	}

	@Override
	protected void text(TextLayer textLayer, NodeCommon node, double factor, DiagramProfileNode clas) {
		final NodeProperties properties = new ScaledNodeProperties(node.getProp(), factor);
		final double x = properties.getX();
		final double width = properties.getWidth();
		final double yOffset = 0.5 * RendererProperties.GENE_SYMBOL_WIDTH;
		final double y = properties.getY() + yOffset;
		final double height = properties.getHeight() - yOffset;
		final String color = clas.getText();
		final NodeProperties limits = NodePropertiesFactory.get(x, y, width, height);
		textLayer.add(color, node.getDisplayName(), limits, RendererProperties.NODE_TEXT_PADDING);
	}

	@Override
	protected Shape shape(double factor, DiagramObject item) {
		final Node node = (Node) item;
		final NodeProperties prop = new ScaledNodeProperties(node.getProp(), factor);
		final double x = prop.getX();
		final double y = prop.getY();
		final double width = prop.getWidth();
		final double height = prop.getHeight();
		return ShapeFactory.getGeneFillShape(x, y, width, height);
	}

	private Shape line(double factor, Node node) {
		final NodeProperties prop = new ScaledNodeProperties(node.getProp(), factor);
		final double x = prop.getX();
		final double y = prop.getY();
		final double width = prop.getWidth();
		return ShapeFactory.getGeneLine(x, y, width);
	}

	private Shape arrow(double factor, Node node) {
		final NodeProperties prop = new ScaledNodeProperties(node.getProp(), factor);
		final double x = prop.getX();
		final double y = prop.getY();
		final double width = prop.getWidth();
		return ShapeFactory.getGeneArrow(x, y, width);
	}

}
