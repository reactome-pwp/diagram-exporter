package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.awt.*;

public class RenderableEncapsulatedNode extends RenderableProcessNode {

	RenderableEncapsulatedNode(Node node) {
		super(node);
	}

	@Override
	protected Shape backgroundShape() {
		return ShapeFactory.hexagon(getDiagramObject().getProp());
	}

	@Override
	Shape innerShape() {
		return ShapeFactory.hexagon(getNode().getProp(), PROCESS_NODE_PADDING);
	}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getEncapsulatedNode();
	}

}
