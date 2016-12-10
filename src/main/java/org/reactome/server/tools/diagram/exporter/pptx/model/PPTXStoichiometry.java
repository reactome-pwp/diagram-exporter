package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.IAutoShape;
import com.aspose.slides.IGroupShape;
import com.aspose.slides.IShapeCollection;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.data.layout.Stoichiometry;

import java.awt.*;

import static org.reactome.server.tools.diagram.exporter.pptx.util.PPTXShape.*;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class PPTXStoichiometry {

    private IGroupShape iGroupShape;
    private IAutoShape iAutoShape;
    private IAutoShape hiddenCenterShape; // hidden behind the stoichiometry shape

    private Integer value;
    private Shape shape;

    public PPTXStoichiometry(Stoichiometry stoichiometry) {
        this.shape = stoichiometry.getShape();
        this.value = stoichiometry.getValue();
    }

    public PPTXStoichiometry render(IShapeCollection shapes, Stylesheet stylesheet) {
        stylesheet.setTextColor(Color.BLACK);

        iGroupShape = shapes.addGroupShape();
        float xCenter = (shape.getA().getX().floatValue() + shape.getB().getX().floatValue()) / 2;
        float yCenter = (shape.getA().getY().floatValue() + shape.getB().getY().floatValue()) / 2;
        hiddenCenterShape = renderAuxiliaryShape(iGroupShape, xCenter, yCenter);
        iAutoShape = renderShape(iGroupShape, shape, stylesheet);
        setTextFrame(iAutoShape, value.toString(), new double[]{0, 0, 0, 0}, stylesheet.getTextColor(), 8, true, false, null);

        return this;
    }

    public IAutoShape getHiddenCenterShape() {
        return hiddenCenterShape;
    }

    public IGroupShape getiGroupShape() {
        return iGroupShape;
    }
}
