package org.reactome.server.tools.diagram.exporter.raster.profiles;

public class DiagramSheet extends ColorSheet {

	private PropertiesColorSheet properties;
	private NodeColorSheet attachment;
	private NodeColorSheet chemical;
	private NodeColorSheet chemicalDrug;
	private NodeColorSheet compartment;
	private NodeColorSheet complex;
	private NodeColorSheet entity;
	private NodeColorSheet entitySet;
	private NodeColorSheet flowLine;
	private NodeColorSheet gene;
	private NodeColorSheet interactor;
	private NodeColorSheet link;
	private NodeColorSheet note;
	private NodeColorSheet otherEntity;
	private NodeColorSheet processNode;
	private NodeColorSheet protein;
	private NodeColorSheet reaction;
	private NodeColorSheet rna;
	private NodeColorSheet stoichiometry;
	private NodeColorSheet encapsulatedNode;
	private NodeColorSheet entitySetDrug;
	private NodeColorSheet proteinDrug;
	private NodeColorSheet rnaDrug;
	private NodeColorSheet complexDrug;
	private ThumbnailColorSheet thumbnail;

	public PropertiesColorSheet getProperties() {
		return properties;
	}

	public NodeColorSheet getAttachment() {
		return attachment;
	}

	public NodeColorSheet getChemical() {
		return chemical;
	}

	public NodeColorSheet getChemicalDrug() {
		return chemicalDrug;
	}

	public NodeColorSheet getCompartment() {
		return compartment;
	}

	public NodeColorSheet getComplex() {
		return complex;
	}

	public NodeColorSheet getEntity() {
		return entity;
	}

	public NodeColorSheet getEntitySet() {
		return entitySet;
	}

	public NodeColorSheet getFlowLine() {
		return flowLine;
	}

	public NodeColorSheet getGene() {
		return gene;
	}

	public NodeColorSheet getInteractor() {
		return interactor;
	}

	public NodeColorSheet getLink() {
		return link;
	}

	public NodeColorSheet getNote() {
		return note;
	}

	public NodeColorSheet getOtherEntity() {
		return otherEntity;
	}

	public NodeColorSheet getProcessNode() {
		return processNode;
	}

	public NodeColorSheet getProtein() {
		return protein;
	}

	public NodeColorSheet getReaction() {
		return reaction;
	}

	public NodeColorSheet getRna() {
		return rna;
	}

	public NodeColorSheet getStoichiometry() {
		return stoichiometry;
	}

	public NodeColorSheet getEncapsulatedNode() {
		return encapsulatedNode;
	}

	public NodeColorSheet getEntitySetDrug() {
		return entitySetDrug;
	}

	public NodeColorSheet getProteinDrug() {
		return proteinDrug;
	}

	public NodeColorSheet getRnaDrug() {
		return rnaDrug;
	}

	public NodeColorSheet getComplexDrug() {
		return complexDrug;
	}

	public ThumbnailColorSheet getThumbnail() {
		return thumbnail;
	}
}
