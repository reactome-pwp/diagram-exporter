package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.NodeRenderInfo;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ProcessNodeRenderer extends NodeAbstractRenderer {

	// When it comes to analysis, alpha must be 0.75
	private static final Color ANALYSIS_INNER_COLOR = new Color(254, 253, 255, 191);
	private static final Color INNER_COLOR = new Color(254, 253, 255);

	@Override
	public Shape backgroundShape(NodeCommon node) {
		return ShapeFactory.rectangle(node.getProp());
	}

	@Override
	public Shape foregroundShape(NodeCommon node) {
		return ShapeFactory.rectangle(node.getProp(), RendererProperties.PROCESS_NODE_PADDING);
	}

	@Override
	public Color getForegroundFill(ColorProfiles colors, DiagramIndex index) {
		return index.getAnalysis().getType() == AnalysisType.NONE
				? INNER_COLOR
				: ANALYSIS_INNER_COLOR;
	}

	@Override
	public double expression(ColorProfiles colorProfiles, NodeRenderInfo info, DiagramIndex index, int t) {
		final Double percentage = info.getDecorator().getEnrichment();
		if (percentage != null && percentage > 0) {
			final NodeProperties prop = info.getNode().getProp();
			final Color color = colorProfiles.getDiagramSheet().getProcessNode().getFill();
			final Area enrichment = new Area(info.getBackgroundShape());
			final Rectangle2D rectangle = new Rectangle2D.Double(
					prop.getX() + prop.getWidth() * percentage,
					prop.getY(),
					prop.getWidth(),
					prop.getHeight());
			enrichment.intersect(new Area(rectangle));
			info.getBackgroundArea().subtract(enrichment);
			info.getAnalysisLayer().add(color, enrichment);
		}
		// process node text is not split
		return 0;
	}

}
