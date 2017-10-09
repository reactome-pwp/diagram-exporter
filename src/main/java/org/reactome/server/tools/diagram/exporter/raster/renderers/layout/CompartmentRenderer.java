package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Bound;
import org.reactome.server.tools.diagram.data.layout.Compartment;
import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledBound;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.awt.*;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Renderer for compartments
 */
public class CompartmentRenderer extends NodeAbstractRenderer {

	@Override
	public void fill(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		final Collection<Compartment> compartments = (Collection<Compartment>) items;
		final List<Shape> outer = compartments.stream()
				.map(compartment -> outer(compartment, graphics.getFactor()))
				.collect(Collectors.toCollection(ArrayList::new));
		final List<Shape> inner = compartments.stream()
				.map(compartment -> inner(compartment, graphics.getFactor()))
				.collect(Collectors.toCollection(ArrayList::new));

		final Paint fill = graphics.getGraphics().getPaint();
		// Instead of painting both rectangles for each compartment
		// we fill the inner one, but for the outer we paint only the residual
		// space. That means that we are setting each pixel only once
		for (int i = 0; i < outer.size(); i++) {
			final Area out = new Area(outer.get(i));
			if (inner.get(i) != null) {
				final Area inn = new Area(inner.get(i));
				out.subtract(inn);
			}
			graphics.getGraphics().fill(out);
		}
		// Inner color is the sum of the fill color with itself
		graphics.getGraphics().setPaint(blend((Color) fill, (Color) fill));
		inner.stream().filter(Objects::nonNull).forEach(shape -> graphics.getGraphics().fill(shape));
	}

	@Override
	public void text(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		final Collection<Compartment> compartments = (Collection<Compartment>) items;
		compartments.forEach(compartment ->
				TextRenderer.drawTextSingleLine(graphics,
						compartment.getDisplayName(),
						compartment.getTextPosition()));
	}

	@Override
	public void border(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		final Collection<Compartment> compartments = (Collection<Compartment>) items;
		compartments.stream()
				.map(node -> outer(node, graphics.getFactor()))
				.forEach(graphics.getGraphics()::draw);
		compartments.stream()
				.filter(node -> node.getInsets() != null)
				.map(node -> inner(node, graphics.getFactor()))
				.forEach(graphics.getGraphics()::draw);
	}

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

	private Color blend(Color a, Color b) {
		int alpha = a.getAlpha() + (b.getAlpha() - a.getAlpha()) / 2;
		int red = a.getRed() + (b.getRed() - a.getRed()) / 2;
		int green = a.getGreen() + (b.getGreen() - a.getGreen()) / 2;
		int blue = a.getBlue() + (b.getBlue() - a.getBlue()) / 2;

		return new Color(red, green, blue, alpha);
	}

}
