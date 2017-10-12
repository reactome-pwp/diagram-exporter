package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Note;
import org.reactome.server.tools.diagram.data.profile.analysis.AnalysisProfile;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.data.profile.interactors.InteractorProfile;
import org.reactome.server.tools.diagram.exporter.raster.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;

/**
 * Notes only contain text.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class NoteRenderer extends AbstractRenderer {

//
//	@Override
//	public void text(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
//		final Collection<Note> nodes = (Collection<Note>) items;
//		nodes.forEach(note -> TextRenderer.drawTextSingleLine(graphics, note.getDisplayName(), note.getTextPosition()));
//	}

	@Override
	public void draw(DiagramCanvas canvas, DiagramObject item, DiagramProfile diagramProfile, AnalysisProfile analysisProfile, InteractorProfile interactorProfile, double factor, DiagramIndex index) {
		final String text = diagramProfile.getNote().getText();
		final Note note = (Note) item;
		final Coordinate position = note.getPosition().multiply(factor);
		canvas.getNotes().add(text, item.getDisplayName(), position);
	}
}
