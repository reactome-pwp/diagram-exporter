package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

public class RenderableProteinDrug extends RenderableProtein {

	RenderableProteinDrug(Node node) {
		super(node);
	}

	// TODO: This is shared by all drugs, but creating a Drug class and extending from it will cause a diamond paradox.
	@SuppressWarnings("Duplicates")
	@Override
	public void draw(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		super.draw(canvas, colorProfiles, index, t);
		DrugHelper.addDrugText(canvas, this, colorProfiles, index, 0, 0);
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getProteinDrug();
	}
}
