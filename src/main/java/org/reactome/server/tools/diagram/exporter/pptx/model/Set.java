package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.FillType;
import com.aspose.slides.IShapeCollection;
import com.aspose.slides.LineStyle;
import com.aspose.slides.ShapeType;
import org.reactome.server.tools.diagram.data.layout.Node;

import java.awt.*;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@SuppressWarnings("ALL")
public class Set extends PPTXNode {

    private final int shapeType = ShapeType.RoundCornerRectangle;
    private final byte shapeFillType = FillType.Solid;
    private final byte lineFillStyle = FillType.Solid;
    private final byte lineStyle = LineStyle.ThinThin;
    private final int lineWidth = 10;
    private final Color lineColor = new Color(106, 106, 227);
    private final Color fillColor = new Color(160, 187, 205);

    public Set(Node node) {
        super(node);
    }

    @Override
    public void render(IShapeCollection shapes) {
        render(shapes, shapeType, lineWidth, lineStyle, lineFillStyle, lineColor, shapeFillType, fillColor);
    }
}
