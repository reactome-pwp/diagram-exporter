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

@SuppressWarnings("ALL")
public class RNA extends PPTXNode {

    private final int shapeType = ShapeType.Rectangle;
    private byte shapeFillType = FillType.Solid;
    private byte lineFillType = FillType.Solid;
    private byte lineStyle = LineStyle.Single;

    public RNA(Node node) {
        super(node);
    }

    @Override
    public void render(IShapeCollection shapes, DiagramProfile profile) {
        Stylesheet stylesheet = new Stylesheet(profile.getRna(), shapeFillType, lineFillType, lineStyle);
        render(shapes, shapeType, stylesheet);

        // TODO Render a RNA shape
    }
}
