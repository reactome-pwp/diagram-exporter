package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.Node;

import java.awt.*;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@SuppressWarnings("ALL")
public class Gene extends PPTXNode {

    private final int shapeType = ShapeType.Rectangle;

    // Shape that the connector will be connected. This is a simple line with 2 anchor points only
    private IAutoShape anchorShape;

    public Gene(Node node) {
        super(node);
    }

    @Override
    public void render(IShapeCollection shapes, ColourProfile colourProfile) {
        iGroupShape = shapes.addGroupShape();
        iAutoShape = iGroupShape.getShapes().addAutoShape(shapeType, x, y + width / 2, width, height - 20);

        PPTXShape.setShapeStyle(iAutoShape, colourProfile.get(Gene.class));
        PPTXShape.setTextFrame(iAutoShape, displayName, new double[]{0,0,0,0}, Color.BLACK, 8, true, true, reactomeId);

        IAutoShape topThickLine = iGroupShape.getShapes().addAutoShape(ShapeType.Line, iAutoShape.getX(), iAutoShape.getY(), iAutoShape.getWidth(), 0f);
        PPTXShape.setShapeStyle(topThickLine, new Stylesheet(3, LineStyle.Single, FillType.Solid, Color.black, FillType.Solid, Color.black));

        IAutoShape verticalLine = iGroupShape.getShapes().addAutoShape(ShapeType.Line, iAutoShape.getX() + iAutoShape.getWidth() - 17, iAutoShape.getY() - 20, 0, 20);
        PPTXShape.setShapeStyle(verticalLine, new Stylesheet(3, LineStyle.Single, FillType.Solid, Color.black, FillType.Solid, Color.black));

        anchorShape = iGroupShape.getShapes().addAutoShape(ShapeType.Line, verticalLine.getX() - 1, verticalLine.getY(), 20, 0);
        PPTXShape.setShapeStyle(anchorShape, new Stylesheet(3, LineStyle.Single, FillType.Solid, Color.black, FillType.Solid, Color.black));
        PPTXShape.setEndArrowShape(anchorShape, LineArrowheadWidth.Wide, LineArrowheadLength.Medium, LineArrowheadStyle.Triangle);
    }

    public IAutoShape getAnchorShape() {
        return anchorShape;
    }
}
