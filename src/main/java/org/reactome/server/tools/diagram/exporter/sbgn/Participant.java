package org.reactome.server.tools.diagram.exporter.sbgn;

import org.reactome.server.tools.diagram.data.layout.*;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Label;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
class Participant {

    final Node node;
    final Edge edge;
    private Connector connector;

    Participant(Node node, Edge edge, Connector connector) {
        this.node = node;
        this.edge = edge;
        this.connector = connector;
    }

    public String getType() {
        return connector.getType();
    }

    List<Segment> getSegments() {
        if (connector.getSegments() != null && !connector.getSegments().isEmpty()) return connector.getSegments();
        switch (connector.getType()) {
            case "INPUT":
                return edge.getSegments().subList(0, 1);
            case "OUTPUT":
                if(edge.getSegments().size() == 1) return edge.getSegments();
                return edge.getSegments().subList(1, 2);
            default:
                return edge.getSegments();
        }
    }

    Glyph getStoichiometry(){
        Shape shape = connector.getStoichiometry().getShape();
        if(shape == null) return null;

        Glyph glyph = new Glyph();
        glyph.setClazz("stoichiometry");    // "cardinality"
        glyph.setId("Stoichiometry_" + node.getReactomeId() + "_" + connector.getType() + "_" + edge.getReactomeId());

        Label label = new Label();
        label.setText(connector.getStoichiometry().getValue() + "");
        glyph.setLabel(label);

        Bbox bBox = new Bbox();
        bBox.setX(shape.getA().getX().floatValue());
        bBox.setY(shape.getA().getY().floatValue());
        bBox.setW(shape.getB().getX().floatValue() - shape.getA().getX().floatValue());
        bBox.setH(shape.getB().getY().floatValue() - shape.getA().getY().floatValue());
        glyph.setBbox(bBox);

        return glyph;
    }

    List<Arc.Next> getArcNextList(){
        List<Arc.Next> nextList = new ArrayList<>();
        if (connector.getSegments() != null) {
            for (Segment segment : connector.getSegments()  ) {
                ShapeWrapper shape = new ShapeWrapper(edge.getReactionShape());
                if (!shape.touches(segment)) {
                    Arc.Next next = new Arc.Next();
                    next.setX(segment.getTo().getX().floatValue());
                    next.setY(segment.getTo().getY().floatValue());
                    nextList.add(next);
                }
            }
        }

        if (edge.getSegments() != null) {
            switch (connector.getType()) {
                case "INPUT":
                    for (Segment segment : edge.getSegments()) {
                        ShapeWrapper shape = new ShapeWrapper(edge.getReactionShape());
                        if (!shape.touches(segment)) {
                            Arc.Next next = new Arc.Next();
                            next.setX(segment.getTo().getX().floatValue());
                            next.setY(segment.getTo().getY().floatValue());
                            nextList.add(next);
                        } else {
                            break;
                        }
                    }
                    break;
                case "OUTPUT":
                    List<Segment> segs = edge.getSegments().subList(0, edge.getSegments().size());
                    Collections.reverse(segs);
                    for (Segment segment : segs) {
                        ShapeWrapper shape = new ShapeWrapper(edge.getReactionShape());
                        if (!shape.touches(segment)) {
                            Arc.Next next = new Arc.Next();
                            next.setX(segment.getFrom().getX().floatValue());
                            next.setY(segment.getFrom().getY().floatValue());
                            nextList.add(next);
                        } else {
                            break;
                        }
                    }
                    break;
            }
        }

        if(connector.getType().equals("OUTPUT")) Collections.reverse(nextList);
        return nextList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return Objects.equals(node, that.node) &&
                Objects.equals(edge, that.edge) &&
                Objects.equals(connector, that.connector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, edge, connector);
    }

    @Override
    public String toString() {
        return "Participant{" +
                "node=" + node.getReactomeId() +
                ", edge=" + edge.getReactomeId() +
                '}';
    }
}
