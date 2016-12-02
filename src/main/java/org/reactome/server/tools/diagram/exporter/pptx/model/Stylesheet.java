package org.reactome.server.tools.diagram.exporter.pptx.model;

import java.awt.*;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class Stylesheet {

    private Color lineColor;
    private Color fillColor;
    private Color fontColor;
    private byte shapeFillType;
    private byte lineFillType;
    private byte lineStyle;
    private int lineWidth;

    public Color getFillColor() {
        return fillColor;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public byte getLineFillType() {
        return lineFillType;
    }

    public byte getLineStyle() {
        return lineStyle;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public byte getShapeFillType() {
        return shapeFillType;
    }

    public Color getFontColor() {
        return fontColor;
    }

    public Stylesheet(int lineWidth,
                      byte lineStyle,
                      byte lineFillType,
                      Color lineColor,
                      byte shapeFillType,
                      Color fillColor) {
        this.lineWidth = lineWidth;
        this.lineStyle = lineStyle;
        this.lineFillType = lineFillType;
        this.lineColor = lineColor;
        this.shapeFillType = shapeFillType;
        this.fillColor = fillColor;
    }
    public Stylesheet(int lineWidth,
                      byte lineStyle,
                      byte lineFillType,
                      Color lineColor,
                      byte shapeFillType,
                      Color fillColor,
                      Color fontColor) {
        this(lineWidth, lineStyle, lineFillType,lineColor,shapeFillType,fillColor);
        //Font Color does work because of the Hyperlink. Even trying to set it in PP.
        this.fontColor = fontColor;
    }
}
