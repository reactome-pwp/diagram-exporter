package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class RenderableEntity extends RenderableNode {

	RenderableEntity(Node node) {
		super(node);
	}

	@Override
	public Shape backgroundShape() {
		return new Rectangle2D.Double(
				getNode().getProp().getX(),
				getNode().getProp().getY(),
				getNode().getProp().getWidth(),
				getNode().getProp().getHeight());
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getOtherEntity();
	}
}
