package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.*;

import java.awt.Color;
import java.awt.*;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class CompartmentRenderer extends AbstractRenderer {

	@Override
	public void draw(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items, Paint fill, Paint border, Paint textColor, Stroke segmentStroke, Stroke borderStroke) {
		final Collection<Compartment> compartments = (Collection<Compartment>) items;
		if (fill != null) fill(graphics, fill, compartments);
		if (border != null) border(graphics, border, compartments);
		if (textColor != null) text(graphics, textColor, compartments);
	}

	private void fill(AdvancedGraphics2D graphics, Paint fill, Collection<Compartment> compartments) {
		final List<Shape> outer = compartments.stream()
				.map(compartment -> outer(compartment, graphics.getFactor()))
				.collect(Collectors.toCollection(ArrayList::new));
		final List<Shape> inner = compartments.stream()
				.map(compartment -> inner(compartment, graphics.getFactor()))
				.collect(Collectors.toCollection(ArrayList::new));

		graphics.getGraphics().setPaint(fill);
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

	private void text(AdvancedGraphics2D graphics, Paint textColor, Collection<Compartment> compartments) {
		graphics.getGraphics().setPaint(textColor);
		compartments.forEach(compartment -> graphics.drawTextSingleLine(compartment.getDisplayName(), compartment.getTextPosition()));
	}

	private void border(AdvancedGraphics2D graphics, Paint border, Collection<Compartment> compartments) {
		graphics.getGraphics().setPaint(border);
		compartments.stream()
				.map(NodeCommon::getProp)
				.map(props -> new IntNodeProperties(new ScaledNodeProperties(props, graphics.getFactor())))
				.forEach(props -> draw(graphics, props));
		compartments.stream()
				.map(NodeCommon::getInsets)
				.filter(Objects::nonNull)
				.map(bound -> new IntBound(new ScaledBound(bound, graphics.getFactor())))
				.forEach(bound -> draw(graphics, bound));
	}

	private void draw(AdvancedGraphics2D graphics, IntNodeProperties props) {
		graphics.getGraphics().drawRoundRect(
				props.intX(),
				props.intY(),
				props.intWidth(),
				props.intHeight(),
				(int) RendererProperties.ROUND_RECT_ARC_WIDTH,
				(int) RendererProperties.ROUND_RECT_ARC_WIDTH
		);
	}

	private void draw(AdvancedGraphics2D graphics, IntBound props) {
		graphics.getGraphics().drawRoundRect(
				props.intX(),
				props.intY(),
				props.intWidth(),
				props.intHeight(),
				(int) RendererProperties.ROUND_RECT_ARC_WIDTH,
				(int) RendererProperties.ROUND_RECT_ARC_WIDTH
		);
	}

	private void fill(AdvancedGraphics2D graphics, IntNodeProperties props) {
		graphics.getGraphics().fillRoundRect(
				props.intX(),
				props.intY(),
				props.intWidth(),
				props.intHeight(),
				(int) RendererProperties.ROUND_RECT_ARC_WIDTH,
				(int) RendererProperties.ROUND_RECT_ARC_WIDTH
		);
	}

	private void fill(AdvancedGraphics2D graphics, IntBound props) {
		graphics.getGraphics().fillRoundRect(
				props.intX(),
				props.intY(),
				props.intWidth(),
				props.intHeight(),
				(int) RendererProperties.ROUND_RECT_ARC_WIDTH,
				(int) RendererProperties.ROUND_RECT_ARC_WIDTH
		);
	}

	public Shape outer(DiagramObject item, double factor) {
		final Compartment compartment = (Compartment) item;
		final NodeProperties properties = new ScaledNodeProperties(compartment.getProp(), factor);
		return new RoundRectangle2D.Double(
				properties.getX(),
				properties.getY(),
				properties.getWidth(),
				properties.getHeight(),
				RendererProperties.ROUND_RECT_ARC_WIDTH,
				RendererProperties.ROUND_RECT_ARC_WIDTH);
	}

	public Shape inner(DiagramObject item, double factor) {
		final Compartment compartment = (Compartment) item;
		if (compartment.getInsets() == null)
			return null;
		final Bound bound = new ScaledBound(compartment.getInsets(), factor);
		return new RoundRectangle2D.Double(
				bound.getX(),
				bound.getY(),
				bound.getWidth(),
				bound.getHeight(),
				RendererProperties.ROUND_RECT_ARC_WIDTH,
				RendererProperties.ROUND_RECT_ARC_WIDTH);
	}

	Color blend(Color a, Color b) {
		int alpha = a.getAlpha() + (b.getAlpha() - a.getAlpha()) / 2;
		int red = a.getRed() + (b.getRed() - a.getRed()) / 2;
		int green = a.getGreen() + (b.getGreen() - a.getGreen()) / 2;
		int blue = a.getBlue() + (b.getBlue() - a.getBlue()) / 2;

		return new Color(red, green, blue, alpha);
	}

}
