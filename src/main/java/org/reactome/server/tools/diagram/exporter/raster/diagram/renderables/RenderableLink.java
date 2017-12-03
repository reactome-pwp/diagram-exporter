package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.EdgeCommon;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout.EdgeRenderer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout.LinkRenderer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.util.Collections;
import java.util.List;

public class RenderableLink extends RenderableEdge {
	private static final LinkRenderer renderer = new LinkRenderer();

	public RenderableLink(EdgeCommon edge) {
		super(edge);
	}

	@Override
	public EdgeRenderer getRenderer() {
		return renderer;
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getLink();
	}

	@Override
	protected List<Shape> getRenderableShapes() {
		// report: links have reaction shape
		return Collections.singletonList(getEdge().getEndShape());
	}

	@Override
	public boolean isDashed() {
		return true;
	}
}
