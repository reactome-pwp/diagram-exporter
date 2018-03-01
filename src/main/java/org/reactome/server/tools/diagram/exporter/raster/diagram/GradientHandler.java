package org.reactome.server.tools.diagram.exporter.raster.diagram;

import org.apache.batik.svggen.*;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.awt.*;
import java.awt.image.BufferedImageOp;

import static org.apache.batik.util.SVGConstants.*;

public class GradientHandler implements ExtensionHandler {

	@Override
	public SVGPaintDescriptor handlePaint(Paint paint,
	                                      SVGGeneratorContext generatorCtx) {
		if (paint instanceof LinearGradientPaint) {
			LinearGradientPaint gradient = (LinearGradientPaint) paint;
			String id = generatorCtx.getIDGenerator().generateID("gradient");
			Document doc = generatorCtx.getDOMFactory();
			final Element grad = createGradient(doc, gradient, id);
			return new SVGPaintDescriptor
					("url(#" + id + ")", SVGSyntax.SVG_OPAQUE_VALUE, grad);
		} else if (paint instanceof GradientPaint) {
			GradientPaint gradient = (GradientPaint) paint;

			String id = generatorCtx.getIDGenerator().generateID("gradient");
			Document doc = generatorCtx.getDOMFactory();
			Element grad = createGradient(doc, gradient, id);

			return new SVGPaintDescriptor
					("url(#" + id + ")", SVGSyntax.SVG_OPAQUE_VALUE, grad);
		} else {
			// Let the default mechanism do its job.
			return null;
		}
	}


	private Element createGradient(Document document, GradientPaint gradient, String id) {
		final Element legendGradient = document.createElementNS(SVG_NAMESPACE_URI, SVG_LINEAR_GRADIENT_TAG);
		legendGradient.setAttribute(SVG_ID_ATTRIBUTE, id);
		legendGradient.setAttribute(SVG_X1_ATTRIBUTE, "0");
		legendGradient.setAttribute(SVG_X2_ATTRIBUTE, "0");
		legendGradient.setAttribute(SVG_Y1_ATTRIBUTE, "1");
		legendGradient.setAttribute(SVG_Y2_ATTRIBUTE, "0");

		final Element startColor = document.createElementNS(SVG_NAMESPACE_URI, SVG_STOP_TAG);
		startColor.setAttribute(SVG_STOP_COLOR_ATTRIBUTE, ColorFactory.hex(gradient.getColor1()));
		startColor.setAttribute(SVG_OFFSET_ATTRIBUTE, "0");
		legendGradient.appendChild(startColor);

		final Element endColor = document.createElementNS(SVG_NAMESPACE_URI, SVG_STOP_TAG);
		endColor.setAttribute(SVG_STOP_COLOR_ATTRIBUTE, ColorFactory.hex(gradient.getColor2()));
		endColor.setAttribute(SVG_OFFSET_ATTRIBUTE, "1");
		legendGradient.appendChild(endColor);

		return legendGradient;
	}

	private Element createGradient(Document document, LinearGradientPaint gradient, String id) {
		// This comes from a 3-color-gradient
		final Element legendGradient = document.createElementNS(SVG_NAMESPACE_URI, SVG_LINEAR_GRADIENT_TAG);
		legendGradient.setAttribute(SVG_ID_ATTRIBUTE, id);
		legendGradient.setAttribute(SVG_X1_ATTRIBUTE, "0");
		legendGradient.setAttribute(SVG_X2_ATTRIBUTE, "0");
		legendGradient.setAttribute(SVG_Y1_ATTRIBUTE, "1");
		legendGradient.setAttribute(SVG_Y2_ATTRIBUTE, "0");

		final Element startColor = document.createElementNS(SVG_NAMESPACE_URI, SVG_STOP_TAG);
		startColor.setAttribute(SVG_STOP_COLOR_ATTRIBUTE, ColorFactory.hex(gradient.getColors()[0]));
		startColor.setAttribute(SVG_OFFSET_ATTRIBUTE, "0");
		legendGradient.appendChild(startColor);


		final Element stopColor = document.createElementNS(SVG_NAMESPACE_URI, SVG_STOP_TAG);
		stopColor.setAttribute(SVG_STOP_COLOR_ATTRIBUTE, ColorFactory.hex(gradient.getColors()[1]));
		stopColor.setAttribute(SVG_OFFSET_ATTRIBUTE, "0.5");
		legendGradient.appendChild(stopColor);


		final Element endColor = document.createElementNS(SVG_NAMESPACE_URI, SVG_STOP_TAG);
		endColor.setAttribute(SVG_STOP_COLOR_ATTRIBUTE, ColorFactory.hex(gradient.getColors()[2]));
		endColor.setAttribute(SVG_OFFSET_ATTRIBUTE, "1");
		legendGradient.appendChild(endColor);

		return legendGradient;
	}

	@Override
	public SVGCompositeDescriptor handleComposite(Composite composite, SVGGeneratorContext generatorContext) {
		return null;
	}

	@Override
	public SVGFilterDescriptor handleFilter(BufferedImageOp filter, Rectangle filterRect, SVGGeneratorContext generatorContext) {
		return null;
	}
}
