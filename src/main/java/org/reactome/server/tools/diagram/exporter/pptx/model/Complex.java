package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.Node;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@SuppressWarnings("ALL")
public class Complex extends PPTXNode {

    private final int shapeType = ShapeType.Octagon;

    // Shape that the connector will be connected. This is a simple rectangle with 4 anchor points only
    private IAutoShape anchorShape;

    public Complex(Node node) {
        super(node);
    }

    public IAutoShape getAnchorShape() {
        return anchorShape;
    }

    @Override
    public void render(IShapeCollection shapes, ColourProfile colourProfile) {
        render(shapes, shapeType, colourProfile.get(Complex.class));

        anchorShape = iGroupShape.getShapes().addAutoShape(ShapeType.Rectangle, iAutoShape.getX(), iAutoShape.getY(), iAutoShape.getWidth(), iAutoShape.getHeight());
        anchorShape.getFillFormat().setFillType(FillType.NoFill);
        anchorShape.getLineFormat().getFillFormat().setFillType(FillType.NoFill);

        IAdjustValueCollection adjustments = iAutoShape.getAdjustments();
        IAdjustValue adjustValue = null;
        if (adjustments != null && adjustments.size() > 0) {
            adjustValue = iAutoShape.getAdjustments().get_Item(0);
        }

        if (adjustValue != null) {
            // 0 to 50000 - Where 0 correspond to a value that will reduce the distance between two points such that a rectangle is formed
            adjustValue.setRawValue(14822);

            //0 to 0.833333
            adjustValue.setAngleValue(0.2470333f);
        }

        iGroupShape.getShapes().reorder(iGroupShape.getShapes().size()-1, iAutoShape);

    }

}
