package org.reactome.server.tools.diagram.exporter.raster.ehld;

import org.apache.batik.util.SVGConstants;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.apache.batik.util.SVGConstants.SVG_ID_ATTRIBUTE;

class SVGDecoratorFactory {

	private static final String SELECTION_FILTER = "selection-filter";
	private static final String FLAG_FILTER = "flag-filter";
	private static final String SELECTION_FLAG_FILTER = "selection-flag-filter";

	static void selectAndFlag(SVGDocument document, RasterArgs args) {
		addFilters(document, args);

		final Set<String> selAndFlag = new HashSet<>(args.getSelected());
		selAndFlag.retainAll(args.getFlags());
		setFilter(document, selAndFlag, SVGUtil.toURL(SELECTION_FLAG_FILTER));

		final Set<String> selected = new HashSet<>(args.getSelected());
		selected.removeAll(selAndFlag);
		setFilter(document, selected, SVGUtil.toURL(SELECTION_FILTER));

		final Set<String> flag = new HashSet<>(args.getFlags());
		flag.removeAll(selAndFlag);
		setFilter(document, flag, SVGUtil.toURL(FLAG_FILTER));

		if (!selAndFlag.isEmpty() || !selected.isEmpty() || ! flag.isEmpty())
			addFilters(document, args);
	}

	private static void addFilters(SVGDocument document, RasterArgs args) {
		final Color selection = args.getProfiles().getDiagramSheet().getProperties().getSelection();
		final Element selectionFilter = SVGFilterFactory.createBorderFilter(document, selection, 4, "selection");
		selectionFilter.setAttribute(SVG_ID_ATTRIBUTE, SELECTION_FILTER);

		final Color flagColor = args.getProfiles().getDiagramSheet().getProperties().getFlag();
		final Element flagFilter = SVGFilterFactory.createBorderFilter(document, flagColor, 7, "flag");
		flagFilter.setAttribute(SVG_ID_ATTRIBUTE, FLAG_FILTER);

		final Element flagAndSelectionFilter = SVGFilterFactory.combineFilters(document, flagFilter, selectionFilter);
		flagAndSelectionFilter.setAttribute(SVG_ID_ATTRIBUTE, SELECTION_FLAG_FILTER);

		SVGUtil.appendToDefs(document, selectionFilter, flagFilter, flagAndSelectionFilter);
	}

	private static void setFilter(Document document, Set<String> ids, String filter) {
		ids.stream()
				.map(stId -> SVGAnalisysRenderer.REGION_ + stId)
				.map(document::getElementById)
				.filter(Objects::nonNull)
				.forEach(element -> element.setAttribute(SVGConstants.SVG_FILTER_TAG, filter));
	}

}
