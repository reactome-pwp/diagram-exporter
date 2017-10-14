package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfileNode;

/**
 * empty renderer. Drawing with it will no have effects in the graphics.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public abstract class AbstractRenderer implements Renderer {

	static DiagramProfileNode getDiagramProfileNode(String rClass, DiagramProfile diagramProfile) {
		switch (rClass) {
			case "Chemical":
				return diagramProfile.getChemical();
			case "Complex":
				return diagramProfile.getComplex();
			case "Entity": // Weirdo black background
//				return diagramProfile.getEntity();
			case "EntitySet":
				return diagramProfile.getEntityset();
			case "Gene":
				return diagramProfile.getGene();
			case "ProcessNode":
				return diagramProfile.getProcessnode();
			case "Protein":
				return diagramProfile.getProtein();
			case "RNA":
				return diagramProfile.getRna();
			case "Reaction":
				return diagramProfile.getReaction();
			case "EntitySetAndEntitySetLink":
			case "EntitySetAndMemberLink":
			case "Interaction":
				return diagramProfile.getLink();
			case "FlowLine":
				return diagramProfile.getFlowline();
			case "Stoichiometry":
				return diagramProfile.getStoichiometry();
			default:
				throw new IllegalArgumentException("Type " + rClass + " is not found in the JSON Profile.");
		}
	}
}
