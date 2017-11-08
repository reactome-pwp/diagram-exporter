package org.reactome.server.tools.diagram.exporter.raster.ehld;

import org.apache.batik.util.SVGConstants;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.util.Locale;
import java.util.Scanner;

import static org.apache.batik.anim.dom.SVGDOMImplementation.SVG_NAMESPACE_URI;
import static org.apache.batik.util.SVGConstants.*;

class SVGLegendRenderer {

	private static final String GRADIENT_BOX = "gradient-box";
	private static final double RX = 8.0;
	private static final String LEGEND_BACKGROUND_STYLE = "fill:#dcdcdc; stroke-width:0.5; stroke:#666666; stroke-opacity:1";
	private static final double BG_PADDING = 10;
	private static final double LEGEND_WIDTH = 40;
	private static final double LEGEND_HEIGHT = 240;
	private static final String LEGEND_GRADIENT = "legend-gradient";
	private static final double TEXT_PADDING = 2;
	private static final int FONT_SIZE = 12;

	static void legend(SVGDocument document, RasterArgs args) {
		// Legend dims: to be put as constants
		final double bgWidth = LEGEND_WIDTH + 2 * BG_PADDING;
		final double bgHeight = LEGEND_HEIGHT + 2 * BG_PADDING;
		final double spaceWidth = bgWidth + 2 * BG_PADDING;
//		final double spaceHeight = bgHeight + 2 * BG_PADDING;

		final String viewBox = document.getRootElement().getAttribute(SVGConstants.SVG_VIEW_BOX_ATTRIBUTE);
		final Scanner scanner = new Scanner(viewBox);
		scanner.useLocale(Locale.UK);
		scanner.nextFloat();  // x
		scanner.nextFloat();  // y
		double width = scanner.nextFloat();
		double height = scanner.nextFloat();

		setDiagramSize(document, width + spaceWidth, height);

		// - g id=LEGEND
		//     - rect background
		//     - rect gradient fill=url(#legend-gradient)
		//     - text topText
		//     - text bottomText
		// - defs
		//     - linearGradient id=legend-gradient
		//         - stop min
		//         - stop max

		final Element legendGradient = createGradient(document, args);
		SVGUtil.appendToDefs(document, legendGradient);

		final Element legend = document.createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
		legend.setAttribute(SVG_ID_ATTRIBUTE, "LEGEND");

		final Element background = getBackground(document, width, height, bgWidth, bgHeight);
		final Element gradientBox = getGradientBox(document, width, height);

		final double centerX = width + 2 * BG_PADDING + 0.5 * LEGEND_WIDTH;

		final Element topText = document.createElementNS(SVG_NAMESPACE_URI, SVG_TEXT_TAG);
		topText.setAttribute(SVG_X_ATTRIBUTE, String.valueOf(centerX));
		topText.setAttribute(SVG_Y_ATTRIBUTE, String.valueOf((height - LEGEND_HEIGHT) * 0.5 - TEXT_PADDING));
		topText.setAttribute(SVG_TEXT_ANCHOR_ATTRIBUTE, SVG_MIDDLE_VALUE);
		topText.setAttribute(SVG_STYLE_ATTRIBUTE, "font-size: " + FONT_SIZE);
		topText.setTextContent("0");

		final Element bottomText = document.createElementNS(SVG_NAMESPACE_URI, SVG_TEXT_TAG);
		bottomText.setAttribute(SVG_X_ATTRIBUTE, String.valueOf(centerX));
		bottomText.setAttribute(SVG_Y_ATTRIBUTE, String.valueOf((height + LEGEND_HEIGHT) * 0.5 + TEXT_PADDING + FONT_SIZE));
		bottomText.setAttribute(SVG_TEXT_ANCHOR_ATTRIBUTE, SVG_MIDDLE_VALUE);
		bottomText.setAttribute(SVG_STYLE_ATTRIBUTE, "font-size: " + FONT_SIZE);
		bottomText.setTextContent(String.valueOf(SVGAnalisysRenderer.THRESHOLD));

		legend.appendChild(background);
		legend.appendChild(gradientBox);
		legend.appendChild(topText);
		legend.appendChild(bottomText);

		document.getRootElement().appendChild(legend);
	}

	private static Element getBackground(SVGDocument document, double diagramWidth, double diagramHeight, double width, double height) {
		final Element background = document.createElementNS(SVG_NAMESPACE_URI, SVG_RECT_TAG);
		background.setAttribute(SVG_RX_ATTRIBUTE, String.valueOf(RX));
		background.setAttribute(SVG_RY_ATTRIBUTE, String.valueOf(RX));
		background.setAttribute(SVG_X_ATTRIBUTE, String.valueOf(diagramWidth + BG_PADDING));
		background.setAttribute(SVG_Y_ATTRIBUTE, String.valueOf((diagramHeight - height) * 0.5 - TEXT_PADDING - FONT_SIZE));
		background.setAttribute(SVG_WIDTH_ATTRIBUTE, String.valueOf(width));
		background.setAttribute(SVG_HEIGHT_ATTRIBUTE, String.valueOf(height + 2 * (TEXT_PADDING + FONT_SIZE)));
		background.setAttribute(SVG_STYLE_ATTRIBUTE, LEGEND_BACKGROUND_STYLE);
		return background;
	}

	private static Element getGradientBox(SVGDocument document, double width, double height) {
		final Element gradientBox = document.createElementNS(SVG_NAMESPACE_URI, SVG_RECT_TAG);
		gradientBox.setAttribute(SVG_ID_ATTRIBUTE, GRADIENT_BOX);
		gradientBox.setAttribute(SVG_X_ATTRIBUTE, String.valueOf(width + 2 * BG_PADDING));
		gradientBox.setAttribute(SVG_Y_ATTRIBUTE, String.valueOf((height - LEGEND_HEIGHT) * 0.5));
		gradientBox.setAttribute(SVG_WIDTH_ATTRIBUTE, String.valueOf(LEGEND_WIDTH));
		gradientBox.setAttribute(SVG_HEIGHT_ATTRIBUTE, String.valueOf(LEGEND_HEIGHT));
		gradientBox.setAttribute(SVG_FILL_ATTRIBUTE, SVGUtil.toURL(LEGEND_GRADIENT));
		return gradientBox;
	}

	private static void setDiagramSize(SVGDocument document, double width, double height) {
		final String newVB = String.format(Locale.UK, "0 0 %.3f %.3f", width, height);
		document.getRootElement().setAttribute(SVG_VIEW_BOX_ATTRIBUTE, newVB);
	}

	private static Element createGradient(SVGDocument document, RasterArgs args) {
		final Element legendGradient = document.createElementNS(SVG_NAMESPACE_URI, SVG_LINEAR_GRADIENT_TAG);
		legendGradient.setAttribute(SVG_ID_ATTRIBUTE, LEGEND_GRADIENT);
		legendGradient.setAttribute(SVG_X1_ATTRIBUTE, "0");
		legendGradient.setAttribute(SVG_X2_ATTRIBUTE, "0");
		legendGradient.setAttribute(SVG_Y1_ATTRIBUTE, "0");
		legendGradient.setAttribute(SVG_Y2_ATTRIBUTE, "1");

		final Element startColor = document.createElementNS(SVG_NAMESPACE_URI, SVG_STOP_TAG);
		final Color sc = args.getProfiles().getAnalysisSheet().getEnrichment().getGradient().getMin();
		startColor.setAttribute(SVG_STYLE_ATTRIBUTE, String.format("stop-color: %s", ColorFactory.hex(sc)));
		startColor.setAttribute(SVG_OFFSET_ATTRIBUTE, "0");

		final Element endColor = document.createElementNS(SVG_NAMESPACE_URI, SVG_STOP_TAG);
		final Color ec = args.getProfiles().getAnalysisSheet().getEnrichment().getGradient().getMax();
		endColor.setAttribute(SVG_STYLE_ATTRIBUTE, String.format("stop-color: %s", ColorFactory.hex(ec)));
		endColor.setAttribute(SVG_OFFSET_ATTRIBUTE, "1");
		legendGradient.appendChild(startColor);
		legendGradient.appendChild(endColor);
		return legendGradient;
	}

	/** LEGEND should already be created */
	public static void tick(SVGDocument document, double value, Color color) {
		// - g id=LEGEND
		//     - ...
		//     - gradient-box
		//     - tick
		//          - line
		//          - polygon

		final String hexColor = ColorFactory.hex(color);

		// Find legend box, ith should exist
		final Element box = document.getElementById(GRADIENT_BOX);
		final double x = Double.valueOf(box.getAttribute(SVG_X_ATTRIBUTE));
		final double y = Double.valueOf(box.getAttribute(SVG_Y_ATTRIBUTE));
		final double w = Double.valueOf(box.getAttribute(SVG_WIDTH_ATTRIBUTE));
		final double h = Double.valueOf(box.getAttribute(SVG_HEIGHT_ATTRIBUTE));

		final double y1 = value * h / SVGAnalisysRenderer.THRESHOLD + y;

		// 1 Line
		// y1 - y    val - 0
		// ------ = ---------
		//   h         THR
		final Element line = document.createElementNS(SVG_NAMESPACE_URI, SVG_LINE_TAG);
		line.setAttribute(SVG_X1_ATTRIBUTE, String.valueOf(x));
		line.setAttribute(SVG_X2_ATTRIBUTE, String.valueOf(x + w));
		line.setAttribute(SVG_Y1_ATTRIBUTE, String.valueOf(y1));
		line.setAttribute(SVG_Y2_ATTRIBUTE, String.valueOf(y1));
		line.setAttribute(SVG_STROKE_ATTRIBUTE, hexColor);

		box.getParentNode().appendChild(line);

		// 2 Triangle
		final Element tri = document.createElementNS(SVG_NAMESPACE_URI, SVG_POLYGON_TAG);
		final String points = String.format(Locale.UK, "%.2f,%.2f, %.2f,%.2f %.2f,%.2f",
				x + w, y1,
				x + w + 5, y1 - 5,
				x + w + 5, y1 + 5);
		tri.setAttribute(SVG_POINTS_ATTRIBUTE, points);
		tri.setAttribute(SVG_FILL_ATTRIBUTE, hexColor);
		box.getParentNode().appendChild(tri);
	}
}
