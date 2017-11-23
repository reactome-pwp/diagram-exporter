package org.reactome.server.tools.diagram.exporter.raster.ehld;

import org.apache.batik.util.SVGConstants;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGElement;

import java.awt.*;

import static org.apache.batik.util.SVGConstants.*;

class SVGFilterFactory {

	static Element createBorderFilter(Document document, Color color, double width, String prefix) {

		// Create a layer with the color
		final Element matrix = document.createElementNS(SVG_NAMESPACE_URI, SVG_FE_COLOR_MATRIX_TAG);
		matrix.setAttribute(SVG_IN_ATTRIBUTE, SVG_SOURCE_GRAPHIC_VALUE);
		matrix.setAttribute(SVG_VALUES_ATTRIBUTE, ColorFactory.getColorMatrix(color));
		matrix.setAttribute(SVG_RESULT_ATTRIBUTE, prefix + 1);
		matrix.setAttribute(SVG_TYPE_ATTRIBUTE, SVG_MATRIX_VALUE);

		// Dilate color layer
		final Element morpho = document.createElementNS(SVG_NAMESPACE_URI, SVG_FE_MORPHOLOGY_TAG);
		morpho.setAttribute(SVG_IN_ATTRIBUTE, prefix + 1);
		morpho.setAttribute(SVG_OPERATOR_ATTRIBUTE, SVG_DILATE_VALUE);
		morpho.setAttribute(SVG_RADIUS_ATTRIBUTE, String.valueOf(width));
		morpho.setAttribute(SVG_RESULT_ATTRIBUTE, prefix + 2);

		// Merge dilated with original
		final Element merge = document.createElementNS(SVG_NAMESPACE_URI, SVG_FE_MERGE_TAG);
		final Element source = document.createElementNS(SVG_NAMESPACE_URI, SVG_FE_MERGE_NODE_TAG);
		source.setAttribute(SVG_IN_ATTRIBUTE, SVG_SOURCE_GRAPHIC_VALUE);
		final Element in = document.createElementNS(SVG_NAMESPACE_URI, SVG_FE_MERGE_NODE_TAG);
		in.setAttribute(SVG_IN_ATTRIBUTE, prefix + 2);
		merge.appendChild(in);
		merge.appendChild(source);
		merge.setAttribute(SVG_RESULT_ATTRIBUTE, prefix + "Result");

		final Element filter = document.createElementNS(SVG_NAMESPACE_URI, SVG_FILTER_TAG);
		filter.setAttribute(SVGConstants.SVG_X_ATTRIBUTE, "-25%");
		filter.setAttribute(SVGConstants.SVG_Y_ATTRIBUTE, "-25%");
		filter.setAttribute(SVGConstants.SVG_WIDTH_ATTRIBUTE, "150%");
		filter.setAttribute(SVGConstants.SVG_HEIGHT_ATTRIBUTE, "150%");

		filter.appendChild(matrix);
		filter.appendChild(morpho);
		filter.appendChild(merge);
		filter.setAttribute(SVG_STYLE_ATTRIBUTE, "color-interpolation-filters:sRGB");
		return filter;
	}

	/**
	 * Creates a new filter by adding a copy of all child of filter1 and filter2
	 * and merging the result of filter1 and filter2.
	 *
	 * @param filter1 first filter
	 * @param filter2 second filter
	 */
	static Element combineFilters(Document document, Element filter1, Element filter2) {
		final Element filter = document.createElementNS(SVG_NAMESPACE_URI, SVG_FILTER_TAG);
		filter.setAttribute(SVG_STYLE_ATTRIBUTE, "color-interpolation-filters:sRGB");
		for (int i = 0; i < filter1.getChildNodes().getLength(); i++)
			filter.appendChild(filter1.getChildNodes().item(i).cloneNode(true));
		for (int i = 0; i < filter2.getChildNodes().getLength(); i++)
			filter.appendChild(filter2.getChildNodes().item(i).cloneNode(true));
		final Element merge = document.createElementNS(SVG_NAMESPACE_URI, SVG_FE_MERGE_TAG);
		final Element merge1 = document.createElementNS(SVG_NAMESPACE_URI, SVG_FE_MERGE_NODE_TAG);
		final Element merge2 = document.createElementNS(SVG_NAMESPACE_URI, SVG_FE_MERGE_NODE_TAG);
		final SVGElement last1 = (SVGElement) filter1.getLastChild();
		final SVGElement last2 = (SVGElement) filter2.getLastChild();
		merge1.setAttribute(SVG_IN_ATTRIBUTE, last1.getAttribute(SVG_RESULT_ATTRIBUTE));
		merge2.setAttribute(SVG_IN_ATTRIBUTE, last2.getAttribute(SVG_RESULT_ATTRIBUTE));
		merge.appendChild(merge1);
		merge.appendChild(merge2);
		filter.appendChild(merge);
		return filter;
	}

}
