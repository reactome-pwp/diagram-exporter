package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.IAutoShape;
import com.aspose.slides.ILineFormat;
import com.aspose.slides.IShapeCollection;
import org.reactome.server.tools.diagram.data.layout.Node;

import java.awt.*;
import java.util.LinkedList;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public abstract class PPTXNode {

    Long id;
    float x;
    float y;
    float width = 1;
    float height = 1;
    String displayName;

    @SuppressWarnings("WeakerAccess")
    protected IAutoShape shape;
    PPTXShape pptxShape;
    Stoichiometry stoichiometry;
    LinkedList<PPTXSegment> segments; // or connectors

    public PPTXNode(Node node) {
        this.id = node.getId();
        this.x = node.getPosition().getX().floatValue();
        this.y = node.getPosition().getY().floatValue();
        this.width = node.getProp().getWidth().floatValue();
        this.height = node.getProp().getHeight().floatValue();
        this.displayName = node.getDisplayName();
    }

    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public abstract void render(IShapeCollection shapes);

    final void render(IShapeCollection shapes, int shapeType, int lineWidth, byte lineStyle, byte lineFillStyle, Color lineColor, byte shapeFillType, Color fillColor) {
        shape = shapes.addAutoShape(shapeType, x, y, width, height);

        shape.getFillFormat().setFillType(shapeFillType);
        shape.getFillFormat().getSolidFillColor().setColor(fillColor);

        ILineFormat lineFormat = shape.getLineFormat();
        lineFormat.setWidth(lineWidth);
        lineFormat.setStyle(lineStyle);
        lineFormat.getFillFormat().setFillType(lineFillStyle);
        lineFormat.getFillFormat().getSolidFillColor().setColor(lineColor);

    }
}
