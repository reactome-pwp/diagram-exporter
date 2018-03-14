package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.ChemicalDrugRenderer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.NodeAbstractRenderer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

public class RenderableChemicalDrug extends RenderableChemical {

	private static final ChemicalDrugRenderer renderer = new ChemicalDrugRenderer();

	public RenderableChemicalDrug(Node node) {
		super(node);
	}

	@Override
	NodeAbstractRenderer getRenderer() {
		return renderer;
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getChemicalDrug();
	}
}
