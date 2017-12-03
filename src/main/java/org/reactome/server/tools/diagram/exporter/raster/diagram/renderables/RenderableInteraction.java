package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.EdgeCommon;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout.EdgeRenderer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout.InteractionRenderer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

public class RenderableInteraction extends RenderableLink {

	private static final InteractionRenderer renderer = new InteractionRenderer();

	public RenderableInteraction(EdgeCommon edge) {
		super(edge);
	}

	@Override
	public EdgeRenderer getRenderer() {
		return renderer;
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getInteractor();
	}

	@Override
	public boolean isDashed() {
		return false;
	}
}
