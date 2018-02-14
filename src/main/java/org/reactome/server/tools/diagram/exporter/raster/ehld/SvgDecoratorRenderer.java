package org.reactome.server.tools.diagram.exporter.raster.ehld;

import org.apache.batik.util.SVGConstants;
import org.reactome.server.tools.diagram.exporter.common.content.ContentServiceClient;
import org.reactome.server.tools.diagram.exporter.common.content.ContentServiceResponse;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.batik.util.SVGConstants.*;
import static org.reactome.server.tools.diagram.exporter.raster.ehld.SvgAnalysis.REGION_;

class SvgDecoratorRenderer {

	private static final String SELECTION_FILTER = "selection-filter";
	private static final String FLAG_FILTER = "flag-filter";
	private static final String SELECTION_FLAG_FILTER = "selection-flag-filter";

	static void selectAndFlag(SVGDocument document, RasterArgs args) {
		if (args.getSelected() == null && args.getFlags() == null)
			return;
		addFilters(document, args);
		final List<String> regions = getRegions(document);
		Collection<String> pathways = regions.stream()
				.map(id -> id.substring(REGION_.length()))
				.collect(Collectors.toList());

		final Set<String> selected = args.getSelected() == null
				? Collections.EMPTY_SET
				: new HashSet<>(args.getSelected());
		final Set<String> flags = args.getFlags() == null
				? Collections.EMPTY_SET
				: new HashSet<>(getFlaggedPathways(args, pathways));

		final Set<String> selAndFlag = new HashSet<>(selected);
		selAndFlag.retainAll(flags);
		setFilter(document, selAndFlag, SvgUtil.toURL(SELECTION_FLAG_FILTER));

		selected.removeAll(selAndFlag);
		setFilter(document, selected, SvgUtil.toURL(SELECTION_FILTER));

		flags.removeAll(selAndFlag);
		setFilter(document, flags, SvgUtil.toURL(FLAG_FILTER));

	}
	private static List<String> getRegions(SVGDocument document) {
		final NodeList groups = document.getRootElement().getElementsByTagNameNS(SVG_NAMESPACE_URI, SVG_G_TAG);
		return IntStream.range(0, groups.getLength())
				.mapToObj(groups::item)
				.filter(SVGElement.class::isInstance)
				.map(SVGElement.class::cast)
				.map(SVGElement::getId)
				.filter(id -> id.startsWith(REGION_))
				.collect(Collectors.toList());
	}


	private static void addFilters(SVGDocument document, RasterArgs args) {
		final Color selection = args.getProfiles().getDiagramSheet().getProperties().getSelection();
		final Element selectionFilter = SvgFilterFactory.createBorderFilter(document, selection, 4, "selection");
		selectionFilter.setAttribute(SVG_ID_ATTRIBUTE, SELECTION_FILTER);

		final Color flagColor = args.getProfiles().getDiagramSheet().getProperties().getFlag();
		final Element flagFilter = SvgFilterFactory.createBorderFilter(document, flagColor, 7, "flag");
		flagFilter.setAttribute(SVG_ID_ATTRIBUTE, FLAG_FILTER);

		final Element flagAndSelectionFilter = SvgFilterFactory.combineFilters(document, flagFilter, selectionFilter);
		flagAndSelectionFilter.setAttribute(SVG_ID_ATTRIBUTE, SELECTION_FLAG_FILTER);

		SvgUtil.appendToDefs(document, selectionFilter, flagFilter, flagAndSelectionFilter);
	}

	private static void setFilter(Document document, Set<String> ids, String filter) {
		ids.stream()
				.map(stId -> REGION_ + stId)
				.map(document::getElementById)
				.filter(Objects::nonNull)
				.forEach(element -> element.setAttribute(SVGConstants.SVG_STYLE_TAG, "filter:" + filter));
	}


	private static Collection<String> getFlaggedPathways(RasterArgs args, Collection<String> pathways) {
		if (args.getFlags() == null) return Collections.emptyList();
		final Set<String> pathwaysHit = new TreeSet<>();
		args.getFlags().forEach(s -> {
			final List<ContentServiceResponse> responses = ContentServiceClient.getFlagged(s, args.getStId(), pathways);
			if (responses == null) return;
			responses.forEach(contentServiceResponse -> pathwaysHit.add(contentServiceResponse.getStId()));
		});
		return pathwaysHit;
	}
}
