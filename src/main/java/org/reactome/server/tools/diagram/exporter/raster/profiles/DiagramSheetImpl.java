package org.reactome.server.tools.diagram.exporter.raster.profiles;

public class DiagramSheetImpl implements DiagramSheet{

	private String name;
	private PropertiesColorSheetImpl properties;
	private NodeColorSheetImpl attachment;
	private NodeColorSheetImpl chemical;
	private NodeColorSheetImpl compartment;
	private NodeColorSheetImpl complex;
	private NodeColorSheetImpl entity;
	private NodeColorSheetImpl entitySet;
	private NodeColorSheetImpl flowLine;
	private NodeColorSheetImpl gene;
	private NodeColorSheetImpl interactor;
	private NodeColorSheetImpl link;
	private NodeColorSheetImpl note;
	private NodeColorSheetImpl otherEntity;
	private NodeColorSheetImpl processNode;
	private NodeColorSheetImpl protein;
	private NodeColorSheetImpl reaction;
	private NodeColorSheetImpl rna;
	private NodeColorSheetImpl stoichiometry;
	private ThumbnailColorSheetIml thumbnail;


	public void setName(String name) {
		this.name = name;
	}

	@Override
	public PropertiesColorSheet getProperties() {
		return properties;
	}

	@Override
	public NodeColorSheet getAttachment() {
		return attachment;
	}

	@Override
	public NodeColorSheet getChemical() {
		return chemical;
	}

	@Override
	public NodeColorSheet getCompartment() {
		return compartment;
	}

	@Override
	public NodeColorSheet getComplex() {
		return complex;
	}

	@Override
	public NodeColorSheet getEntity() {
		return entity;
	}

	@Override
	public NodeColorSheet getEntitySet() {
		return entitySet;
	}

	@Override
	public NodeColorSheet getFlowLine() {
		return flowLine;
	}

	@Override
	public NodeColorSheet getGene() {
		return gene;
	}

	@Override
	public NodeColorSheet getInteractor() {
		return interactor;
	}

	@Override
	public NodeColorSheet getLink() {
		return link;
	}

	@Override
	public NodeColorSheet getNote() {
		return note;
	}

	@Override
	public NodeColorSheet getOtherEntity() {
		return otherEntity;
	}

	@Override
	public NodeColorSheet getProcessNode() {
		return processNode;
	}

	@Override
	public NodeColorSheet getProtein() {
		return protein;
	}

	@Override
	public NodeColorSheet getReaction() {
		return reaction;
	}

	@Override
	public NodeColorSheet getRna() {
		return rna;
	}

	@Override
	public NodeColorSheet getStoichiometry() {
		return stoichiometry;
	}

	@Override
	public ThumbnailColorSheet getThumbnail() {
		return thumbnail;
	}

	@Override
	public String getName() {
		return name;
	}

}
