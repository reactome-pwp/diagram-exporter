package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.profile.analysis.AnalysisProfile;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfileNode;
import org.reactome.server.tools.diagram.data.profile.interactors.InteractorProfile;
import org.reactome.server.tools.diagram.exporter.raster.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;

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
			case "Entity":
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
				throw new IllegalArgumentException("Type " + rClass + " is not found in the JSON Profile.");		}
	}

	@Override
	public void draw(DiagramCanvas canvas, DiagramObject item, DiagramProfile diagramProfile, AnalysisProfile analysisProfile, InteractorProfile interactorProfile, DiagramIndex index, AnalysisType analysisType) {

	}
}
