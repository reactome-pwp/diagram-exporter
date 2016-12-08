package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.IAutoShape;
import com.aspose.slides.IShapeCollection;
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

    public PPTXLink(Link link, Map<Long, PPTXNode> nodesMap) {
        this.link = link;
        this.nodesMap = nodesMap;

        switch (link.getRenderableClass()) {
            case "EntitySetAndMemberLink":
            case "EntitySetAndEntitySetLink":
                this.dashed = true;
                break;
            default:
                this.dashed = false;
        }
    }

    public void render(IShapeCollection shapes, DiagramProfile profile) {
        Stylesheet stylesheet = new Stylesheet(profile.getLink());

        // There is only ONE input and ONE output in the link objects
        PPTXNode from = nodesMap.get(link.getInputs().get(0).getId());
        IAutoShape last = from.getiAutoShape();
        for (Segment segment : link.getSegments()) {
            IAutoShape step = renderAuxiliaryShape(shapes, segment.getFrom());
            if (from != null) {
                connect(shapes, from, last, step, false, dashed);
            } else {
                drawSegment(shapes, last, step, dashed);
            }
            from = null;
            last = step;
        }
        PPTXNode to = nodesMap.get(link.getOutputs().get(0).getId());
        connect(shapes, to, last, to.getiAutoShape(), false, dashed);

        //TODO: Render end-shape
    }
}
