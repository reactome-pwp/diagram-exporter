package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Note;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;

public class NoteRenderer extends AbstractRenderer {

//	@Override
//	public void fill(AdvancedGraphics2D graphics, DiagramObject item) {
//		final Note note = (Note) item;
//		graphics.fillRectangle(note.getProp());
//	}
//
//	@Override
//	public void draw(AdvancedGraphics2D graphics, DiagramObject item) {
//		final Note note = (Note) item;
//		graphics.drawRectangle(note.getProp());
//	}

	@Override
	public void drawText(AdvancedGraphics2D graphics, DiagramObject item) {
		final Note note = (Note) item;
		graphics.drawTextSingleLine(note.getDisplayName(), note.getTextPosition());
	}
}
