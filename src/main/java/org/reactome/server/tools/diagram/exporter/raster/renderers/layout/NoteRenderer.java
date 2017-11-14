package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Note;
import org.reactome.server.tools.diagram.exporter.raster.diagram.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;

/**
 * Notes only contain text.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class NoteRenderer {

	public void draw(DiagramCanvas canvas, Note note, ColorProfiles colorProfiles) {
		final Color text = colorProfiles.getDiagramSheet().getNote().getText();
		canvas.getNotes().add(text, note.getDisplayName(), note.getTextPosition());
	}
}
