package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.IAutoShape;
import com.aspose.slides.ILineFormat;
import com.aspose.slides.IShapeCollection;
import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.Node;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public abstract class PPTXNode {

    private Long id;
    private float x;
    private float y;
    private float width = 1;
    private float height = 1;

    private String displayName;
    private List<Connector> connectors;

    @SuppressWarnings("WeakerAccess")
    protected IAutoShape iAutoShape;
    PPTXShape pptxShape;
    Stoichiometry stoichiometry;
    LinkedList<PPTXSegment> segments; // or connectors

    public PPTXNode(Node node) {
        this.id = node.getId();
        this.x = node.getProp().getX().floatValue();
        this.y = node.getProp().getY().floatValue();
        this.width = node.getProp().getWidth().floatValue();
        this.height = node.getProp().getHeight().floatValue();
        this.displayName = node.getDisplayName();
        this.connectors = node.getConnectors();
    }

    public Long getId() {
        return id;
    }

    public IAutoShape getiAutoShape() {
        return iAutoShape;
    }

    public List<Connector> getConnectors() {
        return connectors != null ? connectors : new LinkedList<>();
    }

    public abstract void render(IShapeCollection shapes);

    final void render(IShapeCollection shapes, int shapeType, int lineWidth, byte lineStyle, byte lineFillStyle, Color lineColor, byte shapeFillType, Color fillColor) {
        iAutoShape = shapes.addAutoShape(shapeType, x, y, width, height);

        iAutoShape.getFillFormat().setFillType(shapeFillType);
        iAutoShape.getFillFormat().getSolidFillColor().setColor(fillColor);

        ILineFormat lineFormat = iAutoShape.getLineFormat();
        lineFormat.setWidth(lineWidth);
        lineFormat.setStyle(lineStyle);
        lineFormat.getFillFormat().setFillType(lineFillStyle);
        lineFormat.getFillFormat().getSolidFillColor().setColor(lineColor);

    }
}
