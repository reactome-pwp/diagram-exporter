package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.awt.*;

public class RenderableComplex extends RenderableNode {

	RenderableComplex(Node node) {
		super(node);
	}

	@Override
	protected Shape backgroundShape() {
		final NodeProperties prop = getDiagramObject().getProp();
		return ShapeFactory.getCornedRectangle(prop.getX(), prop.getY(),
				prop.getWidth(), prop.getHeight());
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getComplex();
	}
}
