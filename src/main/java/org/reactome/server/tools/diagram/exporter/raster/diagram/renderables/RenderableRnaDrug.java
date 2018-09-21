package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

public class RenderableRnaDrug extends RenderableRna {
	RenderableRnaDrug(Node node) {
		super(node);
	}

	@Override
	public void draw(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		super.draw(canvas, colorProfiles, index, t);
		DrugHelper.addDrugText(canvas, this, colorProfiles, index, 4, 7);
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getRnaDrug();
	}
}
