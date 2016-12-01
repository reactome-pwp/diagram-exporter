package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.Node;

import java.awt.*;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@SuppressWarnings("ALL")
public class Complex extends PPTXNode {

    private final int shapeType = ShapeType.Octagon;
    private final byte shapeFillType = FillType.Solid;
    private final byte lineFillStyle = FillType.Solid;
    private final byte lineStyle = LineStyle.Single;
    private final int lineWidth = 4;
    private final Color lineColor = new Color(31, 136, 167);
    private final Color fillColor = new Color(171, 209, 227);

    private IAutoShape invisibleShape;

    public Complex(Node node) {
        super(node);
    }

    public IAutoShape getInvisibleShape() {
        return invisibleShape;
    }

    @Override
    public void render(IShapeCollection shapes) {
        render(shapes, shapeType, lineWidth, lineStyle, lineFillStyle, lineColor, shapeFillType, fillColor);

        // TODO anchor point to the outter box
        invisibleShape = iGroupShape.getShapes().addAutoShape(ShapeType.Rectangle, iAutoShape.getX(), iAutoShape.getY(), iAutoShape.getWidth(), iAutoShape.getHeight());
        invisibleShape.getFillFormat().setFillType(FillType.NoFill);
        invisibleShape.getLineFormat().getFillFormat().setFillType(FillType.NoFill);

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
