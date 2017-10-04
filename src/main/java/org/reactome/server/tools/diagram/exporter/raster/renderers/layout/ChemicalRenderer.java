package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.IntNodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.List;

public class ChemicalRenderer extends NodeAbstractRenderer {


//	@Override
//	public void draw(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items, Paint fillColor, Paint lineColor, Paint textColor, Stroke stroke) {
//		final Collection<Node> nodes = (Collection<Node>) items;
//		if (fillColor != null) {
//			graphics.getGraphics().setPaint(fillColor);
//			nodes.stream()
//					.map(NodeCommon::getProp)
//					.map(props -> new IntNodeProperties(new ScaledNodeProperties(props, graphics.getFactor())))
//					.forEach(props -> graphics.getGraphics().fillOval(props.intX(), props.intY(), props.intWidth(), props.intHeight()));
//		}
//		if (lineColor != null) {
//			graphics.getGraphics().setPaint(lineColor);
//			nodes.stream()
//					.map(NodeCommon::getProp)
//					.map(props -> new IntNodeProperties(new ScaledNodeProperties(props, graphics.getFactor())))
//					.forEach(props -> graphics.getGraphics().drawOval(props.intX(), props.intY(), props.intWidth(), props.intHeight()));
//		}
//		if (textColor != null) {
//			graphics.getGraphics().setPaint(textColor);
//			nodes.forEach(graphics::drawText);
//		}
//	}

	protected Shape shape(AdvancedGraphics2D graphics, Node node) {
		final NodeProperties properties = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		return new Ellipse2D.Double(properties.getX(), properties.getY(), properties.getWidth(), properties.getHeight());
	}
}
