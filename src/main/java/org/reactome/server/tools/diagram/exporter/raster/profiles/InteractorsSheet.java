package org.reactome.server.tools.diagram.exporter.raster.profiles;

public class InteractorsSheet extends ColorSheet {

	private NodeColorSheet protein;
	private NodeColorSheet chemical;

	public NodeColorSheet getChemical() {
		return chemical;
	}


	public NodeColorSheet getProtein() {
		return protein;
	}
}
