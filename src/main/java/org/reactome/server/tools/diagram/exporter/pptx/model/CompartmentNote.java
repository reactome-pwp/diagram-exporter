package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.Note;

import java.awt.*;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class CompartmentNote {

    private float width = 1;
    private float height = 1;
    private String displayName;
    private Coordinate textPosition;

    public CompartmentNote(Note note) {
        this.width = note.getProp().getWidth().floatValue();
        this.height = note.getProp().getHeight().floatValue();
        this.displayName = note.getDisplayName();
        this.textPosition = note.getTextPosition();
    }

    public void render(IShapeCollection shapes) {
        IAutoShape iAutoShape = shapes.addAutoShape(ShapeType.Rectangle, textPosition.getX().floatValue(), textPosition.getY().floatValue(), width, height);
        // line no fill , bg no fill
        iAutoShape.getLineFormat().getFillFormat().setFillType(FillType.NoFill);
        iAutoShape.getFillFormat().setFillType(FillType.NoFill);
        // Add TextFrame to the Rectangle
        iAutoShape.addTextFrame(" ");
        // Accessing the text frame
        ITextFrame txtFrame = iAutoShape.getTextFrame();
        // Create the Paragraph object for text frame
        IParagraph para = txtFrame.getParagraphs().get_Item(0);
        // Create Portion object for paragraph
        IPortion portion = para.getPortions().get_Item(0);
        portion.getPortionFormat().getFillFormat().setFillType(FillType.Solid);
        portion.getPortionFormat().getFillFormat().getSolidFillColor().setColor(Color.GRAY);
        portion.getPortionFormat().setFontHeight(12);
        portion.getPortionFormat().setFontBold(NullableBool.True);
        portion.setText(displayName);
    }
}
