package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.EdgeCommon;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.EdgeRenderer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.FlowLineRenderer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

public class RenderableFlowLine extends RenderableLink {

	private static final FlowLineRenderer renderer = new FlowLineRenderer();

	public RenderableFlowLine(EdgeCommon edge) {
		super(edge);
	}

	@Override
	public EdgeRenderer getRenderer() {
		return renderer;
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getFlowLine();
	}

	@Override
	public boolean isDashed() {
		return false;
	}
}
