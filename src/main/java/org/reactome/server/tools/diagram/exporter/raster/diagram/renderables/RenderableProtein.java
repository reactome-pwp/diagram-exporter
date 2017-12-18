package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.NodeAbstractRenderer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.ProteinRenderer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.awt.*;

public class RenderableProtein extends RenderableNode {
	public RenderableProtein(Node node) {
		super(node);
	}

	@Override
	Shape backgroundShape() {
		return ShapeFactory.roundedRectangle(getNode().getProp());
	}

	@Override
	NodeAbstractRenderer getRenderer() {
		return new ProteinRenderer();
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getProtein();
	}
}
