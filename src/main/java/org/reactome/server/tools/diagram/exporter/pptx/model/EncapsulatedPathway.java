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

public class EncapsulatedPathway extends PPTXNode {

    private final int shapeType = ShapeType.Rectangle;
    private final byte shapeFillType = FillType.Solid;
    private final byte lineFillStyle = FillType.Solid;
    private final byte lineStyle = LineStyle.ThinThin;
    private final int lineWidth = 10;
    private final Color lineColor = new Color(165, 215, 145);
    private final Color fillColor = Color.WHITE;

    public EncapsulatedPathway(Node node) {
        super(node);
    }

    @Override
    public void render(IShapeCollection shapes) {
        render(shapes, shapeType, lineWidth, lineStyle, lineFillStyle, lineColor, shapeFillType, fillColor);
    }
}
