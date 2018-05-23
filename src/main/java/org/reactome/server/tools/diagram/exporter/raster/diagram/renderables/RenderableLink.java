package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Link;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.util.Collections;
import java.util.List;

public class RenderableLink extends RenderableEdgeCommon<Link> {

	RenderableLink(Link edge) {
		super(edge);
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getLink();
	}

	@Override
	public void draw(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, int t) {

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
