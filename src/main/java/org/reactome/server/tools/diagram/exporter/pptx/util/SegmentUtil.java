package org.reactome.server.tools.diagram.exporter.pptx.util;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.exporter.pptx.model.*;

import java.awt.*;

import static org.reactome.server.tools.diagram.exporter.pptx.util.PPTXShape.setBeginArrowShape;
import static org.reactome.server.tools.diagram.exporter.pptx.util.PPTXShape.setShapeStyle;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class SegmentUtil {

    public static void drawSegment(IShapeCollection shapes, IShape start, IShape end) {
        drawSegment(shapes, start, end, false);
    }

    /**
     * Draw segments can connect a shape to an auxiliary shape, a shape to a backbone start or end
     * and an auxiliary shape to another auxiliary shape.
     *
     * @param shapes the collection of shapes present in the slide
     * @param start  start node to be connected
     * @param end    end node to be connected
     */
    public static void drawSegment(IShapeCollection shapes, IShape start, IShape end, boolean dashed) {
        IConnector connector = shapes.addConnector(ShapeType.StraightConnector1, start.getX(), start.getY(), 1, 1, true);
        setShapeStyle(connector, new Stylesheet(1, LineStyle.Single, FillType.Solid, Color.BLACK, FillType.NotDefined, null));
        connector.setStartShapeConnectedTo(start);
        connector.getLineFormat().setCapStyle(LineCapStyle.Round);
        connector.setEndShapeConnectedTo(end);

        if (dashed) {
            connector.getLineFormat().getFillFormat().setFillType(FillType.Solid);
            connector.getLineFormat().setDashStyle(LineDashStyle.Dash);
            connector.getLineFormat().setStyle(LineStyle.Single);
            connector.getLineFormat().setWidth(1.25);
        }

        connector.reroute();
    }

    public static void connect(IShapeCollection shapes, PPTXNode pptxNode, IShape start, IShape end, boolean renderArrow) {
        connect(shapes, pptxNode, start, end, renderArrow, false);
    }

    /**
     * Connectors always go from Node to Backbone/Catalyst/ or Node to Auxiliary shape
     * <p>
     * In order to draw segments only please refer {@link #drawSegment(IShapeCollection, IShape, IShape)}
     *
     * @param shapes      the collection of shapes present in the slide
     * @param pptxNode    instance PPTXNode, used for special cases like Chemicals or Complexes
     * @param start       start point of a connector
     * @param end         end point of a connector
     * @param renderArrow flag to render an arrow and beginning of a connector
     */
    public static void connect(IShapeCollection shapes, PPTXNode pptxNode, IShape start, IShape end, boolean renderArrow, boolean dashed) {
        IConnector connector = shapes.addConnector(ShapeType.StraightConnector1, start.getX(), start.getY(), 1, 1, true);
        setShapeStyle(connector, new Stylesheet(1, LineStyle.Single, FillType.Solid, Color.BLACK, FillType.NotDefined, null));
        connector.getLineFormat().setCapStyle(LineCapStyle.Round);
        if (renderArrow) {
            // in this case, our direction is from NODE to backbone or segment, then the arrow
            // should be in the NODE as begin arrow.
            setBeginArrowShape(connector, LineArrowheadLength.Long, LineArrowheadStyle.Triangle, LineArrowheadWidth.Wide);
        }

        if (dashed) {
            connector.getLineFormat().getFillFormat().setFillType(FillType.Solid);
            connector.getLineFormat().setDashStyle(LineDashStyle.Dash);
            connector.getLineFormat().setStyle(LineStyle.Single);
            connector.getLineFormat().setWidth(1.25);
        }

        // Some implementations of PPTXNode have an anchorShape shape to ease and beautify where the anchor point
        // is attached
        int anchorPoint;
        if (pptxNode instanceof Complex) {
            start = ((Complex) pptxNode).getAnchorShape();
            anchorPoint = getAnchorPoint(start, end);
        } else if (pptxNode instanceof Chemical) {
            start = ((Chemical) pptxNode).getAnchorShape();
            anchorPoint = getAnchorPoint(start, end);
        } else if (pptxNode instanceof Gene) {
            start = ((Gene) pptxNode).getAnchorShape();
            anchorPoint = 1;
        } else {
            anchorPoint = getAnchorPoint(start, end);
        }

        connector.setStartShapeConnectedTo(start);
        connector.setStartShapeConnectionSiteIndex(anchorPoint);
        connector.setEndShapeConnectedTo(end);
    }

    private static int getAnchorPoint(IShape node, IShape backbone) {
        boolean bottom = (node.getY() + node.getHeight()) <= (int) backbone.getY();
        boolean top = (node.getY()) >= (int) backbone.getY();
        boolean left = (int) backbone.getX() <= node.getX();
        boolean right = (node.getX() + node.getHeight()) <= backbone.getX();

        if (top) return 0;
        if (left) return 1;
        if (bottom) return 2;
        if (right) return 3;

        return 0;
    }
}
