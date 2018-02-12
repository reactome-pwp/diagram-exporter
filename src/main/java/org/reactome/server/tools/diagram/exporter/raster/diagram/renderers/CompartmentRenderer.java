package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers;

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

import java.awt.*;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Renderer for compartments
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class CompartmentRenderer {

	private static final Coordinate GWU_CORRECTION = CoordinateFactory.get(14, 18);

	private Shape outer(Compartment node) {
		return ShapeFactory.roundedRectangle(node.getProp());
	}

	private Shape inner(Compartment item) {
		if (item.getInsets() == null)
			return null;
		final Bound bound = item.getInsets();
		return ShapeFactory.roundedRectangle(bound.getX(), bound.getY(),
				bound.getWidth(), bound.getHeight());
	}


	public void draw(DiagramCanvas canvas, List<Compartment> compartments, ColorProfiles profile, DiagramIndex index) {
		final Stroke stroke = StrokeStyle.BORDER.get(false);
		final Color fill;
		final Color innerColor;
		final Color text;
		final Color border;
		if (index.getAnalysis().getType() == null) {
			border = profile.getDiagramSheet().getCompartment().getStroke();
			text = profile.getDiagramSheet().getCompartment().getText();
			fill = profile.getDiagramSheet().getCompartment().getFill();
			// Inner color is the sum of the fill color with itself
			innerColor = ColorFactory.blend(fill, fill);
		} else {
			border = profile.getDiagramSheet().getCompartment().getLighterStroke();
			text = profile.getDiagramSheet().getCompartment().getLighterText();
			fill = profile.getDiagramSheet().getCompartment().getLighterFill();
			// Inner color is the sum of the fill color with itself
			innerColor = ColorFactory.blend(fill, fill);
		}

		final List<Shape> outer = compartments.stream()
				.map(this::outer)
				.collect(Collectors.toCollection(ArrayList::new));
		final List<Shape> inner = compartments.stream()
				.map(this::inner)
				.collect(Collectors.toCollection(ArrayList::new));

		// Instead of painting both rectangles for each compartment
		// we fill the inner one, but for the outer we paint only the residual
		// space. That means that we are setting each pixel only once!
		for (int i = 0; i < outer.size(); i++) {
			final Area out = new Area(outer.get(i));
			if (inner.get(i) != null) {
				final Area inn = new Area(inner.get(i));
				out.subtract(inn);
				canvas.getCompartmentFill().add(inn, innerColor);
				canvas.getCompartmentBorder().add(inn, border, stroke);
			}
			canvas.getCompartmentFill().add(out, fill);
			canvas.getCompartmentBorder().add(outer.get(i), border, stroke);
		}
		compartments.forEach(compartment ->
				canvas.getCompartmentText().add(text, compartment.getDisplayName(),
						compartment.getTextPosition().add(GWU_CORRECTION), FontProperties.DEFAULT_FONT));
	}

}
