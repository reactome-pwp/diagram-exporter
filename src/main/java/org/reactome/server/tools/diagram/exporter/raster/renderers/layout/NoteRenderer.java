package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Note;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

import java.util.Collection;

/**
 * Notes only contain text.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class NoteRenderer extends AbstractRenderer {


	@Override
	public void text(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		final Collection<Note> nodes = (Collection<Note>) items;
		nodes.forEach(note -> TextRenderer.drawTextSingleLine(graphics, note.getDisplayName(), note.getTextPosition()));
	}

}
