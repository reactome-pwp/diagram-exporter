package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.EdgeCommon;
import org.reactome.server.tools.diagram.data.layout.Link;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ColorProfile;

import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class LinkRenderer extends EdgeRenderer {

	@Override
	public void segments(AdvancedGraphics2D graphics, Paint lineColor, Stroke stroke, Collection<? extends EdgeCommon> edges) {
		super.segments(graphics, lineColor, ColorProfile.DASHED_LINE_STROKE, edges);
	}

	@Override
	public void draw(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items, Paint fillColor, Paint lineColor, Paint textColor, Stroke borderStroke) {
		final Collection<Link> links = (Collection<Link>) items;
		// Links only draw their end shape
		final java.util.List<Shape> empty = new LinkedList<>();
		final java.util.List<Shape> nonEmpty = new LinkedList<>();
		links.forEach(edge -> {
			if (edge.getEndShape() != null) {
				final java.util.List<Shape> rendered = getScaledShapes(edge.getEndShape(), graphics.getFactor());
				if (edge.getEndShape().getEmpty() == null)
					nonEmpty.addAll(rendered);
				else
					empty.addAll(rendered);
			}
		});
		fill(graphics, fillColor, lineColor, empty, nonEmpty);
		empty.addAll(nonEmpty);
		border(graphics, lineColor, borderStroke, empty);
		text(graphics, lineColor, links);
	}
}
