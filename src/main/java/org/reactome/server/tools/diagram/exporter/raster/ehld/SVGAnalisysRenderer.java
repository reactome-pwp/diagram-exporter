package org.reactome.server.tools.diagram.exporter.raster.ehld;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.SVGConstants;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisClient;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.*;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.profiles.AnalysisSheet;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;
import org.reactome.server.tools.diagram.exporter.raster.profiles.GradientSheet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.apache.batik.anim.dom.SVGDOMImplementation.SVG_NAMESPACE_URI;
import static org.apache.batik.util.SVGConstants.*;

public class SVGAnalisysRenderer {
	static final String REGION_ = "REGION-";
	private static final int HALF_FONT_SIZE = 4;
	private static final String OVERLAY_TEXT_STYLE = "{ fill: #000000 !important; stroke:#000000; stroke-width:0.5px }";
	private static final String ANALYSIS_INFO_STYLE = "{ opacity: 1 !important; -webkit-transition: all .9s ease-in-out;  -moz-transition: all .9s ease-in-out; transition: all .9s ease-in-out;}";
	private static final String HIT_BASE_COLOR = "#FFFFFF";
	private static final String OVERLAY_TEXT_CLASS = "ST-OVERLAY-TEXT";
	private static final String OVERLAY_BASE_ = "OVERLAYBASE-";
	private static final String OVERLAY_CLONE_ = "OVERLAYCLONE-";
	private static final String CLIPPING_PATH = "CLIPPINGPATH-";
	private static final String OVERLAY_ = "OVERLAY-";
	private static final String ANALINFO = "ANALINFO";
	private static final String ANALYSIS_INFO_CLASS = "ST-ANALYSIS-INFO";
	private static final double MIN_ENRICHMENT = 0.05;
	static final double THRESHOLD = 0.05;
	private static final String HIT_BASIS_STROKE_COLOUR = "#000000";
	private static final String HIT_BASIS_STROKE_WIDTH = "0.5";
	private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#.##E0", DecimalFormatSymbols.getInstance(Locale.UK));
	private static final Color DEFAULT_ANALYSIS_COLOR = new Color(194, 194, 194);
	private static final double OVERLAY_OPACITY = 0.9;
	private final static GVTBuilder builder = new GVTBuilder();
	private final static BridgeContext context = new BridgeContext(new UserAgentAdapter());
	/*
	 * If you are asking why there is a cloned Document here, the short answer
	 * is: text centering. The long answer is that we ar using a builder to
	 * calculate centers, but the builder, somehow is breaking the original
	 * document. So I created a copy of the document just to calculate the
	 * center of the ANALINFO boxes.
	 */
	private static Document cloned;

	public static void analysis(SVGDocument document, RasterArgs args) {
		if (args.getToken() == null) return;
		final java.util.List<String> regions = getRegions(document);
		if (regions.isEmpty()) return;
		try {
			final AnalysisResult result = AnalysisClient.getAnalysisResult(args.getToken());
			final java.util.List<ResourceSummary> summaryList = result.getResourceSummary();
			final ResourceSummary resourceSummary = summaryList.size() == 2
					? summaryList.get(1)
					: summaryList.get(0);
			final String resource = resourceSummary.getResource();
			AnalysisType analysisType = AnalysisType.getType(result.getSummary().getType());
			if (analysisType != AnalysisType.OVERREPRESENTATION) return;

			final List<String> pathways = regions.stream()
					.map(id -> id.substring(REGION_.length()))
					.collect(Collectors.toList());

			final Map<String, EntityStatistics> ratios = getRatios(args, resource, pathways);

			SVGUtil.addInlineStyle(document, OVERLAY_TEXT_CLASS, OVERLAY_TEXT_STYLE);
			SVGUtil.addInlineStyle(document, ANALYSIS_INFO_CLASS, ANALYSIS_INFO_STYLE);

			SVGLegendRenderer.legend(document, args);

			// Calculate document dimensions
			// Must be done to get children dimensions
			cloned = (Document) document.cloneNode(true);
			builder.build(context, cloned);

			pathways.forEach(s -> {
				final EntityStatistics stats = ratios.getOrDefault(s, null);
				overlay(document, args, s, stats);
				analysisInfo(document, s, stats);
			});

		} catch (AnalysisServerError | AnalysisException analysisServerError) {
			analysisServerError.printStackTrace();
		}
	}

	private static Map<String, EntityStatistics> getRatios(RasterArgs args, String resource, List<String> pathways) throws AnalysisServerError, AnalysisException {
		final PathwaySummary[] pathwaysSummary = AnalysisClient.getPathwaysSummary(pathways, args.getToken(), resource);

		final Map<String, EntityStatistics> ratios = new HashMap<>();
		for (PathwaySummary summary : pathwaysSummary)
			ratios.put(summary.getStId(), summary.getEntities());
		return ratios;
	}

	private static List<String> getRegions(SVGDocument document) {
		final NodeList childNodes = document.getRootElement().getChildNodes();
		return IntStream.range(0, childNodes.getLength())
				.mapToObj(childNodes::item)
				.filter(SVGElement.class::isInstance)
				.map(SVGElement.class::cast)
				.map(SVGElement::getId)
				.filter(id -> id.startsWith(REGION_))
				.collect(Collectors.toList());
	}

	private static void overlay(SVGDocument document, RasterArgs args, String stId, EntityStatistics stats) {
		if (stats == null) {
			createClip(document, stId, 0.0);
			createOverlayNodes(document, args.getProfiles().getAnalysisSheet(), stId, 0.0);
		} else {
			int found = stats.getFound();
			int total = stats.getTotal();
			final Double pValue = stats.getpValue();
			double percentage = (double) found / total;
			if (percentage < MIN_ENRICHMENT && percentage > 0)
				percentage = MIN_ENRICHMENT;
			createClip(document, stId, percentage);
			createOverlayNodes(document, args.getProfiles().getAnalysisSheet(), stId, pValue);
			if (args.getSelected().contains(stId)) {
				final Color selection = args.getProfiles().getDiagramSheet().getProperties().getSelection();
				SVGLegendRenderer.tick(document, pValue, selection);
			}
		}

	}

	private static void createClip(SVGDocument document, String stId, double percentage) {
		final Element rect = document.createElementNS(SVG_NAMESPACE_URI, SVG_RECT_TAG);
		rect.setAttribute(SVG_X_ATTRIBUTE, "0");
		rect.setAttribute(SVG_Y_ATTRIBUTE, "0");
		rect.setAttribute(SVG_HEIGHT_ATTRIBUTE, "1");
		rect.setAttribute(SVG_WIDTH_ATTRIBUTE, String.format(Locale.UK, "%.2f", percentage));
		rect.setAttribute(SVG_RX_ATTRIBUTE, "0");
		rect.setAttribute(SVG_RY_ATTRIBUTE, "0");
		final Element clip = document.createElementNS(SVG_NAMESPACE_URI, SVG_CLIP_PATH_TAG);
		clip.setAttribute(SVG_CLIP_PATH_UNITS_ATTRIBUTE, SVG_OBJECT_BOUNDING_BOX_VALUE);
		clip.setAttribute(SVG_ID_ATTRIBUTE, CLIPPING_PATH + stId);
		clip.appendChild(rect);
		SVGUtil.appendToDefs(document, clip);
	}

	private static void createOverlayNodes(SVGDocument document, AnalysisSheet sheet, String stId, Double pValue) {
		// OVERLAY
		//  - OVERLAYBASE (white)
		//  - OVERLAYCLONE (enrichment)
		//  - texts
		final Element overlay = document.getElementById(OVERLAY_ + stId);

		final Element base = (Element) overlay.cloneNode(true);
		// remove text elements
		final NodeList texts = base.getElementsByTagName(SVG_TEXT_TAG);
		IntStream.range(0, texts.getLength())
				.mapToObj(texts::item)
				.forEach(base::removeChild);
		base.setAttribute(SVG_ID_ATTRIBUTE, OVERLAY_BASE_ + stId);
		base.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, HIT_BASE_COLOR);
		base.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, HIT_BASIS_STROKE_COLOUR);
		base.setAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, HIT_BASIS_STROKE_WIDTH);

		final Element clone = (Element) overlay.cloneNode(true);
		Color analysisColor = getAnalysisColor(sheet, pValue);
		clone.setAttribute(SVG_ID_ATTRIBUTE, OVERLAY_CLONE_ + stId);
		clone.setAttribute(SVGConstants.SVG_CLIP_PATH_ATTRIBUTE, SVGUtil.toURL(CLIPPING_PATH + stId));
		clone.removeAttribute(SVGConstants.SVG_FILTER_ATTRIBUTE);
		clone.removeAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE);

		// Batik does not support rgba format colors, only HEX + opacity
		clone.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, ColorFactory.hex(analysisColor));
		clone.setAttribute(SVGConstants.SVG_OPACITY_ATTRIBUTE, String.valueOf(OVERLAY_OPACITY));

		final Element group = document.createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
		group.appendChild(base);
		group.appendChild(clone);
		// Add texts to group
		final NodeList textss = clone.getElementsByTagName(SVG_TEXT_TAG);
		IntStream.range(0, textss.getLength())
				.mapToObj(textss::item)
				.map(Element.class::cast)
				.forEach(node -> {
					SVGUtil.addClass(node, OVERLAY_TEXT_CLASS);
					group.appendChild(node);
				});
		overlay.appendChild(group);
		removeAttributeFromChildren(base, SVG_CLASS_ATTRIBUTE);
		removeAttributeFromChildren(clone, SVG_CLASS_ATTRIBUTE);
	}

	private static void removeAttributeFromChildren(Element element, String attribute) {
		if (element == null) return;
		final NodeList childNodes = element.getChildNodes();
		IntStream.range(0, childNodes.getLength())
				.mapToObj(childNodes::item)
				.filter(Element.class::isInstance)
				.map(Element.class::cast)
				.forEach(child -> {
					child.removeAttribute(attribute);
					removeAttributeFromChildren(child, attribute);
				});
	}

	private static Color getAnalysisColor(AnalysisSheet sheet, Double pValue) {
		Color color;
		if (pValue > 0 && pValue < THRESHOLD) {
			final GradientSheet gradient = sheet.getEnrichment().getGradient();
			color = ColorFactory.interpolate(gradient, pValue / THRESHOLD);
		} else color = DEFAULT_ANALYSIS_COLOR;
		return color;
	}

	private static void analysisInfo(SVGDocument document, String stId, EntityStatistics stats) {
		if (stats == null) return;
		final Element region = document.getElementById(REGION_ + stId);
		streamChildren(region)
				.filter(Element.class::isInstance)
				.map(Element.class::cast)
				.filter(element -> element.getAttribute(SVG_ID_ATTRIBUTE).startsWith(ANALINFO))
				.findFirst()
				.ifPresent(analysisInfo -> {
					makeVisible(analysisInfo);
					setAnalysisInfoText(analysisInfo, stats);
				});
	}

	private static void makeVisible(Element analysisInfo) {
		SVGUtil.addClass(analysisInfo, ANALYSIS_INFO_CLASS);
	}

	private static void setAnalysisInfoText(Element element, EntityStatistics entities) {
		final NodeList texts = element.getElementsByTagName(SVG_TEXT_TAG);
		if (texts.getLength() == 0) return;
		final Element text = (Element) texts.item(0);

		final Integer found = entities.getFound();
		final Integer total = entities.getTotal();
		String msg = String.format("Hit: %d/%d - FDR: %s", found, total,
				NUMBER_FORMAT.format(entities.getFdr()));
		text.setTextContent(msg);

//		// Center text
		final Element clonedElement = cloned.getElementById(element.getAttribute(SVG_ID_ATTRIBUTE));
		final GraphicsNode box = builder.build(context, clonedElement);

		double centerX = box.getSensitiveBounds().getCenterX();
		// Vertical centering must be done manually, since BATIK does not
		// support aligment-baseline either dominant-baseline
		double centerY = box.getSensitiveBounds().getCenterY() + HALF_FONT_SIZE;

		text.removeAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE);
		text.setAttribute(SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, SVG_MIDDLE_VALUE);
		text.setAttribute(SVGConstants.SVG_X_ATTRIBUTE, String.valueOf(centerX));
		text.setAttribute(SVGConstants.SVG_Y_ATTRIBUTE, String.valueOf(centerY));
	}

	private static Stream<Node> streamChildren(Element element) {
		return IntStream.range(0, element.getChildNodes().getLength())
				.mapToObj(element.getChildNodes()::item);
	}

}
