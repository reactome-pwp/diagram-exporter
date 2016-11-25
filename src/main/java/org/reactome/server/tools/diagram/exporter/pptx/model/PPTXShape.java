package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.IAutoShape;
import com.aspose.slides.IShapeCollection;
import com.aspose.slides.ShapeType;
import org.reactome.server.tools.diagram.data.layout.Shape;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class PPTXShape {

    public static IAutoShape renderShape(Shape rctShape, IShapeCollection shapes) {
        switch (rctShape.getType()) {
            case "CIRCLE":
                return shapes.addAutoShape(
                        ShapeType.Ellipse,
                        rctShape.getC().getX().floatValue(),
                        rctShape.getC().getY().floatValue(),
                        rctShape.getR().floatValue(),
                        rctShape.getR().floatValue()
                );
            case "DOUBLE_CIRCLE": //TODO: Please check how to do the double circle
                return shapes.addAutoShape(
                        ShapeType.Ellipse,
                        rctShape.getC().getX().floatValue(),
                        rctShape.getC().getY().floatValue(),
                        rctShape.getR().floatValue(),
                        rctShape.getR().floatValue()
                );
            case "BOX":
                return shapes.addAutoShape(
                        ShapeType.Rectangle,
                        rctShape.getA().getX().floatValue(),
                        rctShape.getA().getY().floatValue(),
                        rctShape.getB().getX().floatValue() - rctShape.getA().getX().floatValue(),
                        rctShape.getB().getY().floatValue() - rctShape.getA().getY().floatValue());
            default:
                throw new RuntimeException(rctShape.getType() + " hasn't been recognised.");
        }
    }
}
