package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.Note;
import org.reactome.server.tools.diagram.exporter.common.profiles.model.DiagramProfile;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class CompartmentNote {
    private float width = 1;
    private float height = 1;
    private String displayName;
    private Coordinate textPosition;
    private Stylesheet stylesheet;

    public CompartmentNote(Note note, DiagramProfile profile) {
        this.width = note.getProp().getWidth().floatValue();
        this.height = note.getProp().getHeight().floatValue();
        this.displayName = note.getDisplayName();
        this.textPosition = note.getTextPosition();
        this.stylesheet = new Stylesheet(profile.getNote());
    }

    public void render(IShapeCollection shapes) {
        IAutoShape iAutoShape = shapes.addAutoShape(ShapeType.Rectangle, textPosition.getX().floatValue(), textPosition.getY().floatValue(), width, height);
        iAutoShape.getLineFormat().getFillFormat().setFillType(FillType.NoFill);
        iAutoShape.getFillFormat().setFillType(FillType.NoFill);
        iAutoShape.addTextFrame(" ");
        ITextFrame txtFrame = iAutoShape.getTextFrame();
        IParagraph para = txtFrame.getParagraphs().get_Item(0);
        IPortion portion = para.getPortions().get_Item(0);
        portion.getPortionFormat().getFillFormat().setFillType(FillType.Solid);
        portion.getPortionFormat().getFillFormat().getSolidFillColor().setColor(stylesheet.getTextColor());
        portion.getPortionFormat().setFontHeight(12);
        portion.getPortionFormat().setFontBold(NullableBool.True);
        portion.setText(displayName);
    }
}
