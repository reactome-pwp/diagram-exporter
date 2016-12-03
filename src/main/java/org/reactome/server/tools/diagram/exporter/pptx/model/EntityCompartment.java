package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.Bound;
import org.reactome.server.tools.diagram.data.layout.Compartment;
import org.reactome.server.tools.diagram.data.layout.Coordinate;

import java.awt.*;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class EntityCompartment {

    private IAutoShape iAutoShape;
    private IAutoShape insetAutoShape;
    private float x;
    private float y;
    private float width = 1;
    private float height = 1;
    private String displayName;
    private Coordinate textPosition;
    private Bound insets = null;

    public EntityCompartment(Compartment compartment) {
        this.x = compartment.getProp().getX().floatValue();
        this.y = compartment.getProp().getY().floatValue();
        this.width = compartment.getProp().getWidth().floatValue();
        this.height = compartment.getProp().getHeight().floatValue();
        this.displayName = compartment.getDisplayName();
        this.textPosition = compartment.getTextPosition();
        this.insets = compartment.getInsets();
    }

    public void render(IShapeCollection shapes, ColourProfile colourProfile) {
        Stylesheet stylesheet = colourProfile.get(EntityCompartment.class);

        iAutoShape = shapes.addAutoShape(ShapeType.Rectangle, x, y, width, height);
        ILineFormat lineFormat = iAutoShape.getLineFormat();
        lineFormat.setWidth(4);
        lineFormat.setStyle(LineStyle.Single);
        lineFormat.getFillFormat().setFillType(FillType.Solid);
        lineFormat.getFillFormat().getSolidFillColor().setColor(stylesheet.getLineColor());
        IFillFormat fillFormat = iAutoShape.getFillFormat();
        fillFormat.setFillType(FillType.Solid);
        fillFormat.getSolidFillColor().setColor(stylesheet.getFillColor());

        if (insets != null) {
            insetAutoShape = shapes.addAutoShape(ShapeType.Rectangle, insets.getX().floatValue(), insets.getY().floatValue(), insets.getWidth().floatValue(), insets.getHeight().floatValue());
            ILineFormat insetLineFormat = insetAutoShape.getLineFormat();
            insetLineFormat.setWidth(4);
            insetLineFormat.setStyle(LineStyle.Single);
            insetLineFormat.getFillFormat().setFillType(FillType.Solid);
            insetLineFormat.getFillFormat().getSolidFillColor().setColor(stylesheet.getLineColor());
            IFillFormat insetFillFormat = insetAutoShape.getFillFormat();
            insetFillFormat.setFillType(FillType.Solid);
            insetFillFormat.getSolidFillColor().setColor(stylesheet.getFillColor());

            insetAutoShape.getAutoShapeLock().setSelectLocked(true);
            insetAutoShape.getAutoShapeLock().setSizeLocked(true);
        }

        iAutoShape.getAutoShapeLock().setSelectLocked(true);
        iAutoShape.getAutoShapeLock().setSizeLocked(true);

        addTextbox(shapes);
    }

    // TODO create it in PPTXShape
    private void addTextbox(IShapeCollection shapes) {
        IAutoShape textBox = shapes.addAutoShape(ShapeType.Rectangle, textPosition.getX().floatValue(), textPosition.getY().floatValue(), 110, 50);
        // line no fill , bg no fill
        textBox.getLineFormat().getFillFormat().setFillType(FillType.NoFill);
        textBox.getFillFormat().setFillType(FillType.NoFill);
        // Add TextFrame to the Rectangle
        textBox.addTextFrame(" ");
        // Accessing the text frame
        ITextFrame txtFrame = textBox.getTextFrame();
        // Create the Paragraph object for text frame
        IParagraph para = txtFrame.getParagraphs().get_Item(0);
        // Create Portion object for paragraph
        IPortion portion = para.getPortions().get_Item(0);
        portion.getPortionFormat().getFillFormat().setFillType(FillType.Solid);
        portion.getPortionFormat().getFillFormat().getSolidFillColor().setColor(new Color(0, 0, 255));
        portion.getPortionFormat().setFontHeight(10);
        portion.getPortionFormat().setFontBold(NullableBool.True);
        portion.setText(displayName);
    }
}
