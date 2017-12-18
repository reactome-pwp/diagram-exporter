package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.ProcessNodeRenderer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.awt.*;

public class RenderableProcessNode extends RenderableNode {

	private static ProcessNodeRenderer renderer = new ProcessNodeRenderer();

	RenderableProcessNode(Node node) {
		super(node);
	}

	@Override
	Shape backgroundShape() {
		return ShapeFactory.rectangle(getNode().getProp());
	}

	@Override
	ProcessNodeRenderer getRenderer() {
		return renderer;
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getProcessNode();
	}
}
