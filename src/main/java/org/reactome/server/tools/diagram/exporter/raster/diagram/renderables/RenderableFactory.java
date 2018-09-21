package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.*;

public class RenderableFactory {

	/*
	 * DiagramObject
	 * - EdgeCommon
	 *    - Edge [Reaction]
	 *    - Link [FlowLine, Interaction, EntitySetAndMemberLink, EntitySetAndEntitySetLink]
	 * - NodeCommon
	 *    - Node [Chemical, ChemicalDrug, Complex, Entity, EntitySet, Gene, ProcessNode, EncapsulatedNode, Protein, RNA]
	 *    - Compartment [Compartment]
	 *    - Note [Note]
	 * - Shadow [Shadow]
	 */

	public static RenderableDiagramObject getRenderableObject(DiagramObject object) {
		switch (object.getRenderableClass()) {
			case "Protein":
				return new RenderableProtein((Node) object);
			case "Chemical":
				return new RenderableChemical((Node) object);
			case "ChemicalDrug":
				return new RenderableChemicalDrug((Node) object);
			case "Complex":
				return new RenderableComplex((Node) object);
			case "EntitySet":
				return new RenderableEntitySet((Node) object);
			case "ProcessNode":
				return new RenderableProcessNode((Node) object);
			case "EncapsulatedNode":
				return new RenderableEncapsulatedNode((Node) object);
			case "RNA":
				return new RenderableRna((Node) object);
			case "Gene":
				return new RenderableGene((Node) object);
			case "Entity":
				return new RenderableEntity((Node) object);
			case "FlowLine":
				return new RenderableFlowLine((Link) object);
			case "Interaction":
				return new RenderableInteraction((Link) object);
			case "EntitySetAndMemberLink":
				return new RenderableEntitySetAndMemberLink((Link) object);
			case "EntitySetAndEntitySetLink":
				return new RenderableEntitySetAndEntitySetLink((Link) object);
			case "Reaction":
				return new RenderableReaction((Edge) object);
			case "Compartment":
				return new RenderableCompartment((Compartment) object);
			case "Note":
				return new RenderableNote((Note) object);
			case "Shadow":
				return new RenderableShadow((Shadow) object);
			case "ProteinDrug":
				return new RenderableProteinDrug((Node) object);
			case "RNADrug":
				return new RenderableRnaDrug((Node) object);
			case "EntitySetDrug":
				return new RenderableEntitySetDrug((Node) object);
			case "ComplexDrug":
				return new RenderableComplexDrug((Node) object);
		}
		throw new UnsupportedOperationException("There is no RenderableObject class for " + object.getRenderableClass());
	}
}
