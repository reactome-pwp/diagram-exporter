package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.IAutoShape;
import com.aspose.slides.IGroupShape;
import com.aspose.slides.IShapeCollection;
import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.common.profiles.model.DiagramProfile;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.reactome.server.tools.diagram.exporter.pptx.util.PPTXShape.setShapeStyle;
import static org.reactome.server.tools.diagram.exporter.pptx.util.PPTXShape.setTextFrame;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public abstract class PPTXNode {

    @SuppressWarnings("WeakerAccess")
    protected IAutoShape iAutoShape;
    protected IGroupShape iGroupShape;
    private Long id;
    protected float x;
    protected float y;
    protected float width = 1;
    protected float height = 1;
    protected Long reactomeId;
    private String schemaClass;
    protected String displayName;
    private List<Connector> connectors;

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

    public IGroupShape getiGroupShape() {
        return iGroupShape;
    }

    public List<Connector> getConnectors() {
        return connectors != null ? connectors : new LinkedList<>();
    }

    public List<Connector> getConnectors(Long edgeId, String type) {
        return getConnectors().stream().filter(connector -> Objects.equals(connector.getEdgeId(), edgeId) && Objects.equals(connector.getType(), type)).collect(Collectors.toList());
    }

    public abstract void render(IShapeCollection shapes, DiagramProfile profile);

    final void render(IShapeCollection shapes, int shapeType, Stylesheet stylesheet) {
        iGroupShape = shapes.addGroupShape();
        iAutoShape = iGroupShape.getShapes().addAutoShape(shapeType, x, y, width, height);

        setShapeStyle(iAutoShape, stylesheet);
        setTextFrame(iAutoShape, displayName, new double[]{0,0,0,0}, stylesheet.getTextColor(), 10, true, true, reactomeId);
    }

    @Override
    public String toString() {
        return schemaClass + "{" +
                "id=" + reactomeId +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}
