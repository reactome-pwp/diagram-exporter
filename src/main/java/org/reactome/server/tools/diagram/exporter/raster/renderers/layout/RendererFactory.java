package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import java.util.HashMap;
import java.util.Map;

public class RendererFactory {

	private static Map<String, Renderer> renderers;

	static {
		createRenderers();
	}

	private RendererFactory() {
	}

	public static Renderer get(String renderingClass) {
		return renderers.get(renderingClass);
	}

	private static void createRenderers() {
		renderers = new HashMap<>();
		renderers.put("Protein", new ProteinRenderer());
		renderers.put("Chemical", new ChemicalRenderer());
		renderers.put("ChemicalDrug", new ChemicalDrugRenderer());
		renderers.put("Reaction", new ReactionRenderer());
		renderers.put("Complex", new ComplexRenderer());
		renderers.put("Entity", new OtherEntityRenderer());
		renderers.put("EntitySet", new SetRenderer());
		renderers.put("ProcessNode", new ProcessNodeRenderer());
		renderers.put("FlowLine", new FlowLineRenderer());
		renderers.put("Interaction", new InteractionRenderer());
		renderers.put("RNA", new RnaRenderer());
		renderers.put("Gene", new GeneRenderer());
		renderers.put("EntitySetAndMemberLink", new LinkRenderer());
		renderers.put("EntitySetAndEntitySetLink", new LinkRenderer());
	}

}
