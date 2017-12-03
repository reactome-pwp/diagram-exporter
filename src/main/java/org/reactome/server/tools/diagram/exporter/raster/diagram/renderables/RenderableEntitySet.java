package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout.NodeAbstractRenderer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout.SetRenderer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.awt.*;

public class RenderableEntitySet extends RenderableNode {

	private static final SetRenderer renderer = new SetRenderer();

	RenderableEntitySet(Node node) {
		super(node);
	}

	@Override
	Shape backgroundShape() {
		return ShapeFactory.roundedRectangle(getNode().getProp());
	}

	@Override
	NodeAbstractRenderer getRenderer() {
		return renderer;
	}


	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getEntitySet();
	}
}
