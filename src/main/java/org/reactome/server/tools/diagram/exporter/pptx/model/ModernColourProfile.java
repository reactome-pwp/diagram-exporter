package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.FillType;
import com.aspose.slides.LineStyle;

import java.awt.*;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class ModernColourProfile extends ColourProfile {

    public Stylesheet get(Class _clazz) {
        byte shapeFillType = FillType.Solid;
        byte lineFillType = FillType.Solid;
        byte lineStyle = LineStyle.Single;
        double lineWidth = 1.25;
        Color lineColor;
        Color fillColor;
        Color fontColor = Color.BLUE;

        switch (_clazz.getSimpleName()) {
            case "Chemical":
                lineWidth = 2;
                lineColor = new Color(133, 175, 117);
                fillColor = new Color(165, 215, 145);
                break;
            case "Complex":
                lineColor = new Color(31, 136, 167);
                fillColor = new Color(171, 209, 227);
                break;
            case "EncapsulatedPathway":
                lineStyle = LineStyle.ThinThin;
                lineWidth = 10;
                lineColor = new Color(165, 215, 145);
                fillColor =  new Color(255, 255, 255);
                break;
            case "EntityCompartment":
                lineColor = new Color(253, 138, 61);
                fillColor =  new Color(245, 217, 188);
                break;
            case "Gene":
                lineFillType = FillType.NoFill;
                lineColor = null;
                fillColor =  Color.lightGray;
                break;
            case "OtherEntity":
            case "Protein":
                lineColor =  new Color(74, 149, 134);
                fillColor = new Color(141, 199, 187);
                break;
            case "RNA":
                lineColor = new Color(74, 149, 134);
                fillColor = new Color(155, 0, 0);
                break;
            case "Set":
                lineStyle = LineStyle.ThinThin;
                lineWidth = 4.75;
                lineColor = new Color(106, 106, 227);
                fillColor = new Color(160, 187, 205);
                break;
            default:
                throw new IllegalArgumentException("Colour profile not found [" + _clazz.getSimpleName() + "]. Create the switch-case for the given class");
        }

        return new Stylesheet(lineWidth, lineStyle, lineFillType, lineColor, shapeFillType, fillColor, fontColor);
    }
}
