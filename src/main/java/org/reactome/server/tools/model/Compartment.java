package org.reactome.server.tools.model;

import java.awt.*;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class Compartment {

    private String name;
    private Point.Float textPosition;
    private float x;
    private float y;
    private float width;
    private float height;

    public Compartment(String displayName, double x, double y, double width, double height) {
        this.name = displayName;
        this.x = (float) x;
        this.y = (float) y;
        this.width = (float) width;
        this.height = (float) height;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Point.Float getTextPosition() {
        return textPosition;
    }

    public void setTextPosition(Point.Float textPosition) {
        this.textPosition = textPosition;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
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

    public void render() {

    }
}
