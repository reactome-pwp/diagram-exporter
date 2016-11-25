package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.data.layout.Shape;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class PPTXReaction {

    private Edge edge;
    private Map<Long, PPTXNode> nodesMap;

    private IAutoShape reactionShape;
    private Map<Long, IAutoShape> reactionShapeParts = new HashMap<>();

    public PPTXReaction(Edge edge, Map<Long, PPTXNode> nodesMap) {
        this.edge = edge;
        this.nodesMap = nodesMap;
    }

    public void render(IShapeCollection shapes) {

        // It checks for all the shapes to be grouped and creates them in advance placing
        // the main shape in reactionShape and the rest in the reactionShapeParts map
        createReactionShape(shapes);

        if (hasInputs()) {
            Segment segment = edge.getSegments().get(0); //Input backbone
            if (onlyOneInputWithoutConnectors()) {
                IAutoShape input = nodesMap.get(edge.getInputs().get(0).getId()).getiAutoShape();
                connect(shapes, input, reactionShapeParts.get(edge.getId()));
            } else {
                createConnectorsFromInputs(shapes, segment.getFrom());
            }
        }

        if (hasOutputs()) {
            Segment segment = edge.getSegments().get(1); //Output backbone
            if (onlyOneOutputWithoutConnectors()) {
                IAutoShape output = nodesMap.get(edge.getOutputs().get(0).getId()).getiAutoShape();
                connect(shapes, output, reactionShape);
            } else {
                createConnectorsToOutputs(shapes, segment.getTo());
            }
        }

        if (hasCatalyst()) {
            createConnectorsToCatalyst(shapes);
        }
    }

    private void createConnectorsFromInputs(IShapeCollection shapes, Coordinate anchor) {
        final IAutoShape backboneStart = shapes.addAutoShape(ShapeType.Ellipse, anchor.getX().floatValue(), anchor.getY().floatValue(), 1f, 1f);
        connect(shapes, reactionShape, backboneStart); //Drawing the backbone
        for (ReactionPart reactionPart : edge.getInputs()) {
            PPTXNode input = nodesMap.get(reactionPart.getId());
            IAutoShape last = backboneStart;
            for (Connector connector : input.getConnectors()) {
                if (!connector.getType().equals("INPUT")) continue;
                IAutoShape step = backboneStart;
                for (int i = 0; i < connector.getSegments().size() - 1; i++) {
                    Segment segment = connector.getSegments().get(i);
                    last = shapes.addAutoShape(ShapeType.Ellipse, segment.getFrom().getX().floatValue(), segment.getFrom().getY().floatValue(), 1f, 1f);
                    connect(shapes, step, last);
                    step = last;
                }
            }
            connect(shapes, last, input.getiAutoShape());
        }
    }

    private void createConnectorsToOutputs(IShapeCollection shapes, Coordinate anchor) {
        final IAutoShape backboneEnd = shapes.addAutoShape(ShapeType.Ellipse, anchor.getX().floatValue(), anchor.getY().floatValue(), 1f, 1f);
        connect(shapes, reactionShape, backboneEnd); //Drawing the backbone
        for (ReactionPart reactionPart : edge.getOutputs()) {
            PPTXNode output = nodesMap.get(reactionPart.getId());
            IAutoShape last = backboneEnd;
            for (Connector connector : output.getConnectors()) {
                if (!connector.getType().equals("OUTPUT")) continue;
                IAutoShape step = backboneEnd;
                for (int i = 0; i < connector.getSegments().size() - 1; i++) {
                    Segment segment = connector.getSegments().get(i);
                    last = shapes.addAutoShape(ShapeType.Ellipse, segment.getTo().getX().floatValue(), segment.getTo().getY().floatValue(), 1f, 1f);
                    connect(shapes, step, last);
                    step = last;
                }
            }
            connect(shapes, last, output.getiAutoShape());
        }
    }

    private void createConnectorsToCatalyst(IShapeCollection shapes) {
        for (ReactionPart reactionPart : edge.getCatalysts()) {
            PPTXNode catalyst = nodesMap.get(reactionPart.getId());
            IAutoShape last = catalyst.getiAutoShape();
            for (Connector connector : catalyst.getConnectors()) {
                if (!connector.getType().equals("CATALYST")) continue;
                for (int i = 0; i < connector.getSegments().size() - 1; i++) {
                    Segment segment = connector.getSegments().get(i);
                    IAutoShape step = shapes.addAutoShape(ShapeType.Ellipse, segment.getFrom().getX().floatValue(), segment.getFrom().getY().floatValue(), 1f, 1f);
                    connect(shapes, last, step);
                    last = step;
                }
                IAutoShape endShape = reactionShapeParts.get(reactionPart.getId());
                connect(shapes, last, endShape);
            }
        }
    }

    private void createReactionShape(IShapeCollection shapes) {
        IGroupShape groupShape = shapes.addGroupShape();

        Shape rShape = edge.getReactionShape();
        reactionShape = PPTXShape.renderShape(groupShape, rShape);
        reactionShapeParts.put(edge.getId(), reactionShape);

        if (hasCatalyst()) {
            for (ReactionPart reactionPart : edge.getCatalysts()) {
                PPTXNode catalyst = nodesMap.get(reactionPart.getId());
                for (Connector connector : catalyst.getConnectors()) {
                    Shape shape = connector.getEndShape();
                    IAutoShape cs = PPTXShape.renderShape(groupShape, shape);
                    reactionShapeParts.put(reactionPart.getId(), cs);
                }
            }
        }

        //TODO: Missing regulators
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
        return edge.getInputs().size() == 1 && nodesMap.get(edge.getInputs().get(0).getId()).getConnectors().get(0).getSegments().isEmpty();
    }

    private boolean hasInputs() {
        return edge.getInputs() != null && !edge.getInputs().isEmpty();
    }

    private boolean hasOutputs() {
        return edge.getOutputs() != null && !edge.getOutputs().isEmpty();
    }

    private boolean hasCatalyst() {
        return edge.getCatalysts() != null && !edge.getCatalysts().isEmpty();
    }

    private boolean onlyOneOutputWithoutConnectors() {
        return edge.getOutputs().size() == 1 && nodesMap.get(edge.getOutputs().get(0).getId()).getConnectors().get(0).getSegments().isEmpty();
    }
}
