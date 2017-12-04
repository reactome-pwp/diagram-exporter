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
	private final Shape line;

	RenderableGene(Node node) {
		super(node);
		line = ShapeFactory.getGeneLine(
				getNode().getProp().getX(),
				getNode().getProp().getY(),
				getNode().getProp().getWidth());
		arrow = ShapeFactory.getGeneArrow(
				getNode().getProp().getX(),
				getNode().getProp().getY(),
				getNode().getProp().getWidth());
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

	public Shape getLine() {
		return line;
	}
}
