package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.common.profiles.model.DiagramProfile;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@SuppressWarnings("ALL")
public class Chemical extends PPTXNode {

    private final int shapeType = ShapeType.Ellipse;
    private final byte shapeFillType = FillType.Solid;
    private final byte lineFillType = FillType.Solid;
    private final byte lineStyle = LineStyle.Single;

    // Shape that the connector will be connected. This is a simple rectangle with 4 anchor points only
    private IAutoShape anchorShape;

    public Chemical(Node node) {
        super(node);
    }

    public IAutoShape getAnchorShape() {
        return anchorShape;
    }

    @Override
    public void render(IShapeCollection shapes, DiagramProfile profile) {
        Stylesheet stylesheet = new Stylesheet(profile.getChemical(), shapeFillType, lineFillType, lineStyle);

        render(shapes, shapeType, stylesheet);

        anchorShape = iGroupShape.getShapes().addAutoShape(ShapeType.Rectangle, iAutoShape.getX(), iAutoShape.getY(), iAutoShape.getWidth(), iAutoShape.getHeight());
        anchorShape.getFillFormat().setFillType(FillType.NoFill);
        anchorShape.getLineFormat().getFillFormat().setFillType(FillType.NoFill);

        //reorder
        iGroupShape.getShapes().reorder(iGroupShape.getShapes().size() - 1, iAutoShape);
    }
}
