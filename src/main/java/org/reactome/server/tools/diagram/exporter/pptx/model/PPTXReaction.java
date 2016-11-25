package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.*;

import java.awt.Color;
import java.util.Map;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class PPTXReaction {

    private Edge edge;
    private Map<Long, PPTXNode> nodesMap;

    private IAutoShape reactionShape;

    public PPTXReaction(Edge edge, Map<Long, PPTXNode> nodesMap) {
        this.edge = edge;
        this.nodesMap = nodesMap;
    }

    public void render(IShapeCollection shapes) {

        //Reaction shape
        this.reactionShape = PPTXShape.renderShape(edge.getReactionShape(), shapes);

        if (hasInputs()) {
            Segment segment = edge.getSegments().get(0); //Input backbone
            if (onlyOneInputWithoutConnectors()) {
                IAutoShape input = nodesMap.get(edge.getInputs().get(0).getId()).getiAutoShape();
                connect(shapes, input, reactionShape);
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

    }

    private void createConnectorsFromInputs(IShapeCollection shapes, Coordinate anchor) {
        final IAutoShape backboneStart = shapes.addAutoShape(ShapeType.Ellipse, anchor.getX().floatValue(), anchor.getY().floatValue(), 1f, 1f);
        connect(shapes, reactionShape, backboneStart); //Drawing the backbone
        for (ReactionPart reactionPart : edge.getInputs()) {
            PPTXNode input = nodesMap.get(reactionPart.getId());
            IAutoShape last = backboneStart;
            for (Connector connector : input.getConnectors()) {
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
        return edge.getInputs().size() > 0;
    }

    private boolean hasOutputs() {
        return edge.getOutputs().size() > 0;
    }

    private boolean onlyOneOutputWithoutConnectors() {
        return edge.getOutputs().size() == 1 && nodesMap.get(edge.getOutputs().get(0).getId()).getConnectors().get(0).getSegments().isEmpty();
    }
}
