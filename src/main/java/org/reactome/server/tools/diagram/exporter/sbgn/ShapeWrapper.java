package org.reactome.server.tools.diagram.exporter.sbgn;

import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.Segment;
import org.reactome.server.tools.diagram.data.layout.Shape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ShapeWrapper {

    private final Integer minX, maxX, minY, maxY;

    ShapeWrapper(Shape shape) {
        List<Double> xx = new ArrayList<>();
        List<Double> yy = new ArrayList<>();
        Coordinate c = shape.getC();
        switch (shape.getType()) {
            case "CIRCLE":
            case "DOUBLE_CIRCLE":
                Double r = shape.getR();
                xx.add(c.getX() + r); yy.add(c.getY() + r);
                xx.add(c.getX() - r); yy.add(c.getY() - r);
                break;
            case "ARROW":
                xx.add(c.getX()); yy.add(c.getY());
            default:
                Coordinate a = shape.getA();
                Coordinate b = shape.getB();
                xx.add(a.getX()); yy.add(a.getY());
                xx.add(b.getX()); yy.add(b.getY());
        }
        this.minX = Collections.min(xx).intValue();
        this.maxX = Collections.max(xx).intValue();
        this.minY = Collections.min(yy).intValue();
        this.maxY = Collections.max(yy).intValue();
    }

    boolean touches(Segment s) {
        return pointInRectangle(s.getFrom().getX(), s.getFrom().getY()) || pointInRectangle(s.getTo().getX(), s.getTo().getY());
    }

    private boolean pointInRectangle(double x, double y) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }


}
