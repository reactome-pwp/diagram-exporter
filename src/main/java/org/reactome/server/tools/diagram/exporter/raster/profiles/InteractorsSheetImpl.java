package org.reactome.server.tools.diagram.exporter.raster.profiles;

public class InteractorsSheetImpl implements InteractorsSheet {

	private String name;
	private NodeColorSheetImpl protein;
	private NodeColorSheetImpl chemical;

	@Override
	public String getName() {
		return name;
	}


	@Override
	public NodeColorSheet getChemical() {
		return chemical;
	}

	@Override
	public NodeColorSheet getProtein() {
		return protein;
	}
}
