package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.FillType;
import com.aspose.slides.IShapeCollection;
import com.aspose.slides.LineStyle;
import com.aspose.slides.ShapeType;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.common.profiles.model.DiagramProfile;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class EncapsulatedPathway extends PPTXNode {

    private final int shapeType = ShapeType.Rectangle;
    private byte shapeFillType = FillType.Solid;
    private byte lineFillType = FillType.Solid;
    private byte lineStyle = LineStyle.ThinThin;

    // TODO: APPLY
    double lineWidth = 10;

    public EncapsulatedPathway(Node node) {
        super(node);
    }

    @Override
    public void render(IShapeCollection shapes, DiagramProfile profile) {
        render(shapes, shapeType, new Stylesheet(profile.getProcessnode(), shapeFillType, lineFillType, lineStyle));


    }
}
