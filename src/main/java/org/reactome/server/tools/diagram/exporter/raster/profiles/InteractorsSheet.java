package org.reactome.server.tools.diagram.exporter.raster.profiles;

public interface InteractorsSheet extends ColorSheet {

	NodeColorSheet getChemical();

	NodeColorSheet getProtein();
}
