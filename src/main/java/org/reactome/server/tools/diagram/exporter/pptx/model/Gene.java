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
public class Gene extends PPTXNode {

    private final int shapeType = ShapeType.Rectangle;
    private final byte shapeFillType = FillType.Solid;
    private final byte lineFillStyle = FillType.Solid;
    private final byte lineStyle = LineStyle.Single;
    private final int lineWidth = 4;
    private final Color lineColor = new Color(74, 149, 134);
    private final Color fillColor = new Color(141, 199, 187);

    public Gene(Node node) {
        super(node);
    }

    @Override
    public void render(IShapeCollection shapes) {
        render(shapes, shapeType, lineWidth, lineStyle, lineFillStyle, lineColor, shapeFillType, fillColor);
    }
}
