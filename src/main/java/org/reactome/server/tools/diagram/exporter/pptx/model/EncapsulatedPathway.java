package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.IShapeCollection;
import com.aspose.slides.ShapeType;
import org.reactome.server.tools.diagram.data.layout.Node;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class EncapsulatedPathway extends PPTXNode {

    private final int shapeType = ShapeType.Rectangle;

    public EncapsulatedPathway(Node node) {
        super(node);
    }

    @Override
    public void render(IShapeCollection shapes, ColourProfile colourProfile) {
        render(shapes, shapeType, colourProfile.get(EncapsulatedPathway.class));
    }
}
