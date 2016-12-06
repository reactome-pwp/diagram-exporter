package org.reactome.server.tools.diagram.exporter.pptx.util;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.exporter.pptx.model.PPTXNode;
import org.reactome.server.tools.diagram.exporter.pptx.model.Stylesheet;

import java.awt.*;
import java.util.Collection;

import org.reactome.server.tools.diagram.data.layout.Shape;

/**
 * This class is concentrating all rendering and Styling shapes.
 * Do not style
 *
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class PPTXShape {

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
                setShapeStyle(circle, new Stylesheet(1, LineStyle.NotDefined, FillType.Solid, Color.BLACK, FillType.Solid, Color.BLACK));

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
                setShapeStyle(dCircle, new Stylesheet(1, LineStyle.ThinThin, FillType.Solid, Color.BLACK,FillType.Solid, Color.BLACK));

                return dCircle;
            case "BOX":
                IAutoShape box = group.getShapes().addAutoShape(
                        ShapeType.Rectangle,
                        rctShape.getA().getX().floatValue(),
                        rctShape.getA().getY().floatValue(),
                        rctShape.getB().getX().floatValue() - rctShape.getA().getX().floatValue(),
                        rctShape.getB().getY().floatValue() - rctShape.getA().getY().floatValue());
//                FillType.Solid, Color.WHITE, FillType.Solid, Color.BLACK, LineStyle.NotDefined, 1
                setShapeStyle(box, new Stylesheet(1, LineStyle.NotDefined, FillType.Solid, Color.BLACK, FillType.Solid, Color.WHITE));

                if (rctShape.getS() != null) {
                    setTextFrame(box, rctShape.getS(), new double[]{0,0,0,0}, Color.BLACK, 6, true, false, null);
                }

                return box;
            default:
                throw new RuntimeException(rctShape.getType() + " hasn't been recognised.");
        }
    }

    /**
     * Retrieve IAutoShape used as a support for drawing connectors, which are meant for connecting shapes only.
     *
     * @return formatted IAutoShape - No line and No fill as part of group shape
     */
    public static IAutoShape renderAuxiliaryShape(IGroupShape group, Coordinate edgeCoordinate) {
        IAutoShape auxShape = group.getShapes().addAutoShape(
                ShapeType.Ellipse,
                edgeCoordinate.getX().floatValue(),
                edgeCoordinate.getY().floatValue(),
                0.1f,
                0.1f
        );

        auxShape.getLineFormat().getFillFormat().setFillType(FillType.NoFill);
        auxShape.getFillFormat().setFillType(FillType.NoFill);
//        auxShape.getFillFormat().setFillType(FillType.Solid);
//        auxShape.getFillFormat().getSolidFillColor().setColor(Color.BLACK);

//        group.reorder(shapes.size()-1, auxShape);

        return auxShape;
    }

    /**
     * Retrieve IAutoShape used as a support for drawing connectors, which are meant for connecting shapes only.
     *
     * @param shapes collection of shapes present in the Slides
     * @return rendered IAutoShape as it is
     */
    public static IAutoShape renderAuxiliaryShape(IShapeCollection shapes, Coordinate edgeCoordinate) {
        IAutoShape auxShape = shapes.addAutoShape(
                ShapeType.Ellipse,
                edgeCoordinate.getX().floatValue(),
                edgeCoordinate.getY().floatValue(),
                0.1f,
                0.1f
        );

        auxShape.getLineFormat().getFillFormat().setFillType(FillType.NoFill);
        auxShape.getFillFormat().setFillType(FillType.Solid);
        auxShape.getFillFormat().getSolidFillColor().setColor(Color.BLACK);

        return auxShape;
    }

    /**
     * Retrieve IAutoShape used as a support for drawing connectors, which are meant for connecting shapes only.
     *
     * @return rendered IAutoShape as it is
     */
    // TEST, remove if it is not in use
    public static IAutoShape renderAuxiliaryShape(IGroupShape group, float x, float y) {
        IAutoShape auxShape = group.getShapes().addAutoShape(ShapeType.Ellipse, x, y, 0.1f, 0.1f);
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

    /**
     * Helper method to ease set the shape Style.
     *
     * @param shape        the given shape to apply the style
     */
    public static void setShapeStyle(IShape shape, Stylesheet stylesheet) {
        // Fill properties
        shape.getFillFormat().setFillType(stylesheet.getShapeFillType());
        shape.getFillFormat().getSolidFillColor().setColor(stylesheet.getFillColor());

        // Line properties
        shape.getLineFormat().getFillFormat().setFillType(stylesheet.getLineFillType());
        shape.getLineFormat().getFillFormat().getSolidFillColor().setColor(stylesheet.getLineColor());
        shape.getLineFormat().setStyle(stylesheet.getLineStyle());
        shape.getLineFormat().setWidth(stylesheet.getLineWidth());
    }

    /**
     * Helper method to ease set the Begin Arrow style
     */
    static void setBeginArrowShape(IShape shape, byte arrowHeadLength, byte arrowHeadStyle, byte arrowHeadWidth) {
        shape.getLineFormat().setBeginArrowheadLength(arrowHeadLength);
        shape.getLineFormat().setBeginArrowheadStyle(arrowHeadStyle);
        shape.getLineFormat().setBeginArrowheadWidth(arrowHeadWidth);
    }

    /**
     * Helper method to ease set the Begin Arrow style
     */
    public static void setEndArrowShape(IShape shape, byte arrowHeadLength, byte arrowHeadStyle, byte arrowHeadWidth) {
        shape.getLineFormat().setEndArrowheadLength(arrowHeadLength);
        shape.getLineFormat().setEndArrowheadStyle(arrowHeadStyle);
        shape.getLineFormat().setEndArrowheadWidth(arrowHeadWidth);
    }

    /**
     * Helper method to ease set text properties, link, margin.
     *
     * @param margins 0-top, 1-left, 2-bottom, 3-right
     * @param addHyperlink flag to add a hyperlink
     * @param stId identifier to build the link
     */
    public static void setTextFrame(IAutoShape shape,
                             String text,
                             double[] margins,
                             Color fontColor,
                             float fontHeight,
                             boolean bold,
                             boolean addHyperlink,
                             Long stId) {

        shape.addTextFrame(" ");

        ITextFrame txtFrame = shape.getTextFrame();
        txtFrame.getTextFrameFormat().setMarginTop(margins[0]);
        txtFrame.getTextFrameFormat().setMarginLeft(margins[1]);
        txtFrame.getTextFrameFormat().setMarginBottom(margins[2]);
        txtFrame.getTextFrameFormat().setMarginRight(margins[3]);

        IPortion portion = txtFrame.getParagraphs().get_Item(0).getPortions().get_Item(0);
        portion.getPortionFormat().setFontHeight(fontHeight);
        portion.getPortionFormat().getFillFormat().setFillType(FillType.Solid);

        if (bold) portion.getPortionFormat().setFontBold(NullableBool.True);

        if (addHyperlink) {
            if(stId != null && stId >= 0) { // Avoiding wrong links to be generated
                // Having the link is not possible to change the font color
                portion.getPortionFormat().getHyperlinkManager().setExternalHyperlinkClick("http://www.reactome.org/content/detail/" + stId);
            }
        }

        portion.getPortionFormat().getFillFormat().getSolidFillColor().setColor(fontColor);
        portion.setText(text);
    }

    /**
     * Render the Activator Shape. PowerPoint does not support the shape we need to achieve. So we are just
     * following the coordinates of an arrow end shape.
     *
     * @return the centre shape to attach the anchor Point
     */
    public static IAutoShape renderActivatorShape(IShapeCollection shapes, IGroupShape groupShape, Shape shape) {
        IAutoShape a = groupShape.getShapes().addAutoShape(
                ShapeType.Ellipse,
                shape.getA().getX().floatValue(),
                shape.getA().getY().floatValue(),
                0.1f,
                0.1f
        );
        a.getLineFormat().getFillFormat().setFillType(FillType.NoFill);
        a.getFillFormat().setFillType(FillType.Solid);
        a.getFillFormat().getSolidFillColor().setColor(Color.YELLOW);

        IAutoShape b = groupShape.getShapes().addAutoShape(
                ShapeType.Ellipse,
                shape.getB().getX().floatValue(),
                shape.getB().getY().floatValue(),
                0.1f,
                0.1f
        );
        b.getLineFormat().getFillFormat().setFillType(FillType.NoFill);
        b.getFillFormat().setFillType(FillType.Solid);
        b.getFillFormat().getSolidFillColor().setColor(Color.RED);

        IAutoShape c = groupShape.getShapes().addAutoShape(
                ShapeType.Ellipse,
                shape.getC().getX().floatValue(),
                shape.getC().getY().floatValue(),
                0.1f,
                0.1f
        );
        c.getLineFormat().getFillFormat().setFillType(FillType.NoFill);
        c.getFillFormat().setFillType(FillType.Solid);
        c.getFillFormat().getSolidFillColor().setColor(Color.BLUE);

        SegmentUtil.drawSegment(shapes, a, b);
        SegmentUtil.drawSegment(shapes, b, c);
        SegmentUtil.drawSegment(shapes, c, a);

        float x = (shape.getA().getX().floatValue() + shape.getC().getX().floatValue()) / 2f;
        float y = (shape.getA().getY().floatValue() + shape.getC().getY().floatValue()) / 2f;

        IAutoShape centre = renderAuxiliaryShape(groupShape, x, y);
        centre.getLineFormat().getFillFormat().setFillType(FillType.NoFill);
        centre.getFillFormat().setFillType(FillType.NoFill);

        return centre;
    }

    public static void reorder (IShapeCollection shapes, Collection<PPTXNode> nodes){
        for (PPTXNode node : nodes) {
            shapes.reorder(shapes.size() - 1, node.getiGroupShape());
        }
    }
}
