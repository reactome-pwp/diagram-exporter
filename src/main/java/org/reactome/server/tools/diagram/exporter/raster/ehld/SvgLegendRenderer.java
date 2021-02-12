package org.reactome.server.tools.diagram.exporter.raster.ehld;

import org.apache.batik.util.SVGConstants;
import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;
import org.reactome.server.tools.diagram.exporter.raster.profiles.GradientSheet;
import org.reactome.server.tools.diagram.exporter.raster.profiles.RegulationSheet;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

import static org.apache.batik.util.SVGConstants.*;

/**
 * Methods to add a legend to a document, including ticks.
 */
class SvgLegendRenderer {

	private static final DecimalFormat EXPRESSION_FORMAT = new DecimalFormat("#.##E0", DecimalFormatSymbols.getInstance(Locale.UK));
	private static final DecimalFormat ENRICHMENT_FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.UK));

	private static final String GRADIENT_BOX = "gradient-box";
	private static final String POLYGON_BOX = "polygon-box";
	private static final double RX = 8.0;
	private static final String LEGEND_BACKGROUND_STYLE = "fill:#dcdcdc; stroke-width:0.5; stroke:#666666; stroke-opacity:1";
	private static final double BG_PADDING = 10;
	private static final double LEGEND_WIDTH = 40;
	private static final double LEGEND_HEIGHT = 240;
	private static final String LEGEND_GRADIENT = "legend-gradient";
	private static final String LEGEND_REGULATION = "legend-regulation";
	private static final double TEXT_PADDING = 2;
	private static final int FONT_SIZE = 12;
	private static final String TICKS = "ticks";
	private static final  String SYMBOL_STYLE = "fill: #FFFFFF; font: bold 13px Arial;";
	private final static double NUMBER_OF_SHAPES = 5.0;
	private final static double NUMBER_OF_STOP_COLORS_ELEMENT = 8.0;

	private final static String[] SYMBOLS = {"\u25B2\u25B2",
			"\u25B2",
			"-",
			"\u25BC",
			"\u25BC\u25BC",
	};
	// adjust the position of the symbols in the regulation legend
	private final static double[][] SYMBOLS_CORRECTION = {
			{13d, 6d},
			{6d, 4d},
			{3d, 2d},
			{6d, 4d},
			{13d, 6d}
	};
	// create the regulation legend in linearGradient
	// the first value is from ColorMap which helps to get color
	// the second value is the stop color offset value which helps to create solid color
	private final static double[][] STOPCOLOR_AND_OFFSET_VALUE = {
			{2d, 0.2d},
			{1d, 0.2d},
			{1d, 0.4d},
			{0d, 0.4d},
			{0d, 0.6d},
			{-1d, 0.6d},
			{-1d, 0.8d},
			{-2d, 0.8d}
	};

	static void legend(SVGDocument document, GradientSheet gradientSheet, double top, double bottom, AnalysisType analysisType) {
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

		final Element legendGradient = (analysisType == AnalysisType.GSA_REGULATION
				|| analysisType == AnalysisType.GSA_STATISTICS
				|| analysisType == AnalysisType.GSVA) ? createRegulationGradient(document, gradientSheet) : createGradient(document, gradientSheet);

		SvgUtil.appendToDefs(document, legendGradient);

		final Element legend = document.createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
		legend.setAttribute(SVG_ID_ATTRIBUTE, "LEGEND");

		final Element background = getBackground(document, width, height, bgWidth, bgHeight);

		final Element gradientBox = (analysisType == AnalysisType.GSA_REGULATION
				|| analysisType == AnalysisType.GSA_STATISTICS
				|| analysisType == AnalysisType.GSVA) ? getRegulationBox(document, width, height) : getGradientBox(document, width, height);

		final double centerX = width + 2 * BG_PADDING + 0.5 * LEGEND_WIDTH;

		final DecimalFormat formatter = (analysisType == AnalysisType.EXPRESSION
				|| analysisType == AnalysisType.GSA_REGULATION
				|| analysisType == AnalysisType.GSA_STATISTICS
				|| analysisType == AnalysisType.GSVA) ? EXPRESSION_FORMAT : ENRICHMENT_FORMAT;

		final String topLabel = (analysisType == AnalysisType.GSA_REGULATION
				|| analysisType == AnalysisType.GSA_STATISTICS
				|| analysisType == AnalysisType.GSVA) ? "Up-regulated" : formatter.format(top);

		final String bottomLabel = (analysisType == AnalysisType.GSA_REGULATION
				|| analysisType == AnalysisType.GSA_STATISTICS
				|| analysisType == AnalysisType.GSVA) ? "Down-regulated" : formatter.format(bottom);

		final int fontSize = (analysisType == AnalysisType.GSA_REGULATION
				|| analysisType == AnalysisType.GSA_STATISTICS
				|| analysisType == AnalysisType.GSVA) ? 6 : 12;

		final Element topText = document.createElementNS(SVG_NAMESPACE_URI, SVG_TEXT_TAG);
		topText.setAttribute(SVG_X_ATTRIBUTE, String.valueOf(centerX));
		topText.setAttribute(SVG_Y_ATTRIBUTE, String.valueOf((height - LEGEND_HEIGHT) * 0.5 - TEXT_PADDING));
		topText.setAttribute(SVG_TEXT_ANCHOR_ATTRIBUTE, SVG_MIDDLE_VALUE);
		topText.setAttribute(SVG_STYLE_ATTRIBUTE, String.format("font-size: %dpx;font-family: arial; font-weight:bold;", fontSize));
		topText.setTextContent(topLabel);

		final Element bottomText = document.createElementNS(SVG_NAMESPACE_URI, SVG_TEXT_TAG);
		bottomText.setAttribute(SVG_X_ATTRIBUTE, String.valueOf(centerX));
		bottomText.setAttribute(SVG_Y_ATTRIBUTE, String.valueOf((height + LEGEND_HEIGHT) * 0.5 + TEXT_PADDING + fontSize));
		bottomText.setAttribute(SVG_TEXT_ANCHOR_ATTRIBUTE, SVG_MIDDLE_VALUE);
		bottomText.setAttribute(SVG_STYLE_ATTRIBUTE, String.format("font-size: %dpx;font-family: arial; font-weight:bold;", fontSize));
		bottomText.setTextContent(bottomLabel);

		legend.appendChild(background);
		legend.appendChild(gradientBox);
		legend.appendChild(topText);
		legend.appendChild(bottomText);

		document.getRootElement().appendChild(legend);

		/** LEGEND should already be created */
		if (analysisType == AnalysisType.GSA_REGULATION
				|| analysisType == AnalysisType.GSA_STATISTICS
				|| analysisType == AnalysisType.GSVA) {
			final Element polygonBox = createPolygonSymbols(document);
			legend.appendChild(polygonBox);
		}
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

	private static Element getRegulationBox(SVGDocument document, double width, double height) {
		final Element regulationBox = document.createElementNS(SVG_NAMESPACE_URI, SVG_RECT_TAG);
		regulationBox.setAttribute(SVG_ID_ATTRIBUTE, GRADIENT_BOX);
		regulationBox.setAttribute(SVG_X_ATTRIBUTE, String.valueOf(width + 2 * BG_PADDING));
		regulationBox.setAttribute(SVG_Y_ATTRIBUTE, String.valueOf((height - LEGEND_HEIGHT) * 0.5));
		regulationBox.setAttribute(SVG_WIDTH_ATTRIBUTE, String.valueOf(LEGEND_WIDTH));
		regulationBox.setAttribute(SVG_HEIGHT_ATTRIBUTE, String.valueOf(LEGEND_HEIGHT));
		regulationBox.setAttribute(SVG_FILL_ATTRIBUTE, SvgUtil.toURL(LEGEND_REGULATION));
		return regulationBox ;
	}

	private static Element getGradientBox(SVGDocument document, double width, double height) {
		final Element gradientBox = document.createElementNS(SVG_NAMESPACE_URI, SVG_RECT_TAG);
		gradientBox.setAttribute(SVG_ID_ATTRIBUTE, GRADIENT_BOX);
		gradientBox.setAttribute(SVG_X_ATTRIBUTE, String.valueOf(width + 2 * BG_PADDING));
		gradientBox.setAttribute(SVG_Y_ATTRIBUTE, String.valueOf((height - LEGEND_HEIGHT) * 0.5));
		gradientBox.setAttribute(SVG_WIDTH_ATTRIBUTE, String.valueOf(LEGEND_WIDTH));
		gradientBox.setAttribute(SVG_HEIGHT_ATTRIBUTE, String.valueOf(LEGEND_HEIGHT));
		gradientBox.setAttribute(SVG_FILL_ATTRIBUTE, SvgUtil.toURL(LEGEND_GRADIENT));
		return gradientBox;
	}

	private static Element createRegulationGradient(SVGDocument document, GradientSheet gradient) {
		final Element legendRegulation = document.createElementNS(SVG_NAMESPACE_URI, SVG_LINEAR_GRADIENT_TAG);
		legendRegulation.setAttribute(SVG_ID_ATTRIBUTE, LEGEND_REGULATION);
		legendRegulation.setAttribute(SVG_X1_ATTRIBUTE, "0");
		legendRegulation.setAttribute(SVG_X2_ATTRIBUTE, "0");
		legendRegulation.setAttribute(SVG_Y1_ATTRIBUTE, "0");
		legendRegulation.setAttribute(SVG_Y2_ATTRIBUTE, "1");

		RegulationSheet regulationSheet = new RegulationSheet(gradient);
		Map<Integer, Color> colorMap = regulationSheet.getColorMap();

		//This uses multi-position color stops, with adjacent colors having the same color stop value, creating a striped effect.
		// example:
		// <svg height="600" width="600">
		//   <linearGradient id="solids" x1="0%" y1="0%" x2="0%" y2="100%">
		//     <stop offset="20%" stop-color="#D65C33" />
		//     <stop offset="20%" stop-color="#EAAD40" />
		//     <stop offset="40%" stop-color="#EAAD40" />
		//     <stop offset="40%" stop-color="#EAEAEA" />
		//     <stop offset="60%" stop-color="#EAEAEA" />
		//     <stop offset="60%"stop-color="#C2D640" />
		//     <stop offset="80%" stop-color="#C2D640" />
		//     <stop offset="80%" stop-color="#85AD33" />
		//   </linearGradient>
        // </svg>
		// <rect width="600" height="200" fill="url(#solids)" />

		for (int i = 0; i < NUMBER_OF_STOP_COLORS_ELEMENT; i++) {
			final Element stopColor = document.createElementNS(SVG_NAMESPACE_URI, SVG_STOP_TAG);
			stopColor.setAttribute(SVG_STOP_COLOR_ATTRIBUTE, ColorFactory.hex(colorMap.get((int) STOPCOLOR_AND_OFFSET_VALUE[i][0])));
			stopColor.setAttribute(SVG_OFFSET_ATTRIBUTE, String.valueOf(STOPCOLOR_AND_OFFSET_VALUE[i][1]));
			legendRegulation.appendChild(stopColor);
		}
		return legendRegulation;
	}

	private static void setDiagramSize(SVGDocument document, double width, double height) {
		final String newVB = String.format(Locale.UK, "0 0 %.3f %.3f", width, height);
		document.getRootElement().setAttribute(SVG_VIEW_BOX_ATTRIBUTE, newVB);
	}

	private static Element createGradient(SVGDocument document, GradientSheet gradient) {
		final Element legendGradient = document.createElementNS(SVG_NAMESPACE_URI, SVG_LINEAR_GRADIENT_TAG);
		legendGradient.setAttribute(SVG_ID_ATTRIBUTE, LEGEND_GRADIENT);
		legendGradient.setAttribute(SVG_X1_ATTRIBUTE, "0");
		legendGradient.setAttribute(SVG_X2_ATTRIBUTE, "0");
		legendGradient.setAttribute(SVG_Y1_ATTRIBUTE, "0");
		legendGradient.setAttribute(SVG_Y2_ATTRIBUTE, "1");

		final Element startColor = document.createElementNS(SVG_NAMESPACE_URI, SVG_STOP_TAG);
		startColor.setAttribute(SVG_STOP_COLOR_ATTRIBUTE, ColorFactory.hex(gradient.getMin()));
		startColor.setAttribute(SVG_OFFSET_ATTRIBUTE, "0");
		legendGradient.appendChild(startColor);

		if (gradient.getStop() != null) {
			final Element stopColor = document.createElementNS(SVG_NAMESPACE_URI, SVG_STOP_TAG);
			stopColor.setAttribute(SVG_STOP_COLOR_ATTRIBUTE, ColorFactory.hex(gradient.getStop()));
			stopColor.setAttribute(SVG_OFFSET_ATTRIBUTE, "0.5");
			legendGradient.appendChild(stopColor);
		}

		final Element endColor = document.createElementNS(SVG_NAMESPACE_URI, SVG_STOP_TAG);
		endColor.setAttribute(SVG_STOP_COLOR_ATTRIBUTE, ColorFactory.hex(gradient.getMax()));
		endColor.setAttribute(SVG_OFFSET_ATTRIBUTE, "1");
		legendGradient.appendChild(endColor);

		return legendGradient;
	}

    /** LEGEND should already be created */
    public static Element createPolygonSymbols(SVGDocument document) {
		// - g id=LEGEND
		//     - ...
		//     - gradient-box
		//     - polygon-box
		//          - polygon (triangle)

		Element polygonBox = document.createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
		polygonBox.setAttribute(SVG_ID_ATTRIBUTE, POLYGON_BOX);
		// Find legend box, ith should exist
		final Element box = document.getElementById(GRADIENT_BOX);
		final double x = Double.parseDouble(box.getAttribute(SVG_X_ATTRIBUTE));
		final double y = Double.parseDouble(box.getAttribute(SVG_Y_ATTRIBUTE));
		final double w = Double.parseDouble(box.getAttribute(SVG_WIDTH_ATTRIBUTE));
		final double h = Double.parseDouble(box.getAttribute(SVG_HEIGHT_ATTRIBUTE));
		final double height = h / NUMBER_OF_SHAPES;

		//example:
		//final Element firstSymbols = document.createElementNS(SVG_NAMESPACE_URI, SVG_TEXT_TAG);
		//firstSymbols.setAttribute(SVG_X_ATTRIBUTE, String.valueOf(x + (w / 2.0) - 13.0));
		//firstSymbols.setAttribute(SVG_Y_ATTRIBUTE, String.valueOf(y + height * 0.5 + 6.0));
		//firstSymbols.setAttribute(SVG_STYLE_ATTRIBUTE, SYMBOL_STYLE);
		//firstSymbols.setTextContent("\u25B2\u25B2");
		for (int i = 0; i < NUMBER_OF_SHAPES; i++) {
			final Element text = document.createElementNS(SVG_NAMESPACE_URI, SVG_TEXT_TAG);
			text.setAttribute(SVG_X_ATTRIBUTE, String.valueOf(x + (w / 2.0) - SYMBOLS_CORRECTION[i][0]));
			text.setAttribute(SVG_Y_ATTRIBUTE, String.valueOf(y + i * height + height * 0.5 + SYMBOLS_CORRECTION[i][1]));
			text.setAttribute(SVG_STYLE_ATTRIBUTE, SYMBOL_STYLE);
			text.setTextContent(SYMBOLS[i]);
			polygonBox.appendChild(text);
		}
		return polygonBox;
    }

	/** LEGEND should already be created */
	public static void tick(SVGDocument document, double value, Color color) {
		// - g id=LEGEND
		//     - ...
		//     - gradient-box
		//     - ticks
		//          - line
		//          - polygon (triangle)

		final String hexColor = ColorFactory.hex(color);

		// Find legend box, ith should exist
		final Element box = document.getElementById(GRADIENT_BOX);
		final Element ticks = getOrCreateTicks(document);
		final double x = Double.valueOf(box.getAttribute(SVG_X_ATTRIBUTE));
		final double y = Double.valueOf(box.getAttribute(SVG_Y_ATTRIBUTE));
		final double w = Double.valueOf(box.getAttribute(SVG_WIDTH_ATTRIBUTE));
		final double h = Double.valueOf(box.getAttribute(SVG_HEIGHT_ATTRIBUTE));

		final double y1 = y + value * h;

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

		ticks.appendChild(line);

		// 2 Triangle
		final Element tri = document.createElementNS(SVG_NAMESPACE_URI, SVG_POLYGON_TAG);
		final String points = String.format(Locale.UK, "%.2f,%.2f, %.2f,%.2f %.2f,%.2f",
				x + w, y1,
				x + w + 5, y1 - 5,
				x + w + 5, y1 + 5);
		tri.setAttribute(SVG_POINTS_ATTRIBUTE, points);
		tri.setAttribute(SVG_FILL_ATTRIBUTE, hexColor);
		ticks.appendChild(tri);
	}

	public static void clearTicks(SVGDocument document) {
		// Find legend box, it should exist
		final Element ticks = getOrCreateTicks(document);
		final List<Node> children = new LinkedList<>();
		IntStream.range(0, ticks.getChildNodes().getLength())
				.mapToObj(ticks.getChildNodes()::item)
				.forEach(children::add);
		children.forEach(ticks::removeChild);

	}

	private static Element getOrCreateTicks(SVGDocument document) {
		Element ticks = document.getElementById(TICKS);
		if (ticks != null)
			return ticks;
		ticks = document.createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
		final Node legend = document.getElementById(GRADIENT_BOX).getParentNode();
		ticks.setAttribute(SVG_ID_ATTRIBUTE, TICKS);
		legend.appendChild(ticks);
		return ticks;
	}
}
