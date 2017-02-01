package org.reactome.server.tools.diagram.exporter.pptx.model;

import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.layout.impl.CoordinateFactory;

/**
 * Class used to rescale the diagram and all the elements present so then it will fit into the slide size. Taking
 * into account the rescaling is implemented because
 *
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class Adjustment {

    private static final double MAX_HEIGHT = 4032;
    private static final double MAX_WIDTH = 4032;
    private double factor = 1;
    private Double slideWidth;
    private Double slideHeight;
    private Coordinate coordinate;

    public Adjustment(Diagram diagram) {
        slideWidth = (diagram.getMaxX().doubleValue() - diagram.getMinX().doubleValue()) + 70;
        slideHeight = (diagram.getMaxY().doubleValue() - diagram.getMinY().doubleValue()) + 70;

        // size exceeded, calculate the factor
        if (slideWidth > MAX_WIDTH || slideHeight > MAX_HEIGHT) {
            double factorHeight = MAX_HEIGHT / slideHeight;
            double factorWidth = MAX_WIDTH / slideWidth;
            if (factorHeight < factorWidth) {
                factor = factorHeight;
                slideHeight = slideHeight * factorHeight;
//                slideWidth = slideWidth * factorHeight; // test
            } else {
                factor = factorWidth;
//                slideHeight = slideHeight * factorWidth; // test ( then we need the Coordinate :) )
                slideWidth = slideWidth * factorWidth;
            }
        }

        coordinate = CoordinateFactory.get(0, 0);
    }

    public double getFactor() {
        if (factor < 0 || factor > 1) System.out.println("Something weird here, factor should be between 0 and 1: " + factor);
        return factor;
    }

    public int getSlideHeight() {
        return slideHeight.intValue();
    }

    public int getSlideWidth() {
        return slideWidth.intValue();
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }
}
