package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.ComplexRenderer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.NodeAbstractRenderer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.awt.*;

public class RenderableComplex extends RenderableNode {

	private static ComplexRenderer renderer = new ComplexRenderer();

	RenderableComplex(Node node) {
		super(node);
	}

	@Override
	Shape backgroundShape() {
		final NodeProperties prop = getNode().getProp();
		return ShapeFactory.getCornedRectangle(prop.getX(), prop.getY(),
				prop.getWidth(), prop.getHeight());
	}

	@Override
	NodeAbstractRenderer getRenderer() {
		return renderer;
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getComplex();
	}
}
