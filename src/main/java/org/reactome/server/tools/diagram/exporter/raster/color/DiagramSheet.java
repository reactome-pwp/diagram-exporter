package org.reactome.server.tools.diagram.exporter.raster.color;

public interface DiagramSheet extends ColorSheet {

	PropertiesColorSheet getProperties();

	NodeColorSheet getAttachment();

	NodeColorSheet getChemical();

	NodeColorSheet getCompartment();

	NodeColorSheet getComplex();

	NodeColorSheet getEntity();

	NodeColorSheet getEntitySet();

	NodeColorSheet getFlowLine();

	NodeColorSheet getGene();

	NodeColorSheet getInteractor();

	NodeColorSheet getLink();

	NodeColorSheet getNote();

	NodeColorSheet getOtherEntity();

	NodeColorSheet getProcessNode();

	NodeColorSheet getProtein();

	NodeColorSheet getReaction();

	NodeColorSheet getRna();

	NodeColorSheet getStoichiometry();

	ThumbnailColorSheet getThumbnail();


}
