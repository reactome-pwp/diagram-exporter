package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.Node;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public abstract class PPTXNode {

    private Long id;
    private float x;
    private float y;
    private float width = 1;
    private float height = 1;

    private Long reactomeId;
    private String schemaClass;
    private String displayName;
    private List<Connector> connectors;

    @SuppressWarnings("WeakerAccess")
    protected IAutoShape iAutoShape;
    protected IGroupShape iGroupShape;

    PPTXShape pptxShape;
    Stoichiometry stoichiometry;

    public PPTXNode(Node node) {
        this.reactomeId = node.getReactomeId();
        this.schemaClass = node.getSchemaClass();

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

    public List<Connector> getConnectors(Long edgeId, String type) {
        List<Connector> rtn = new ArrayList<>();
        for (Connector connector : getConnectors()) {
            if (connector.getEdgeId().equals(edgeId) && connector.getType().equals(type)) rtn.add(connector);
        }
        return rtn;
    }

    public abstract void render(IShapeCollection shapes);

    final void render(IShapeCollection shapes, int shapeType, int lineWidth, byte lineStyle, byte lineFillStyle, Color lineColor, byte shapeFillType, Color fillColor) {
        iGroupShape = shapes.addGroupShape();
        iAutoShape = iGroupShape.getShapes().addAutoShape(shapeType, x, y, width, height);

        iAutoShape.getFillFormat().setFillType(shapeFillType);
        iAutoShape.getFillFormat().getSolidFillColor().setColor(fillColor);

        ILineFormat lineFormat = iAutoShape.getLineFormat();
        lineFormat.setWidth(lineWidth);
        lineFormat.setStyle(lineStyle);
        lineFormat.getFillFormat().setFillType(lineFillStyle);
        lineFormat.getFillFormat().getSolidFillColor().setColor(lineColor);

        //Access ITextFrame associated with the AutoShape
        iAutoShape.addTextFrame("");

        ITextFrame txtFrame = iAutoShape.getTextFrame();

        IParagraph iParagraph = txtFrame.getParagraphs().get_Item(0);
        IPortion portion =  iParagraph.getPortions().get_Item(0);
        //Add some text to the frame
        portion.setText(displayName);
        portion.getPortionFormat().setFontHeight(8);
        portion.getPortionFormat().getHyperlinkManager().setExternalHyperlinkClick("http://www.reactome.org/content/detail/"+reactomeId); // that came with the example and I just added it :)

        // TODO: Set Margin is not working. Post in the forum
        iParagraph.getParagraphFormat().setMarginLeft(0.01f);
        iParagraph.getParagraphFormat().setMarginRight(0.01f);

    }

    @Override
    public String toString() {
        return schemaClass + "{" +
                "id=" + reactomeId +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}
