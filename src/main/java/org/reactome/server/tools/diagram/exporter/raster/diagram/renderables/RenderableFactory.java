package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.EdgeCommon;
import org.reactome.server.tools.diagram.data.layout.Node;

public class RenderableFactory {

	public static RenderableNode getRenderableNode(Node node) {
		switch (node.getRenderableClass()) {
			case "Protein":
				return new RenderableProtein(node);
			case "Chemical":
				return new RenderableChemical(node);
			case "Complex":
				return new RenderableComplex(node);
			case "EntitySet":
				return new RenderableEntitySet(node);
			case "ProcessNode":
				return new RenderableProcessNode(node);
			case "EncapsulatedNode":
				return new RenderableEncapsulatedNode(node);
			case "RNA":
				return new RenderableRna(node);
			case "Gene":
				return new RenderableGene(node);
			case "Entity":
				return new RenderableEntity(node);
		}
		throw new IllegalArgumentException("Class " + node.getRenderableClass() + " not recognized");

	}

	public static RenderableEdge getRenderableEdge(EdgeCommon edge) {
		switch (edge.getRenderableClass()) {
			case "FlowLine":
				return new RenderableFlowLine(edge);
			case "Interaction":
				return new RenderableInteraction(edge);
			case "EntitySetAndMemberLink":
				return new RenderableLink(edge);
			case "EntitySetAndEntitySetLink":
				return new RenderableLink(edge);
			case "Reaction":
				return new RenderableReaction(edge);
		}
		throw new IllegalArgumentException("Class " + edge.getRenderableClass() + " not recognized");
	}

}
