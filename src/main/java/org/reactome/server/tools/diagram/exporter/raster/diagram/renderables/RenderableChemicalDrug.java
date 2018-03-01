package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.ChemicalDrugRenderer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.NodeAbstractRenderer;

public class RenderableChemicalDrug extends RenderableChemical {

	private static final ChemicalDrugRenderer renderer = new ChemicalDrugRenderer();

	public RenderableChemicalDrug(Node node) {
		super(node);
	}

	@Override
	NodeAbstractRenderer getRenderer() {
		return renderer;
	}


}
