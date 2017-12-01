package org.reactome.server.tools.diagram.exporter.raster.profiles;

public class InteractorsSheet {

	private String name;
	private NodeColorSheet protein;
	private NodeColorSheet chemical;


	public String getName() {
		return name;
	}


	public NodeColorSheet getChemical() {
		return chemical;
	}


	public NodeColorSheet getProtein() {
		return protein;
	}
}
