package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.NodeAbstractRenderer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.RnaRenderer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.awt.*;

public class RenderableRna extends RenderableNode {
	private static RnaRenderer renderer = new RnaRenderer();

	RenderableRna(Node node) {
		super(node);
	}

	@Override
	Shape backgroundShape() {
		return ShapeFactory.getRnaShape(getNode().getProp());
	}

	@Override
	NodeAbstractRenderer getRenderer() {
		return renderer;
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getRna();
	}

}
