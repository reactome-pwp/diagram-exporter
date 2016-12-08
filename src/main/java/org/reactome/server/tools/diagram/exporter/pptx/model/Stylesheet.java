package org.reactome.server.tools.diagram.exporter.pptx.model;

import org.reactome.server.tools.diagram.exporter.common.profiles.model.DiagramProfileNode;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class Stylesheet {

    private double lineWidth;
    private Color lineColor;
    private Color fillColor;
    private Color textColor;
    private Color fadeOutStroke;
    private Color fadeOutFill;
    private Color fadeOutText;
    private Color lighterStroke;
    private Color lighterFill;
    private Color lighterText;
    private byte shapeFillType;
    private byte lineFillType;
    private byte lineStyle;

    public Stylesheet() {

    }

    public Stylesheet(DiagramProfileNode profileInfo, byte shapeFillType, byte lineFillType, byte lineStyle) {
        this.lineWidth = profileInfo.getLineWidth() != null ? Double.valueOf(profileInfo.getLineWidth()) * 2 : 2;
        this.lineColor = parseColor(profileInfo.getStroke());
        this.fillColor = parseColor(profileInfo.getFill());
        this.textColor = parseColor(profileInfo.getText());
        this.fadeOutStroke = parseColor(profileInfo.getFadeOutStroke());
        this.fadeOutFill = parseColor(profileInfo.getFadeOutFill());
        this.fadeOutText = parseColor(profileInfo.getFadeOutText());
        this.lighterStroke = parseColor(profileInfo.getLighterStroke());
        this.lighterFill = parseColor(profileInfo.getLighterFill());
        this.lighterText = parseColor(profileInfo.getLighterText());
        this.shapeFillType = shapeFillType;
        this.lineFillType = lineFillType;
        this.lineStyle = lineStyle;
    }

    public Stylesheet(DiagramProfileNode profileInfo) {
        this(profileInfo, (byte) 0, (byte) 0, (byte) 0);
    }

    /**
     * Apply custom style.
     * Mainly used in the connector or in the auxiliary shapes.
     */
    public Stylesheet customStyle(double lineWidth, byte lineStyle, byte lineFillType, Color lineColor, byte shapeFillType, Color fillColor) {
        Stylesheet stylesheet = new Stylesheet();
        stylesheet.setLineWidth(lineWidth);
        stylesheet.setLineStyle(lineStyle);
        stylesheet.setLineFillType(lineFillType);
        stylesheet.setLineColor(lineColor);
        stylesheet.setShapeFillType(shapeFillType);
        stylesheet.setFillColor(fillColor);
        return stylesheet;
    }

    private static Color parseColor(String color) {
        if(color == null) return null;

        if (color.startsWith("#")) {
            return hexToColor(color);
        }
        return rgbaToColor(color);
    }

    private static Color hexToColor(String input) {
        int r = Integer.valueOf(input.substring(1, 3), 16);
        int g = Integer.valueOf(input.substring(3, 5), 16);
        int b = Integer.valueOf(input.substring(5, 7), 16);

        return new Color(r, g, b);
    }

    private static Color rgbaToColor(String input) {
        String rgbaRegex = "^rgba\\(\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])\\s*,\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])\\s*,\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])\\s*,\\s*((0.[1-9])|[01])\\s*\\)$";

        Pattern c = Pattern.compile(rgbaRegex);
        Matcher m = c.matcher(input);
        if (m.matches()) {
            return  new Color(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), (int)(Float.parseFloat(m.group(4)) * 255f));
        }
        return null;
    }

    public Color getFadeOutFill() {
        return fadeOutFill;
    }

    public Color getFadeOutStroke() {
        return fadeOutStroke;
    }

    public Color getFadeOutText() {
        return fadeOutText;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public Color getLighterFill() {
        return lighterFill;
    }

    public Color getLighterStroke() {
        return lighterStroke;
    }

    public Color getLighterText() {
        return lighterText;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public byte getLineFillType() {
        return lineFillType;
    }

    public void setLineFillType(byte lineFillType) {
        this.lineFillType = lineFillType;
    }

    public byte getLineStyle() {
        return lineStyle;
    }

    public void setLineStyle(byte lineStyle) {
        this.lineStyle = lineStyle;
    }

    public double getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(double lineWidth) {
        this.lineWidth = lineWidth;
    }

    public byte getShapeFillType() {
        return shapeFillType;
    }

    public void setShapeFillType(byte shapeFillType) {
        this.shapeFillType = shapeFillType;
    }

    public Color getTextColor() {
        return textColor;
    }
}
