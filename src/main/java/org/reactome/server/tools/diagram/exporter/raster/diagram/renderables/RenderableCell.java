package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesImpl;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramData;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.StrokeStyle;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.FillLayer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.TextLayer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.awt.*;
import java.awt.geom.Area;

public class RenderableCell extends RenderableNode {


    private static final double PADDING = 2;
    public static final int CELL_ARC = 16;


    RenderableCell(Node node) {
        super(node);
    }

    @Override
    StrokeStyle getBorderStroke() {
        return StrokeStyle.SEGMENT;
    }

    @Override
    Shape backgroundShape() {
        return ShapeFactory.roundedRectangle(getNode().getProp(), 0, CELL_ARC);
    }

    @Override
    public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
        return colorProfiles.getDiagramSheet().getCell();
    }

    @Override
    public void draw(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramData data, int t) {
        super.draw(canvas, colorProfiles, data, t);
        // Inner shape
        final Color border = getStrokeColor(colorProfiles, data.getAnalysis().getType());
        final Shape shape = ShapeFactory.roundedRectangle(getNode().getProp(), PADDING, CELL_ARC);
        final Stroke stroke = StrokeStyle.SEGMENT.get(isDashed());
        final DrawLayer strokeLayer = isFadeOut()
                ? canvas.getFadeOutNodeBorder()
                : canvas.getNodeBorder();
        final FillLayer fillLayer = isFadeOut()
                ? canvas.getFadeOutNodeBackground()
                : canvas.getNodeBackground();

        strokeLayer.add(shape, border, stroke);

        //nucleus
        NodeColorSheet cellNucleus = colorProfiles.getDiagramSheet().getCellNucleus();
        final Color nBorder = cellNucleus.getStroke();
        final Color nFill = cellNucleus.getFill();
        final Stroke nStroke = StrokeStyle.SEGMENT.get(false);
        NodeProperties nProp = getNode().getProp();
        nProp = new NodePropertiesImpl(nProp.getX(), nProp.getY(), nProp.getWidth(), nProp.getHeight() / 2 + 3 * PADDING);

        // Outer nucleus
        Shape nShape = ShapeFactory.roundedRectangle(nProp, 3 * PADDING, CELL_ARC);
        fillLayer.add(new Area(nShape), nFill);
        strokeLayer.add(nShape, nBorder, nStroke);

        // Inner nucleus
        nShape = ShapeFactory.roundedRectangle(nProp, 4 * PADDING, CELL_ARC);
        strokeLayer.add(nShape, nBorder, nStroke);

    }

    @Override
    protected void text(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramData data, double textSplit) {
        final TextLayer layer = isFadeOut()
                ? canvas.getFadeOutText()
                : canvas.getText();
        final Color color = getTextColor(colorProfiles, data.getAnalysis().getType());
        NodeProperties prop = getNode().getProp();
        prop = new NodePropertiesImpl(prop.getX(), prop.getY() + prop.getHeight() / 2, prop.getWidth(), prop.getHeight() / 2);
        layer.add(getNode().getDisplayName(),
                color,
                prop,
                PADDING + 2,
                textSplit,
                FontProperties.DEFAULT_FONT);
    }
}
