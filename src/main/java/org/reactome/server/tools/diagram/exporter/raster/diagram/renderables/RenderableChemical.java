package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.ChemicalRenderer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.NodeAbstractRenderer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class RenderableChemical extends RenderableNode {

	private static ChemicalRenderer renderer = new ChemicalRenderer();

	RenderableChemical(Node node) {
		super(node);
	}

	@Override
	Shape backgroundShape() {
		final NodeProperties properties = getNode().getProp();
		return new Ellipse2D.Double(properties.getX(), properties.getY(),
				properties.getWidth(), properties.getHeight());
	}

	@Override
	NodeAbstractRenderer getRenderer() {
		return renderer;
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getChemical();
	}
}
