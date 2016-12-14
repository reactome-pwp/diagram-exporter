package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.Link;
import org.reactome.server.tools.diagram.data.layout.Segment;
import org.reactome.server.tools.diagram.exporter.common.profiles.model.DiagramProfile;

import java.util.Map;

import static org.reactome.server.tools.diagram.exporter.pptx.util.PPTXShape.renderAuxiliaryShape;
import static org.reactome.server.tools.diagram.exporter.pptx.util.SegmentUtil.connect;
import static org.reactome.server.tools.diagram.exporter.pptx.util.SegmentUtil.drawSegment;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class PPTXLink {

    private Link link;
    private Map<Long, PPTXNode> nodesMap;
    private boolean dashed;
    private boolean renderBeginArrow;

    public PPTXLink(Link link, Map<Long, PPTXNode> nodesMap) {
        this.link = link;
        this.nodesMap = nodesMap;

        switch (link.getRenderableClass()) {
            case "EntitySetAndMemberLink":
            case "EntitySetAndEntitySetLink":
                this.dashed = true;
                this.renderBeginArrow = false;
                break;
            default:
                this.dashed = false;
                this.renderBeginArrow = true;
        }
    }

    public void render(IShapeCollection shapes, DiagramProfile profile) {
        Stylesheet stylesheet = new Stylesheet(profile.getLink(), FillType.Solid, FillType.Solid, LineStyle.Single);
        stylesheet.setFillColor(stylesheet.getLineColor()); // fill color is white in the profile
        stylesheet.setLineWidth(2);

        stylesheet.setLineArrowheadLength(LineArrowheadLength.Long);
        stylesheet.setLineArrowheadStyle(LineArrowheadStyle.Triangle);
        stylesheet.setLineArrowheadWidth(LineArrowheadWidth.Wide);

        if (dashed) {
            stylesheet.setLineDashStyle(LineDashStyle.Dash);
        }

        // There is only ONE input and ONE output in the link objects
        PPTXNode from = nodesMap.get(link.getInputs().get(0).getId());
        IAutoShape last = from.getiAutoShape();
        for (Segment segment : link.getSegments()) {
            IAutoShape step = renderAuxiliaryShape(shapes, segment.getFrom(), stylesheet);
            if (from != null) {
                connect(shapes, from, last, step, false, stylesheet);
            } else {
                drawSegment(shapes, last, step, stylesheet);
            }
            from = null;
            last = step;
        }

        PPTXNode to = nodesMap.get(link.getOutputs().get(0).getId());
        // last segments connects the shape to the last auxiliary node,
        // then we can reuse the connect method and use the beginArrow
        connect(shapes, to, to.getiAutoShape(), last, renderBeginArrow, stylesheet);
    }
}
