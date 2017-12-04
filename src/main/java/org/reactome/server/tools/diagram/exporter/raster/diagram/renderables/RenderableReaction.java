package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.EdgeCommon;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.EdgeRenderer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.ReactionRenderer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

public class RenderableReaction extends RenderableEdge {

	private static final EdgeRenderer renderer = new ReactionRenderer();

	public RenderableReaction(EdgeCommon edge) {
		super(edge);
	}

	@Override
	public EdgeRenderer getRenderer() {
		return renderer;
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getReaction();
	}
}
