package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Shadow;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramData;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

public class RenderableShadow extends RenderableDiagramObject<Shadow> {

	RenderableShadow(Shadow shadow) {super(shadow);}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return null;
	}

	@Override
	public void draw(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramData data, int t) {
		// no drawn
	}
}
