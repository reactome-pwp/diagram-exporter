package org.reactome.server.tools.parser;

import org.reactome.server.tools.diagram.data.DiagramFactory;
import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.model.*;
import org.reactome.server.tools.model.PPTXShape;
import org.reactome.server.tools.model.Set;
import org.reactome.server.tools.model.Stoichiometry;

import java.awt.*;
import java.awt.geom.Point2D;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class DiagramParser {
    public Pathway iDontKnowTheNameOfThisMethod(String... lotsOfParamHereMaybe) throws Exception { // TODO catch exception properly....
        String json = new String(Files.readAllBytes(Paths.get("R-HSA-169911.json")));
        Diagram diagram = DiagramFactory.getDiagram(json);

        // TODO: Process compartments

        Map<Long, PPTXNode> nodesMap = new HashMap<>();
        for (Node node : diagram.getNodes()) {
            PPTXNode pptxNode = getNode(node);
            List<Connector> connectors = node.getConnectors();
            if (connectors != null) {
                List<Participant> participants = new ArrayList<>();
                for (Connector connector : connectors) {
                    Participant participant = getParticipant(connector.getType());
                    List<Segment> segments = connector.getSegments();
                    if (segments != null) {
                        for (Segment segment : segments) {
                            participant.addPPTXSegment(new PPTXSegment(new Point2D.Double(segment.getFrom().getX(), segment.getFrom().getY()), new Point2D.Double(segment.getFrom().getX(), segment.getFrom().getY())));
                        }
                    }
                    participant.setStoichiometry(new Stoichiometry(connector.getStoichiometry().getValue()));
                    participant.setPptxNode(pptxNode);
                    participant.setId(connector.getEdgeId());
                    participants.add(participant);

                    //pptxNode.addParticipant(connector.getEdgeId(), participant);
                }
                //pptxNode.setParticipants(participants);

                nodesMap.put(pptxNode.getId(), pptxNode);
            }
        }

        List<Reaction> reactions = new ArrayList<>();
        for (Edge edge : diagram.getEdges()) {
            Reaction reaction = new Reaction();

            if (edge.getSegments() != null && edge.getSegments().size() == 2) {
                reaction.setBackboneInput(new PPTXSegment(new Point2D.Double(edge.getSegments().get(0).getFrom().getX(), edge.getSegments().get(0).getFrom().getY()), new Point2D.Double(edge.getSegments().get(0).getTo().getX(), edge.getSegments().get(0).getTo().getY())));
                reaction.setBackboneOutput(new PPTXSegment(new Point2D.Double(edge.getSegments().get(1).getFrom().getX(), edge.getSegments().get(1).getFrom().getY()), new Point2D.Double(edge.getSegments().get(1).getTo().getX(), edge.getSegments().get(1).getTo().getY())));
            } else {
                // TODO Throw an specific exception
                throw new Exception("Reaction without two segments. I wasn't expected to be like that... edge: [" + edge.getId() + "]");
            }

            reaction.setParticipants(getReactionParticipants(edge));
            reaction.setPptxShape(getReactionShape(edge));
            reaction.setStId(edge.getReactomeId());

            reactions.add(reaction);
        }

        Pathway pathway = new Pathway();
        pathway.setReactions(reactions);
        pathway.setCompartments(null);
        return pathway;
    }

    /**
     *
     * @param edge given reaction
     * @return PPTXShape representing a reaction shape
     * @throws Exception
     */
    private PPTXShape getReactionShape(Edge edge) throws Exception {
        PPTXShape pptxShape;
        switch (edge.getReactionShape().getType()) {
            case "CIRCLE":
                pptxShape = new PPTXShape(edge.getReactionShape().getC().getX(), edge.getReactionShape().getC().getY(), edge.getReactionShape().getR(), edge.getReactionShape().getR(), edge.getReactionShape().getS(), PPTXShape.ShapeType.CIRCLE);
                break;
            case "DOUBLE_CIRCLE":
                pptxShape = new PPTXShape(edge.getReactionShape().getC().getX(), edge.getReactionShape().getC().getY(), edge.getReactionShape().getR(), edge.getReactionShape().getR(), edge.getReactionShape().getS(), PPTXShape.ShapeType.DOUBLE_CIRCLE);
                // getc and getR1 getR
                break;
            case "BOX":
                Rectangle rect = getRectangle(edge.getReactionShape());
                pptxShape = new PPTXShape(rect.getX(), rect.getY(), rect.getWidth(), rect.getWidth(), edge.getReactionShape().getS(), PPTXShape.ShapeType.BOX);
                break;
            default:
                // TODO throw new ReactionNodeNotFound
                throw new Exception("Reaction type " + edge.getReactionShape().getType() + " not found ");
        }
        return pptxShape;
    }

    /**
     * Getting inputs,outputs,catalysts,activators and inhibitors from an Edge.
     * Then get participants associated to each of them.
     *
     * @param edge     reaction
     * @return a List of participants
     */
    private List<Participant> getReactionParticipants(Edge edge) {

        // get or create Participant
        for (ReactionPart reactionPart : nullToEmpty(edge.getInputs())) {

            System.out.println(reactionPart.getId());

            //reactionParticipants.add(pptxNode.getParticipants().get(reactionPart.getId()));
        }


        for (ReactionPart reactionPart : nullToEmpty(edge.getOutputs())) {

            System.out.println("out " + reactionPart.getId());

            //reactionParticipants.add(pptxNode.getParticipants().get(reactionPart.getId()));
        }





        List<Participant> reactionParticipants = new ArrayList<>();
//        for (ReactionPart reactionPart : nullToEmpty(edge.getInputs())) {
//            PPTXNode pptxNode = nodesMap.get(reactionPart.getId());
//            //reactionParticipants.add(pptxNode.getParticipants().get(reactionPart.getId()));
//        }
//        for (ReactionPart reactionPart : nullToEmpty(edge.getOutputs())) {
//            PPTXNode pptxNode = nodesMap.get(reactionPart.getId());
//            //reactionParticipants.add(pptxNode.getParticipants().get(reactionPart.getId()));
//        }
//        for (ReactionPart reactionPart : nullToEmpty(edge.getCatalysts())) {
//            PPTXNode pptxNode = nodesMap.get(reactionPart.getId());
//            //reactionParticipants.add(pptxNode.getParticipants().get(reactionPart.getId()));
//        }
//        for (ReactionPart reactionPart : nullToEmpty(edge.getActivators())) {
//            PPTXNode pptxNode = nodesMap.get(reactionPart.getId());
//            //reactionParticipants.add(pptxNode.getParticipants().get(reactionPart.getId()));
//        }
//        for (ReactionPart reactionPart : nullToEmpty(edge.getInhibitors())) {
//            PPTXNode pptxNode = nodesMap.get(reactionPart.getId());
//            //reactionParticipants.add(pptxNode.getParticipants().get(reactionPart.getId()));
//        }
        return reactionParticipants;
    }

    private Participant getParticipant(String type) {
        Participant participant;
        switch (type) {
            case "INPUT":
                participant = new Input();
                break;
            case "OUTPUT":
                participant = new Output();
                break;
            case "CATALYST":
                participant = new Catalyst();
                break;
            case "ACTIVATOR": // positive
                participant = new PositiveRegulator();
                break;
            case "INHIBITOR": // negative
                participant = new NegativeRegulator();
                break;
            default:
                throw new IllegalArgumentException("Invalid connector type [" + type + "]. Create the switch-case for the given class");
        }
        return participant;
    }

    /**
     * Get proper instance of a node and its coordinates e size
     *
     * @param node
     * @return instance of PPTXNode
     */
    private PPTXNode getNode(Node node) {
        PPTXNode pptxNode;
        switch (node.getSchemaClass()) {
            case "Complex":
                pptxNode = new Complex();
                break;
            case "DefinedSet":
            case "CandidateSet":
            case "OpenSet":
                pptxNode = new Set();
                break;
            case "EntityWithAccessionedSequence":
                pptxNode = new Protein();
                break;
            case "GenomeEncodedEntity":
                pptxNode = new OtherEntity();
                break;
            case "SimpleEntity":
                pptxNode = new Chemical();
                break;
            default:
                throw new IllegalArgumentException("Invalid schema class [" + node.getSchemaClass() + "]. Create the switch-case for the given class");
        }
        pptxNode.setId(node.getId());
        pptxNode.setDisplayName(node.getDisplayName());
        pptxNode.setX(node.getProp().getX().floatValue());
        pptxNode.setY(node.getProp().getY().floatValue());
        pptxNode.setWidth(node.getProp().getWidth().floatValue());
        pptxNode.setHeight(node.getProp().getHeight().floatValue());
        return pptxNode;
    }

    /**
     * Get a rectangle from minX, minY, maxX, maxY and then easily extract width and height.
     *
     * @param reactionShape given reaction shape
     * @return java.awt.Rectangle
     */
    private Rectangle getRectangle(Shape reactionShape) {
        Rectangle rectangle = new Rectangle(new Point(reactionShape.getA().getX().intValue(), reactionShape.getA().getY().intValue()));
        rectangle.add(new Point(reactionShape.getB().getX().intValue(), reactionShape.getB().getY().intValue()));
        return rectangle;

        // TODO Add to a Utility class
    }

    private static <T> List<T> nullToEmpty(final List<T> list ) {
        return list == null ? Collections.EMPTY_LIST : list;
        // TODO Add to a Utility class
    }
}
