package org.reactome.server.tools.diagram.exporter.model;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public abstract class PPTXNode {

    Long id;
    float x;
    float y;
    float width = 1;
    float height = 1;
    String displayName;

    //private Map<Long, Participant> participants;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
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

    public abstract void render();
}
