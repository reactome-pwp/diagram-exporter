package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Bound;
import org.reactome.server.tools.diagram.data.layout.Compartment;
import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.raster.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledBound;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.StrokeProperties;

import java.awt.*;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Renderer for compartments
 */
public class CompartmentRenderer extends NodeAbstractRenderer {

	private Shape outer(Compartment node, double factor) {
		final NodeProperties properties = new ScaledNodeProperties(node.getProp(), factor);
		return ShapeFactory.roundedRectangle(properties.getX(),
				properties.getY(), properties.getWidth(),
				properties.getHeight());
	}

	private Shape inner(Compartment item, double factor) {
		if (item.getInsets() == null)
			return null;
		final Bound bound = new ScaledBound(item.getInsets(), factor);
		return ShapeFactory.roundedRectangle(bound.getX(), bound.getY(),
				bound.getWidth(), bound.getHeight());
	}


	public void draw(DiagramCanvas canvas, List<Compartment> compartments, DiagramProfile profile, double factor) {
		final String fillString = profile.getCompartment().getFill();
		final Paint fill = ColorFactory.parseColor(fillString);
		// Inner color is the sum of the fill color with itself
		final String innerColor = ColorFactory.asRgba(ColorFactory.blend((Color) fill, (Color) fill));
		final String border = profile.getCompartment().getStroke();
		final Stroke stroke = StrokeProperties.BORDER_STROKE;
		final String text = profile.getCompartment().getText();

		final List<Shape> outer = compartments.stream()
				.map(compartment -> outer(compartment, factor))
				.collect(Collectors.toCollection(ArrayList::new));
		final List<Shape> inner = compartments.stream()
				.map(compartment -> inner(compartment, factor))
				.collect(Collectors.toCollection(ArrayList::new));

		// Instead of painting both rectangles for each compartment
		// we fill the inner one, but for the outer we paint only the residual
		// space. That means that we are setting each pixel only once
		for (int i = 0; i < outer.size(); i++) {
			final Area out = new Area(outer.get(i));
			if (inner.get(i) != null) {
				final Area inn = new Area(inner.get(i));
				out.subtract(inn);
				canvas.getCompartmentFill().add(innerColor, inn);
				canvas.getCompartmentBorder().add(border, stroke, inn);
			}
			canvas.getCompartmentFill().add(fillString, out);
			canvas.getCompartmentBorder().add(border, stroke, outer.get(i));
		}

		compartments.forEach(compartment -> {
			final Coordinate position = compartment.getTextPosition().multiply(factor);
			canvas.getCompartmentText().add(text, compartment.getDisplayName(), position);
		});
	}

}
