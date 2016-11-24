package org.reactome.server.tools.diagram.exporter.pptx.model;

import java.awt.geom.Point2D;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class PPTXSegment {

    private Point2D.Double from;
    private Point2D.Double to;
    private SegmentStyle segmentStyle;

    public PPTXSegment(Point2D.Double from, Point2D.Double to){
        this.from = from;
        this.to = to;
    }

    public Long getAnchor(){
        return 0L;
    }

    enum SegmentStyle { LINE, DASHED }
}
