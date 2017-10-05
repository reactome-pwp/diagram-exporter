package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Note;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

import java.awt.*;
import java.util.Collection;

/**
 * Notes only contain text.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class NoteRenderer extends AbstractRenderer {


	@Override
	public void draw(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items, Paint fillColor, Paint lineColor, Paint textColor, Stroke segmentStroke, Stroke borderStroke) {
		final Collection<Note> nodes = (Collection<Note>) items;
		graphics.getGraphics().setPaint(textColor);
		nodes.forEach(note -> graphics.drawTextSingleLine(note.getDisplayName(), note.getTextPosition()));
	}
}
