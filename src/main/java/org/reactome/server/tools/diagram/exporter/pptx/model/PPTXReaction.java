package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.data.layout.Shape;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class PPTXReaction {

    private Edge edge;
    private Map<Long, PPTXNode> nodesMap;

    private IAutoShape backboneStart;
    private IAutoShape backboneEnd;

    private IAutoShape reactionShape;
    private Map<Connector, IAutoShape> catalystShapeMap = new HashMap<>();

    public PPTXReaction(Edge edge, Map<Long, PPTXNode> nodesMap) {
        this.edge = edge;
        this.nodesMap = nodesMap;
    }

    public void render(IShapeCollection shapes) {

        // It checks for all the shapes to be grouped and creates them in advance placing
        // the main shape in reactionShape and the rest in the catalystShapeMap map
        createReactionShape(shapes);

        if (hasInputs()) {
            if (onlyOneInputWithoutConnectors()) {
                IAutoShape input = nodesMap.get(edge.getInputs().get(0).getId()).getiAutoShape();
                connect(shapes, input, backboneStart);
            } else {
                createConnectorsFromInputs(shapes);
            }
        }

        if (hasOutputs()) {
            if (onlyOneOutputWithoutConnectors()) {
                IAutoShape output = nodesMap.get(edge.getOutputs().get(0).getId()).getiAutoShape();
                connect(shapes, backboneEnd, output);
            } else {
                createConnectorsToOutputs(shapes);
            }
        }

        if (hasCatalyst()) createConnectorsToCatalyst(shapes);

        if (hasActivators()) createConnectorsToActivator(shapes);

    }

    private void createConnectorsFromInputs(IShapeCollection shapes) {
//        connect(shapes, reactionShape, backboneStart); //Drawing the backbone
        for (ReactionPart reactionPart : edge.getInputs()) {
            PPTXNode input = nodesMap.get(reactionPart.getId());
            IAutoShape last = input.getiAutoShape();
            for (Connector connector : input.getConnectors()) {
                if (!isType(connector, "INPUT")) continue;
                for (int i = 0; i < connector.getSegments().size() - 1; i++) {
                    Segment segment = connector.getSegments().get(i);
                    IAutoShape step = shapes.addAutoShape(ShapeType.Ellipse, segment.getTo().getX().floatValue(), segment.getTo().getY().floatValue(), 1f, 1f);
                    connect(shapes, step, last);
                    last = step;
                }
                connect(shapes, last, backboneStart);
            }
        }
    }

    private void createConnectorsToOutputs(IShapeCollection shapes) {
        for (ReactionPart reactionPart : edge.getOutputs()) {
            PPTXNode output = nodesMap.get(reactionPart.getId());
            IAutoShape last = output.getiAutoShape();
            for (Connector connector : output.getConnectors()) {
                if (!isType(connector, "OUTPUT")) continue;
                for (int i = 1; i < connector.getSegments().size(); i++) {
                    Segment segment = connector.getSegments().get(i);
                    IAutoShape step = shapes.addAutoShape(ShapeType.Ellipse, segment.getFrom().getX().floatValue(), segment.getFrom().getY().floatValue(), 1f, 1f);
                    connect(shapes, last, step);
                    last = step;
                }
                connect(shapes, last, backboneEnd);
            }
        }
    }

    private void createConnectorsToCatalyst(IShapeCollection shapes) {
        for (ReactionPart reactionPart : edge.getCatalysts()) {
            PPTXNode catalyst = nodesMap.get(reactionPart.getId());
            IAutoShape last = catalyst.getiAutoShape();
            for (Connector connector : catalyst.getConnectors()) {
                if (!isType(connector, "CATALYST")) continue;
                for (int i = 0; i < connector.getSegments().size() - 1; i++) {
                    Segment segment = connector.getSegments().get(i);
                    IAutoShape step = shapes.addAutoShape(ShapeType.Ellipse, segment.getTo().getX().floatValue(), segment.getTo().getY().floatValue(), 1f, 1f);
                    connect(shapes, last, step);
                    last = step;
                }
                connect(shapes, last, catalystShapeMap.get(connector));
            }
        }
    }

    private void createConnectorsToActivator(IShapeCollection shapes) {
        for (ReactionPart reactionPart : edge.getActivators()) {
            PPTXNode activator = nodesMap.get(reactionPart.getId());
            IAutoShape last = activator.getiAutoShape();
            for (Connector connector : activator.getConnectors()) {
                if (!isType(connector, "ACTIVATOR")) continue;
                for (int i = 0; i < connector.getSegments().size() - 1; i++) {
                    Segment segment = connector.getSegments().get(i);
                    IAutoShape step = shapes.addAutoShape(ShapeType.Ellipse, segment.getTo().getX().floatValue(), segment.getTo().getY().floatValue(), 1f, 1f);
                    connect(shapes, last, step);
                    last = step;
                }
                connect(shapes, last, reactionShape);
            }
        }
    }

    private void createReactionShape(IShapeCollection shapes) {
        //Creates reaction shape
        IGroupShape groupShape = shapes.addGroupShape();
        Shape rShape = edge.getReactionShape();
        reactionShape = PPTXShape.renderShape(groupShape, rShape);

        createBackBone(shapes, rShape);

        if (hasCatalyst()) {
            for (ReactionPart reactionPart : edge.getCatalysts()) {
                PPTXNode catalyst = nodesMap.get(reactionPart.getId());
                for (Connector connector : catalyst.getConnectors()) {
                    if (!isType(connector, "CATALYST")) continue;
                    Shape shape = connector.getEndShape();
                    IAutoShape cs = PPTXShape.renderShape(groupShape, shape);
                    catalystShapeMap.put(connector, cs);
                }
            }
        }

//        if (hasActivators()) {
//            for (ReactionPart reactionPart : edge.getActivators()) {
//                PPTXNode activator = nodesMap.get(reactionPart.getId());
//                for (Connector connector : activator.getConnectors()) {
//                    if (!isType(connector, "ACTIVATOR")) continue;
//                    Shape shape = connector.getEndShape();
//                    IAutoShape cs = PPTXShape.renderShape(groupShape, shape);
//                    catalystShapeMap.put(connector, cs);
//                }
//            }
//        }

        //TODO: Missing Inhibitors
    }

    /**
     * The backbone is a little bit special since it might contain a number of segments and the reaction shape
     * will be in the intersection of either of them.
     *
     * The problem is that initially we don't know which one because the diagram has always been drawn in
     * raster and that was not a big problem. Now we have to figure out which connector connects to the
     * reaction shape, and that's what this method is doing (don't panic!)
     *
     * @param shapes the collection of shapes where the backbone has to be added
     * @param rShape the reaction shape previously created
     */
    private void createBackBone(IShapeCollection shapes, Shape rShape){
        Coordinate start = edge.getSegments().get(0).getFrom();
        if(PPTXShape.touches(rShape, start)){
            backboneStart = reactionShape;
        } else {
            backboneStart = shapes.addAutoShape(ShapeType.Ellipse, start.getX().floatValue(), start.getY().floatValue(), 1f, 1f);
        }
        IAutoShape last = backboneStart;
        for (int i = 1; i < edge.getSegments().size(); i++) { //IMPORTANT: It starts in "1" because "0" has been taken above
            Coordinate step = edge.getSegments().get(i).getFrom();
            if(PPTXShape.touches(rShape, step)) {
                connect(shapes, last, reactionShape);
                last = reactionShape;
            } else {
                IAutoShape backboneStep = shapes.addAutoShape(ShapeType.Ellipse, step.getX().floatValue(), step.getY().floatValue(), 1f, 1f);
                connect(shapes, last, backboneStep);
                last = backboneStep;
            }
        }
        backboneEnd = last; //The last one could either be an anchor point or the reactionShape itself, but that's not a problem at all!
    }

    private void connect(IShapeCollection shapes, IShape start, IShape end) {
        IConnector connector = shapes.addConnector(ShapeType.StraightConnector1, start.getX(), start.getY(), 1, 1, true);

        connector.getLineFormat().getFillFormat().setFillType(FillType.Solid);
        connector.getLineFormat().getFillFormat().getSolidFillColor().setColor(Color.BLACK);
        connector.getLineFormat().setWidth(2);

        connector.setStartShapeConnectedTo(start);
        connector.setEndShapeConnectedTo(end);
    }

    private boolean onlyOneInputWithoutConnectors() {
        if (edge.getInputs().size() == 1) {
            List<Connector> connectors = nodesMap.get(edge.getInputs().get(0).getId()).getConnectors(edge.getId(), "INPUT");
            return connectors.isEmpty() || connectors.get(0).getSegments().isEmpty();
        }
        return false;
    }

    private boolean hasInputs() {
        return edge.getInputs() != null && !edge.getInputs().isEmpty();
    }

    private boolean isType(Connector connector, String type) {
        return connector.getEdgeId().equals(edge.getId()) && connector.getType().equals(type);
    }

    private boolean hasOutputs() {
        return edge.getOutputs() != null && !edge.getOutputs().isEmpty();
    }

    private boolean hasCatalyst() {
        return edge.getCatalysts() != null && !edge.getCatalysts().isEmpty();
    }

    private boolean hasActivators() {
        return edge.getActivators() != null && !edge.getActivators().isEmpty();
    }

    private boolean onlyOneOutputWithoutConnectors() {
        if (edge.getOutputs().size() == 1) {
            List<Connector> connectors = nodesMap.get(edge.getOutputs().get(0).getId()).getConnectors(edge.getId(), "OUTPUT");
            return connectors.isEmpty() || connectors.get(0).getSegments().isEmpty();
        }
        return false;
    }
}
