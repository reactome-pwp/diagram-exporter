package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.IShapeCollection;
import com.aspose.slides.ShapeType;
import org.reactome.server.tools.diagram.data.layout.Node;

import java.awt.*;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@SuppressWarnings("ALL")
public class OtherEntity extends PPTXNode {

    private final int shapeType = ShapeType.Rectangle;

    private final Color lineColor = new Color(74, 149, 134);
    private final Color fillColor = new Color(141, 199, 187);

    public OtherEntity(Node node) {
        super(node);
    }

    @Override
    public void render(IShapeCollection shapes, ColourProfile colourProfile) {
        render(shapes, shapeType, colourProfile.get(OtherEntity.class));
    }
}
