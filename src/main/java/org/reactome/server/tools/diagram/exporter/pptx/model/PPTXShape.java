package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.IAutoShape;
import com.aspose.slides.IGroupShape;
import com.aspose.slides.IShapeCollection;
import com.aspose.slides.ShapeType;
import org.reactome.server.tools.diagram.data.layout.Shape;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class PPTXShape {

    public static IAutoShape renderShape(IShapeCollection shapes, Shape rctShape) {
        switch (rctShape.getType()) {
            case "CIRCLE":
                float correction = rctShape.getR().floatValue();
                return shapes.addAutoShape(
                        ShapeType.Ellipse,
                        rctShape.getC().getX().floatValue() - correction,
                        rctShape.getC().getY().floatValue() - correction,
                        rctShape.getR().floatValue() + correction,
                        rctShape.getR().floatValue() + correction
                );
            case "DOUBLE_CIRCLE": //TODO: Please check how to do the double circle
                correction = rctShape.getR().floatValue();
                return shapes.addAutoShape(
                        ShapeType.Ellipse,
                        rctShape.getC().getX().floatValue() - correction,
                        rctShape.getC().getY().floatValue() - correction,
                        rctShape.getR().floatValue() + correction,
                        rctShape.getR().floatValue() + correction
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

    public static IAutoShape renderShape(IGroupShape group, Shape rctShape) {
        switch (rctShape.getType()) {
            case "CIRCLE":
                float correction = rctShape.getR().floatValue();
                return group.getShapes().addAutoShape(
                        ShapeType.Ellipse,
                        rctShape.getC().getX().floatValue() - correction,
                        rctShape.getC().getY().floatValue() - correction,
                        rctShape.getR().floatValue() + correction,
                        rctShape.getR().floatValue() + correction
                );
            case "DOUBLE_CIRCLE": //TODO: Please check how to do the double circle
                correction = rctShape.getR().floatValue();
                return group.getShapes().addAutoShape(
                        ShapeType.Ellipse,
                        rctShape.getC().getX().floatValue() - correction,
                        rctShape.getC().getY().floatValue() - correction,
                        rctShape.getR().floatValue() + correction,
                        rctShape.getR().floatValue() + correction
                );
            case "BOX":
                return group.getShapes().addAutoShape(
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
