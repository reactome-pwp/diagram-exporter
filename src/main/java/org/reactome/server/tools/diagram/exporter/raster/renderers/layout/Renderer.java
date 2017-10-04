package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

import java.awt.*;
import java.util.Collection;

public interface Renderer {

	void drawEnrichments(AdvancedGraphics2D graphics, DiagramObject item);

	void drawHitInteractors(AdvancedGraphics2D graphics, DiagramObject item);

	void drawExpression(AdvancedGraphics2D graphics, DiagramObject item);

	void draw(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items, Paint fillColor, Paint lineColor, Paint textColor, Stroke segmentStroke, Stroke borderStroke);
}
