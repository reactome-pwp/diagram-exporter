package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeAttachment;
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
    private byte shapeFillType = FillType.Solid;
    private byte lineFillType = FillType.Solid;
    private byte lineStyle = LineStyle.Single;

    protected List<NodeAttachment> nodeAttachments;

    public Protein(Node node) {
        super(node);
        this.nodeAttachments = node.getNodeAttachments();
    }

    @Override
    public void render(IShapeCollection shapes, DiagramProfile profile) {
        Stylesheet stylesheet = new Stylesheet(profile.getProtein(), shapeFillType, lineFillType, lineStyle);
        render(shapes, shapeType, stylesheet);

        if(nodeAttachments!=null) {
            for (NodeAttachment nodeAttachment : nodeAttachments) {
                IAutoShape box = iGroupShape.getShapes().addAutoShape(
                        ShapeType.Rectangle,
                        nodeAttachment.getShape().getA().getX().floatValue(),
                        nodeAttachment.getShape().getA().getY().floatValue(),
                        nodeAttachment.getShape().getB().getX().floatValue() - nodeAttachment.getShape().getA().getX().floatValue(),
                        nodeAttachment.getShape().getB().getY().floatValue() - nodeAttachment.getShape().getA().getY().floatValue());

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
