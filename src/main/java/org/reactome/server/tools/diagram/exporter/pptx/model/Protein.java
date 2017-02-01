package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeAttachment;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.common.profiles.model.DiagramProfile;

import java.util.List;

import static org.reactome.server.tools.diagram.exporter.pptx.util.PPTXShape.setShapeStyle;
import static org.reactome.server.tools.diagram.exporter.pptx.util.PPTXShape.setTextFrame;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@SuppressWarnings("ALL")
public class Protein extends PPTXNode {

    private final int shapeType = ShapeType.RoundCornerRectangle;
    protected List<NodeAttachment> nodeAttachments;
    private byte shapeFillType = FillType.Solid;
    private byte lineFillType = FillType.Solid;
    private byte lineStyle = LineStyle.Single;

    public Protein(Node node, Adjustment adjustment) {
        super(node, adjustment);
        this.nodeAttachments = node.getNodeAttachments();
    }

    @Override
    public void render(IShapeCollection shapes, DiagramProfile profile) {
        Stylesheet stylesheet = new Stylesheet(profile.getProtein(), shapeFillType, lineFillType, lineStyle);
        render(shapes, shapeType, stylesheet);

        if (nodeAttachments != null) {
            for (NodeAttachment nodeAttachment : nodeAttachments) {
                float w = nodeAttachment.getShape().getB().getX().floatValue() - nodeAttachment.getShape().getA().getX().floatValue();
                float h = nodeAttachment.getShape().getB().getY().floatValue() - nodeAttachment.getShape().getA().getY().floatValue();
                NodeProperties nodeProp = NodePropertiesFactory.get(nodeAttachment.getShape().getA().getX().floatValue(), nodeAttachment.getShape().getA().getY().floatValue(), w, h);
                NodeProperties np = NodePropertiesFactory.transform(nodeProp, adjustment.getFactor(), adjustment.getCoordinate());
                IAutoShape box = iGroupShape.getShapes().addAutoShape(
                        ShapeType.Rectangle,
                        np.getX().floatValue(),
                        np.getY().floatValue(),
                        np.getWidth().floatValue(),
                        np.getHeight().floatValue()
                );

                setShapeStyle(box, stylesheet);

                if (nodeAttachment.getLabel() != null) {
                    setTextFrame(box, nodeAttachment.getLabel(), new double[]{0, 0, 0, 0}, stylesheet.getTextColor(), 8, true, false, null);
                }

                // block the nodeattachments to be selected :)
                box.getAutoShapeLock().setSelectLocked(true);
            }
        }
    }
}
