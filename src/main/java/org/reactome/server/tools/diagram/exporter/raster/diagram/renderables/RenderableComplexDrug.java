package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramData;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

public class RenderableComplexDrug extends RenderableComplex {

	RenderableComplexDrug(Node node) {
		super(node);
	}

	@Override
	public void draw(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramData data, int t) {
		super.draw(canvas, colorProfiles, data, t);
		DrugHelper.addDrugText(canvas, this, colorProfiles, data, 4, 2);
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getComplexDrug();
	}
}
