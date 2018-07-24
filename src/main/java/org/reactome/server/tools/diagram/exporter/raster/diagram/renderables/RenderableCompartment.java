package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Bound;
import org.reactome.server.tools.diagram.data.layout.Compartment;
import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.impl.CoordinateFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.StrokeStyle;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.awt.*;
import java.awt.geom.Area;

public class RenderableCompartment extends RenderableNodeCommon<Compartment> {

	private static final Coordinate GWU_CORRECTION = CoordinateFactory.get(14, 18);

	RenderableCompartment(Compartment compartment) {super(compartment);}

	@Override
	public NodeColorSheet getColorProfile(ColorProfiles colorProfiles) {
		return colorProfiles.getDiagramSheet().getCompartment();
	}

	private Shape outer() {
		return ShapeFactory.roundedRectangle(getDiagramObject().getProp());
	}

	private Shape inner() {
		if (getDiagramObject().getInsets() == null)
			return null;
		final Bound bound = getDiagramObject().getInsets();
		return ShapeFactory.roundedRectangle(bound.getX(), bound.getY(),
				bound.getWidth(), bound.getHeight());
	}

	@Override
	public void draw(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		final Stroke stroke = StrokeStyle.BORDER.get(false);
		final Color fill = getFillColor(colorProfiles, index.getAnalysis().getType());
		final Color border = getStrokeColor(colorProfiles, index.getAnalysis().getType());
		final Color text = getTextColor(colorProfiles, index.getAnalysis().getType());
		// Inner color is the sum of the fill color with itself
		final Color innerColor = ColorFactory.blend(fill, fill);
		final Shape outer = outer();
		final Shape inner = inner();
		// Instead of painting both rectangles for each compartment
		// we fill the inner one, but for the outer we paint only the residual
		// space. That means that we are setting each pixel only once!
		final Area out = new Area(outer);
		if (inner != null) {
			final Area inn = new Area(inner);
			out.subtract(inn);
			canvas.getCompartmentFill().add(inn, innerColor);
			canvas.getCompartmentBorder().add(inn, border, stroke);
		}
		canvas.getCompartmentFill().add(out, fill);
		canvas.getCompartmentBorder().add(outer, border, stroke);
		canvas.getCompartmentText().add(text, getDiagramObject().getDisplayName(),
				getDiagramObject().getTextPosition().add(GWU_CORRECTION), FontProperties.DEFAULT_FONT);
	}
}
