package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.common.profiles.model.DiagramProfile;

import java.awt.*;

import static org.reactome.server.tools.diagram.exporter.pptx.util.PPTXShape.*;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@SuppressWarnings("ALL")
public class Gene extends PPTXNode {

    private final int shapeType = ShapeType.Rectangle;
    private byte shapeFillType = FillType.Solid;
    private byte lineFillType = FillType.NoFill;
    private byte lineStyle = LineStyle.Single;

    // Shape that the connector will be connected. This is a simple line with 2 anchor points only
    private IAutoShape anchorShape;

    public Gene(Node node, Adjustment adjustment) {
        super(node, adjustment);
    }

    @Override
    public void render(IShapeCollection shapes, DiagramProfile profile) {
        Stylesheet stylesheet = new Stylesheet(profile.getGene(), shapeFillType, lineFillType, lineStyle);

        iGroupShape = shapes.addGroupShape();
        iGroupShape.setName("[GROUP] Gene");
        iAutoShape = iGroupShape.getShapes().addAutoShape(shapeType, x, y + width / 2, width, height - 20);

        setShapeStyle(iAutoShape, stylesheet);
        setTextFrame(iAutoShape, displayName, new double[]{0, 0, 0, 0}, stylesheet.getTextColor(), 8, true, true, reactomeId, adjustment);

        IAutoShape topThickLine = iGroupShape.getShapes().addAutoShape(ShapeType.Line, iAutoShape.getX(), iAutoShape.getY(), iAutoShape.getWidth() - 2, 0f);
        setShapeStyle(topThickLine, new Stylesheet().customStyle(3, LineStyle.Single, FillType.Solid, Color.BLACK, FillType.Solid, Color.BLACK, stylesheet.getLineDashStyle()));

        IAutoShape verticalLine = iGroupShape.getShapes().addAutoShape(ShapeType.Line, iAutoShape.getX() + iAutoShape.getWidth() - 17, iAutoShape.getY() - 20, 0, 20);
        setShapeStyle(verticalLine, new Stylesheet().customStyle(3, LineStyle.Single, FillType.Solid, Color.BLACK, FillType.Solid, Color.BLACK, stylesheet.getLineDashStyle()));

        anchorShape = iGroupShape.getShapes().addAutoShape(ShapeType.Line, verticalLine.getX() - 1, verticalLine.getY(), 20, 0);
        setShapeStyle(anchorShape, new Stylesheet().customStyle(3, LineStyle.Single, FillType.Solid, Color.BLACK, FillType.Solid, Color.BLACK, stylesheet.getLineDashStyle()));
        setEndArrowShape(anchorShape, LineArrowheadWidth.Wide, LineArrowheadLength.Medium, LineArrowheadStyle.Triangle);
    }

    public IAutoShape getAnchorShape() {
        return anchorShape;
    }
}
