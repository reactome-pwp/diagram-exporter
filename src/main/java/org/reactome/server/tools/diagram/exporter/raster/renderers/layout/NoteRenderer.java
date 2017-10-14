package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Note;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.raster.DiagramCanvas;

/**
 * Notes only contain text.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class NoteRenderer {

	public void draw(DiagramCanvas canvas, Note note, DiagramProfile diagramProfile) {
		final String text = diagramProfile.getNote().getText();
		canvas.getNotes().add(text, note.getDisplayName(), note.getTextPosition());
	}
}
