package org.reactome.server.tools.diagram.exporter.sbgn;


import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.sbgn.bindings.*;
import org.sbgn.bindings.Label;
import org.sbgn.bindings.Map;

import java.awt.*;
import java.util.*;
import java.util.List;


/**
 * Generates the SBGN of a given Diagram
 *
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@SuppressWarnings("unused")
public class SbgnConverter {

    private static final String ENTITY_PREFIX = "entityVertex_";
    private static final String REACTION_PREFIX = "reactionVertex_";
    private static final String COMPARTMENT_PREFIX = "compartmentVertex_";

    private static final FontMetrics FONT_DESIGN_METRICS = new Canvas().getFontMetrics((new Font("Arial", Font.PLAIN, 16)));

    private Diagram diagram;
    private Set<Participant> participants = new HashSet<>();
    private java.util.Map<DiagramObject, Glyph> glyphMap = new HashMap<>();
    private java.util.Map<Edge, List<Port>> portMap = new HashMap<>();

    private java.util.Map<Long, Edge> edgeMap = new HashMap<>();

    public SbgnConverter(Diagram diagram) {
        this.diagram = diagram;
        if (diagram.getEdges() != null) diagram.getEdges().forEach(e -> edgeMap.put(e.getId(), e));
    }

    public Sbgn getSbgn() {
        Map map = new Map();
        map.setLanguage("process description");


        for (Compartment compartment : diagram.getCompartments()) {
            map.getGlyph().add(getNodeCompartmentGlyph(compartment));
        }

        for (Node node : diagram.getNodes()) {
            if (node.getIsFadeOut() == null && node.getIsCrossed() == null) {
                glyphMap.put(node, getNodeGlyph(node));
            }
        }

        for (Edge edge : diagram.getEdges()) {
            if (edge.getIsFadeOut() == null) glyphMap.put(edge, getEdgeGlyph(edge));
        }

        glyphMap.values().forEach(g -> map.getGlyph().add(g));

        for (Participant participant : participants) {
            map.getArc().add(getArc(participant));
        }

        Sbgn sbgn = new Sbgn();
        sbgn.setMap(map);
        return sbgn;
    }

    private Glyph getNodeGlyph(Node node) {
        for (Connector connector : node.getConnectors()) {
            Edge edge = edgeMap.get(connector.getEdgeId());
            if (edge.getIsFadeOut() == null) participants.add(new Participant(node, edge, connector));
        }

        Glyph glyph = new Glyph();
        glyph.setId(ENTITY_PREFIX + node.getReactomeId() + "_" + node.getId());
        glyph.setClazz("unspecified entity");

        //The starts with is to automatically handle Drug renderable classes
        if (node.getRenderableClass().startsWith("Complex")) glyph.setClazz("complex");
        else if (node.getRenderableClass().startsWith("Protein")) glyph.setClazz("macromolecule");
        else if (node.getRenderableClass().startsWith("Chemical")) glyph.setClazz("simple chemical");
            //Green boxes to "process"
        else if (node.getRenderableClass().startsWith("Process")) glyph.setClazz("submap");
        else if (node.getRenderableClass().startsWith("EncapsulatedNode")) glyph.setClazz("submap");

        if (node.getNodeAttachments() != null) {
            for (NodeAttachment nodeAttachment : node.getNodeAttachments()) {
                glyph.getGlyph().add(getNodeAttachmentGlyph(nodeAttachment));
            }
        }

        NodeProperties prop = node.getProp();
        Bbox cBox = new Bbox();
        cBox.setX(prop.getX().floatValue());
        cBox.setY(prop.getY().floatValue());
        cBox.setW(prop.getWidth().floatValue());
        cBox.setH(prop.getHeight().floatValue());
        glyph.setBbox(cBox);

        Label label = new Label();
        label.setText(node.getDisplayName());

        glyph.setLabel(label);
        return glyph;
    }

    private int attachmentCounter = 1;

    private Glyph getNodeAttachmentGlyph(NodeAttachment attachment) {
        Glyph glyph = new Glyph();
        glyph.setClazz("unit of information");
        glyph.setId(ENTITY_PREFIX + attachment.getReactomeId() + "_" + attachmentCounter++ + "_mt");

        String lbl = attachment.getLabel() == null ? "mt:prot" : attachment.getLabel();
//        String lbl = "mt:prot";

        Label label = new Label();
        label.setText(lbl);
        glyph.setLabel(label);

        Shape shape = attachment.getShape();
        Bbox lBox = new Bbox();
        lBox.setX(shape.getA().getX().floatValue());
        lBox.setY(shape.getA().getY().floatValue());
        lBox.setW(FONT_DESIGN_METRICS.stringWidth(lbl));
        lBox.setH(16f);
        glyph.setBbox(lBox);

        return glyph;
    }

    private Glyph getNodeCompartmentGlyph(Compartment compartment) {
        Glyph glyph = new Glyph();

        glyph.setId(COMPARTMENT_PREFIX + compartment.getReactomeId() + "_" + compartment.getId());
        glyph.setClazz("compartment");
        glyph.setCompartmentOrder(10f);

        NodeProperties prop = compartment.getProp();
        Bbox cBox = new Bbox();
        cBox.setX(prop.getX().floatValue());
        cBox.setY(prop.getY().floatValue());
        cBox.setW(prop.getWidth().floatValue());
        cBox.setH(prop.getHeight().floatValue());
        glyph.setBbox(cBox);

        Label label = new Label();
        label.setText(compartment.getDisplayName());
        Bbox lBox = new Bbox();
        lBox.setX(compartment.getTextPosition().getX().floatValue());
        lBox.setY(compartment.getTextPosition().getY().floatValue());
        lBox.setW(FONT_DESIGN_METRICS.stringWidth(compartment.getDisplayName()));
        lBox.setH(24f);
        label.setBbox(lBox);

        glyph.setLabel(label);
        return glyph;
    }

    private Glyph getEdgeGlyph(Edge edge) {
        Glyph glyph = new Glyph();

        glyph.setId(REACTION_PREFIX + edge.getReactomeId() + "_" + edge.getId());
        String s = edge.getReactionShape().getS();
        if (s != null && !s.isEmpty()) {
            if (Objects.equals(s, "?")) glyph.setClazz("uncertain process");
            else glyph.setClazz("omitted process");
        } else {
            switch (edge.getReactionShape().getType()) {
                case "CIRCLE":
                    glyph.setClazz("association");
                    break;
                case "DOUBLE_CIRCLE":
                    glyph.setClazz("dissociation");
                    break;
                default:
                    glyph.setClazz("process");
                    break;
            }
        }

        Bbox cBox = new Bbox();
        Coordinate a = edge.getReactionShape().getA();
        if (a != null) {
            Coordinate b = edge.getReactionShape().getB();
            cBox.setX(a.getX().floatValue());
            cBox.setY(a.getY().floatValue());
            cBox.setW(b.getX().floatValue() - a.getX().floatValue());
            cBox.setH(b.getY().floatValue() - a.getY().floatValue());
        } else {
            Coordinate c = edge.getReactionShape().getC();
            float r = edge.getReactionShape().getR().floatValue();
            cBox.setX(c.getX().floatValue() - r);
            cBox.setY(c.getY().floatValue() - r);
            cBox.setW(r * 2);
            cBox.setH(r * 2);
        }
        glyph.setBbox(cBox);

        Label label = new Label();
        label.setText(edge.getDisplayName());
        glyph.setLabel(label);

        // Adding ports
        List<Port> ports = new ArrayList<>();
        portMap.put(edge, ports);

        Port port1 = new Port();
        port1.setId(REACTION_PREFIX + edge.getReactomeId() + ".1");
        glyph.getPort().add(port1);
        ports.add(port1);

        Port port2 = new Port();
        port2.setId(REACTION_PREFIX + edge.getReactomeId() + ".2");
        glyph.getPort().add(port2);
        ports.add(port2);

        if (edge.getSegments() != null && !edge.getSegments().isEmpty()) {
            List<Segment> portCandidates = new ArrayList<>();
            for (Segment segment : edge.getSegments()) {
                ShapeWrapper shape = new ShapeWrapper(edge.getReactionShape());
                if (shape.touches(segment)) portCandidates.add(segment);
            }

            Segment s1 = portCandidates.get(0);
            Segment s2 = portCandidates.size() == 1 ? s1 : portCandidates.get(1);

            if (Math.abs(s1.getFrom().getX() - s2.getTo().getX()) > cBox.getW()) {
                port1.setX(cBox.getX() - cBox.getW() / 3);
                port1.setY(s1.getFrom().getY().floatValue());
                port2.setX(cBox.getX() + (4 * cBox.getW() / 3));
                port2.setY(s2.getTo().getY().floatValue());
            } else {
                port1.setX(s1.getFrom().getX().floatValue());
                port1.setY(cBox.getY() + (4 * cBox.getH() / 3));
                port2.setX(s2.getTo().getX().floatValue());
                port2.setY(cBox.getY() - cBox.getH() / 3);
            }
        } else {
            Coordinate c;
            Shape shape = edge.getReactionShape();
            switch (shape.getType()) {
                case "BOX":
                    Coordinate delta = shape.getB().minus(shape.getA()).divide(2);
                    c = edge.getReactionShape().getA().add(delta);
                    break;
                default:
                    c = edge.getReactionShape().getC();
            }
            port1.setX(c.getX().floatValue());
            port1.setY(c.getY().floatValue());

            port2.setX(c.getX().floatValue());
            port2.setY(c.getY().floatValue());
        }

        return glyph;
    }

    private Arc getArc(Participant p) {
        Arc arc = new Arc();
        arc.setId("arc_" + p.node.getId() + "_" + p.getType().toLowerCase() + "_" + p.edge.getId());

        Glyph nodeGlyph = glyphMap.get(p.node);
        Glyph edgeGlyph = glyphMap.get(p.edge);
        List<Port> ports = portMap.get(p.edge);

        arc.setSource(nodeGlyph);
        arc.setTarget(edgeGlyph);
        switch (p.getType()) {
            case "INPUT":
                arc.setTarget(ports.get(0));
                arc.setClazz("consumption");
                break;
            case "OUTPUT":
                arc.setSource(ports.get(1));
                arc.setTarget(nodeGlyph);
                arc.setClazz("production");
                break;
            case "CATALYST":
                arc.setClazz("catalysis");
                break;
            case "INHIBITOR":
                arc.setClazz("inhibition");
                break;
            case "ACTIVATOR":
                arc.setClazz("stimulation");
                break;
        }

        List<Segment> segments = p.getSegments();
        if (segments != null && !segments.isEmpty()) {
            arc.setStart(new Arc.Start());
            Segment s = segments.get(0);
            arc.getStart().setX(s.getFrom().getX().floatValue());
            arc.getStart().setY(s.getFrom().getY().floatValue());

            for (Arc.Next next : p.getArcNextList()) arc.getNext().add(next);

            arc.setEnd(new Arc.End());
            s = segments.get(segments.size() - 1);
            arc.getEnd().setX(s.getTo().getX().floatValue());
            arc.getEnd().setY(s.getTo().getY().floatValue());
        } else {
            System.err.println(diagram.getStableId() + ": Failed 'arc' for " + p);
        }

        Glyph stoichiometry = p.getStoichiometry();
        if (stoichiometry != null) arc.getGlyph().add(stoichiometry);

        return arc;
    }

}