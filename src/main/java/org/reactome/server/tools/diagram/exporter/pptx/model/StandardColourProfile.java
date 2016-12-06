package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.FillType;
import com.aspose.slides.LineStyle;

import java.awt.*;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class StandardColourProfile extends ColourProfile {

    public Stylesheet get(Class _clazz) {
        Color fillColor;
        Color lineColor = Color.BLACK;
        Color fontColor = Color.BLACK;

        switch (_clazz.getSimpleName()) {
            case "Complex":
            case "Set":
                fillColor = new Color(204, 255, 255);
                break;
            case "EncapsulatedPathway":
                fillColor = new Color(255, 255, 255);
                break;
            case "EntityCompartment":
                lineColor = new Color(255, 153, 102);
                fillColor = new Color(249, 245, 245);
                break;
            case "Gene":
                lineColor = null;
                fillColor =  new Color(243, 209, 175);
                break;
            case "OtherEntity":
            case "Protein":
            case "RNA":
            case "Chemical":
                fillColor = new Color(204, 255, 204);
                break;
            default:
                throw new IllegalArgumentException("Color profile not found for class [" + _clazz.getSimpleName() + "]. Create the switch-case for the given class");
        }

        return new Stylesheet(2, LineStyle.Single, FillType.Solid, lineColor, FillType.Solid, fillColor, fontColor);
    }
}
