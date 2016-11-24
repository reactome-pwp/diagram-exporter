package org.reactome.server.tools.model;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class PPTXShape {

    public enum ShapeType { CIRCLE, DOUBLE_CIRCLE, BOX }

    private float x,y,w,h;
    private String text;
    private ShapeType type;

    public PPTXShape(double x, double y, double w, double h, String text, ShapeType type) {
        this.x = (float)x;
        this.y = (float)y;
        this.w = (float)w;
        this.h = (float)h;
        this.text = text;
        this.type = type;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getW() {
        return w;
    }

    public void setW(float w) {
        this.w = w;
    }

    public float getH() {
        return h;
    }

    public void setH(float h) {
        this.h = h;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ShapeType getType() {
        return type;
    }

    public void setType(ShapeType type) {
        this.type = type;
    }
}
