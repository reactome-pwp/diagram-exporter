package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.EdgeCommon;
import org.reactome.server.tools.diagram.data.layout.Link;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

import java.awt.*;
import java.util.Map;
import java.util.Set;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class LinkRenderer extends EdgeRenderer {


	private Map<Boolean, Set<Shape>> divideLinkShapes(Set<Link> links, AdvancedGraphics2D graphics) {
		return divide(graphics, links.stream()
				.map(EdgeCommon::getEndShape));
	}




}
