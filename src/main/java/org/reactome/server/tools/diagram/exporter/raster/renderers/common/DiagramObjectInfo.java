package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import org.reactome.server.tools.diagram.exporter.raster.profiles.DiagramSheet;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class DiagramObjectInfo {

	static NodeColorSheet getNodeColorSheet(String rClass, DiagramSheet diagramSheet) {
		switch (rClass) {
			case "Chemical":
				return diagramSheet.getChemical();
			case "Complex":
				return diagramSheet.getComplex();
			case "Entity": // Weirdo black background
				return diagramSheet.getOtherEntity();
			case "EntitySet":
				return diagramSheet.getEntitySet();
			case "Gene":
				return diagramSheet.getGene();
			case "ProcessNode":
				return diagramSheet.getProcessNode();
			case "Protein":
				return diagramSheet.getProtein();
			case "RNA":
				return diagramSheet.getRna();
			case "Reaction":
				return diagramSheet.getReaction();
			case "EntitySetAndEntitySetLink":
			case "EntitySetAndMemberLink":
			case "Interaction":
				return diagramSheet.getLink();
			case "FlowLine":
				return diagramSheet.getFlowLine();
			case "Stoichiometry":
				return diagramSheet.getStoichiometry();
			default:
				throw new IllegalArgumentException("Type " + rClass + " is not found in the JSON Profile.");
		}
	}
}
