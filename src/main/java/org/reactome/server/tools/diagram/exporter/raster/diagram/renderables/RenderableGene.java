package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.GeneRenderer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.NodeAbstractRenderer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.awt.*;

public class RenderableGene extends RenderableNode {
	private static GeneRenderer renderer = new GeneRenderer();

	private final Shape arrow;
	private final Shape lines;

	RenderableGene(Node node) {
		super(node);
		lines = ShapeFactory.getGeneLine(getNode().getProp());
		arrow = ShapeFactory.getGeneArrow(getNode().getProp());
	}

	@Override
	Shape backgroundShape() {
		return ShapeFactory.getGeneFillShape(getNode().getProp());
	}

	@Override
	NodeAbstractRenderer getRenderer() {
		return renderer;
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getGene();
	}

	public Shape getArrow() {
		return arrow;
	}

	public Shape getLines() {
		return lines;
	}
}
