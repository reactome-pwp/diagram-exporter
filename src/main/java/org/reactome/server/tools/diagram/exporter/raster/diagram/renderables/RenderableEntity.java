package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout.NodeAbstractRenderer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout.OtherEntityRenderer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class RenderableEntity extends RenderableNode {

	private static OtherEntityRenderer renderer = new OtherEntityRenderer();

	RenderableEntity(Node node) {
		super(node);
	}

	@Override
	Shape backgroundShape() {
		return new Rectangle2D.Double(
				getNode().getProp().getX(),
				getNode().getProp().getY(),
				getNode().getProp().getWidth(),
				getNode().getProp().getHeight());
	}

	@Override
	NodeAbstractRenderer getRenderer() {
		return renderer;
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getOtherEntity();
	}
}
