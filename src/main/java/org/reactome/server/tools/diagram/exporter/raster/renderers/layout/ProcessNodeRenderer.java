package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfileNode;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.StrokeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.FillLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.LineLayer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layers.TextLayer;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ProcessNodeRenderer extends NodeAbstractRenderer {

	// When it comes to analysis, alpha must be 0.75
	private static final String ANALYSIS_INNER_COLOR = "rgba(254, 253, 255, 191)";
	private static final String INNER_COLOR = "#fefdff";

	@Override
	protected void fill(FillLayer fillLayer, NodeCommon node, Shape shape, DiagramProfileNode clas, double factor, DiagramProfile diagramProfile) {
		final String fill = clas.getFill();
		final Shape inner = inner(factor, node);
		final Area outer = new Area(shape);
		outer.subtract(new Area(inner));
		fillLayer.add(fill, outer);
		fillLayer.add(INNER_COLOR, inner);
	}

	@Override
	protected void border(LineLayer lineLayer, DiagramObject node, String border, DiagramProfile diagramProfile, DiagramIndex index, Shape shape, DiagramProfileNode clas, double factor) {
//		final String border = computeBorderColor(node, diagramProfile, index, clas);
		final Stroke stroke = isDashed(node)
				? StrokeProperties.DASHED_BORDER_STROKE
				: StrokeProperties.BORDER_STROKE;
		lineLayer.add(border, stroke, shape);
		lineLayer.add(border, stroke, inner(factor, (NodeCommon) node));
	}

	@Override
	protected void text(TextLayer textLayer, NodeCommon node, double factor, DiagramProfileNode clas) {
		final NodeProperties limits = new ScaledNodeProperties(node.getProp(), factor);
		textLayer.add(clas.getText(), node.getDisplayName(), limits, RendererProperties.PROCESS_NODE_INSET_WIDTH);
	}

	private Shape inner(double factor, NodeCommon node) {
		final NodeProperties properties = new ScaledNodeProperties(node.getProp(), factor);
		return new Rectangle2D.Double(properties.getX() + RendererProperties.PROCESS_NODE_INSET_WIDTH,
				properties.getY() + RendererProperties.PROCESS_NODE_INSET_WIDTH,
				properties.getWidth() - 2 * RendererProperties.PROCESS_NODE_INSET_WIDTH,
				properties.getHeight() - 2 * RendererProperties.PROCESS_NODE_INSET_WIDTH);
	}
}
