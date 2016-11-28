package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.Shape;

import java.awt.*;

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
                IAutoShape circle = group.getShapes().addAutoShape(
                        ShapeType.Ellipse,
                        rctShape.getC().getX().floatValue() - correction,
                        rctShape.getC().getY().floatValue() - correction,
                        rctShape.getR().floatValue() + correction,
                        rctShape.getR().floatValue() + correction
                );
                circle.getFillFormat().setFillType(FillType.Solid);
                circle.getFillFormat().getSolidFillColor().setColor(Color.BLACK);
                circle.getLineFormat().getFillFormat().getSolidFillColor().setColor(Color.black);
                circle.getLineFormat().getFillFormat().setFillType(FillType.Solid);
                return circle;

            case "DOUBLE_CIRCLE": //TODO: Please check how to do the double circle
                correction = rctShape.getR().floatValue();
                IAutoShape dCircle = group.getShapes().addAutoShape(
                        ShapeType.Ellipse,
                        rctShape.getC().getX().floatValue() - correction,
                        rctShape.getC().getY().floatValue() - correction,
                        rctShape.getR().floatValue() + correction,
                        rctShape.getR().floatValue() + correction
                );
                dCircle.getLineFormat().setStyle(LineStyle.ThinThin);
                dCircle.getFillFormat().setFillType(FillType.Solid);
                dCircle.getFillFormat().getSolidFillColor().setColor(Color.WHITE);
                dCircle.getLineFormat().getFillFormat().setFillType(FillType.Solid);
                return dCircle;
            case "BOX":
                IAutoShape box = group.getShapes().addAutoShape(
                        ShapeType.Rectangle,
                        rctShape.getA().getX().floatValue(),
                        rctShape.getA().getY().floatValue(),
                        rctShape.getB().getX().floatValue() - rctShape.getA().getX().floatValue(),
                        rctShape.getB().getY().floatValue() - rctShape.getA().getY().floatValue());
                box.getFillFormat().setFillType(FillType.Solid);
                box.getFillFormat().getSolidFillColor().setColor(Color.WHITE);
                box.getLineFormat().getFillFormat().getSolidFillColor().setColor(Color.black);
                box.getLineFormat().getFillFormat().setFillType(FillType.Solid);
                box.getLineFormat().setWidth(1);

                if(rctShape.getS() != null) {
                    box.addTextFrame(" ");
                    ITextFrame txtFrame = box.getTextFrame();
                    IParagraph iParagraph = txtFrame.getParagraphs().get_Item(0);
                    IPortion portion = iParagraph.getPortions().get_Item(0);
                    portion.getPortionFormat().getFillFormat().setFillType(FillType.Solid);
                    portion.getPortionFormat().getFillFormat().getSolidFillColor().setColor(Color.black);
                    portion.getPortionFormat().setFontHeight(10);
                    portion.getPortionFormat().setFontBold(NullableBool.True);
                    // TODO: Set Margin is not working. Post in the forum
                    iParagraph.getParagraphFormat().setMarginLeft(0.01f);
                    iParagraph.getParagraphFormat().setMarginRight(0.01f);
                    //portion.getCoordinates().setLocation(comp.getTextPosition().getX(), comp.getTextPosition().getY());
                    portion.setText(rctShape.getS());
                }
                return box;
            default:
                throw new RuntimeException(rctShape.getType() + " hasn't been recognised.");
        }
    }

    /**
     * Retrieve IAutoShape used as a support for drawing connectors, which are meant for connecting shapes only.
     *
     * @return formatted IAutoShape - No line and No fill
     */
    public static IAutoShape renderAuxiliarShape(IGroupShape group, Coordinate edgeCoordinate) {
        IAutoShape auxShape = group.getShapes().addAutoShape(
                                        ShapeType.Ellipse,
                                        edgeCoordinate.getX().floatValue(),
                                        edgeCoordinate.getY().floatValue(),
                                        1f,
                                        1f
                                );

        auxShape.getLineFormat().getFillFormat().setFillType(FillType.NoFill);
        auxShape.getFillFormat().setFillType(FillType.NoFill);

        return auxShape;
    }

    /**
     * Retrieve IAutoShape used as a support for drawing connectors, which are meant for connecting shapes only.
     *
     * @param shapes collection of shapes present in the Slides
     * @return
     */
    public static IAutoShape renderAuxiliarShape(IShapeCollection shapes, Coordinate edgeCoordinate) {
        IAutoShape auxShape = shapes.addAutoShape(
                                    ShapeType.Ellipse,
                                    edgeCoordinate.getX().floatValue(),
                                    edgeCoordinate.getY().floatValue(),
                                    1f,
                                    1f
                                );

        auxShape.getLineFormat().getFillFormat().setFillType(FillType.NoFill);
        auxShape.getFillFormat().setFillType(FillType.NoFill);

        return auxShape;
    }

    public static boolean touches(Shape shape, Coordinate c) {
        Coordinate centre;
        switch (shape.getType()) {
            case "CIRCLE":
            case "DOUBLE_CIRCLE":
                centre = shape.getC();
                return distance(centre, c) < shape.getR();
            case "BOX":
                Coordinate diff = shape.getB().minus(shape.getA());
                centre = shape.getA().add(diff.divide(2.0));
                return distance(centre, c) < diff.getX();
            default:
                throw new RuntimeException(shape.getType() + " hasn't been recognised.");
        }
    }

    private static double distance(Coordinate c1, Coordinate c2) {
        Coordinate diff = c2.minus(c1);
        return Math.sqrt(diff.getX() * diff.getX() + diff.getY() * diff.getY());
    }
}
