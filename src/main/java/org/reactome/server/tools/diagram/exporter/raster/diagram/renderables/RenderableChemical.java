package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class RenderableChemical extends RenderableNode {

	RenderableChemical(Node node) {
		super(node);
	}

	@Override
	protected Shape backgroundShape() {
		final NodeProperties properties = getDiagramObject().getProp();
		return new Ellipse2D.Double(properties.getX(), properties.getY(),
				properties.getWidth(), properties.getHeight());
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getChemical();
	}
}
