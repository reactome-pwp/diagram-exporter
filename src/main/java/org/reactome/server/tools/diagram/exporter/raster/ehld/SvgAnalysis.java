package org.reactome.server.tools.diagram.exporter.raster.ehld;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.model.*;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;
import org.reactome.server.tools.diagram.exporter.raster.profiles.GradientSheet;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.apache.batik.util.SVGConstants.*;

public class SvgAnalysis {
	static final String REGION_ = "REGION-";

	private static final double MAX_P_VALUE = 0.05;
	private static final int TEXT_V_ALIGN = 4;
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
	private static final double MIN_OVERLAY_CLIP = 0.05;
	private static final String HIT_BASIS_STROKE_COLOUR = "#000000";
	private static final String HIT_BASIS_STROKE_WIDTH = "0.5";
	private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#.##E0", DecimalFormatSymbols.getInstance(Locale.UK));
	private static final Color DEFAULT_OVERLAY_COLOR = new Color(194, 194, 194);
	private static final double OVERLAY_OPACITY = 0.9;
	private static final String BOTTOM_TEXT = "bottom-text";
	private final GVTBuilder builder = new GVTBuilder();
	private final BridgeContext context = new BridgeContext(new UserAgentAdapter());
	private final SVGDocument document;
	private final RasterArgs args;
	/*
	 * If you are asking why there is a cloned Document here, the short answer
	 * is: text centering. The long answer is that we are using a builder to
	 * calculate centers, but the builder, somehow is breaking the original
	 * document. So we have created a copy of the document just to calculate the
	 * centers of the ANALINFO boxes.
	 */
	private Document cloned;
	private AnalysisStoredResult result;
	private Map<String, EntityStatistics> entityStats;
	private AnalysisType analysisType;
	private List<String> pathways;
	private String resource;
	private AnalysisResult summary;

	SvgAnalysis(SVGDocument document, RasterArgs args, AnalysisStoredResult result) {
		this.document = document;
		this.args = args;
		this.result = result;
		collectAnalysisResult();
	}

	@SuppressWarnings("Duplicates")
	private String getResource() {
		if (args.getResource() != null) return args.getResource();
		if (result == null) return null;
		final List<ResourceSummary> summaryList = result.getResourceSummary();
		final ResourceSummary resourceSummary = summaryList.size() == 2
				? summaryList.get(1)
				: summaryList.get(0);
		return resourceSummary.getResource();
	}

	private void collectAnalysisResult() {
		if (result == null) return;
		this.resource = getResource();
		analysisType = AnalysisType.getType(result.getSummary().getType());
		summary = result.getResultSummary(resource, args.isImportableOnly());
		final List<String> regions = getRegions();
		if (regions.isEmpty()) return;
		pathways = regions.stream()
				.map(id -> id.substring(REGION_.length()))
				.collect(Collectors.toList());
		entityStats = getStats();

	}

	private Map<String, EntityStatistics> getStats() {
		final List<PathwaySummary> pathwaysSummary = result.filterByPathways(pathways, resource, args.isImportableOnly());
		final Map<String, EntityStatistics> stats = new HashMap<>();
		for (PathwaySummary summary : pathwaysSummary)
			stats.put(summary.getStId(), summary.getEntities());
		return stats;
	}

	public void analysis() {
		if (result == null) return;
		switch (analysisType) {
			case OVERREPRESENTATION:
			case SPECIES_COMPARISON:
				enrichment();
				break;
			case EXPRESSION:
				expression();
				break;
			case GSA_REGULATION:
			case GSA_STATISTICS:
			case GSVA:
				gsa();
				break;
		}
	}

	private void enrichment() {
		SvgUtil.addInlineStyle(document, OVERLAY_TEXT_CLASS, OVERLAY_TEXT_STYLE);
		SvgUtil.addInlineStyle(document, ANALYSIS_INFO_CLASS, ANALYSIS_INFO_STYLE);
		final GradientSheet gradient = args.getProfiles().getAnalysisSheet().getEnrichment().getGradient();
		SvgLegendRenderer.legend(document, gradient, 0, MAX_P_VALUE, AnalysisType.OVERREPRESENTATION);

		// Calculate document dimensions
		// Must be done to get children dimensions
		// We use a clone because this method modifies the document
		cloned = (Document) document.cloneNode(true);
		builder.build(context, cloned);

		pathways.forEach(s -> {
			final EntityStatistics stats = entityStats.getOrDefault(s, null);
			overlayEnrichment(s, stats);
			analysisInfo(s, stats);
		});
	}

	private void expression() {
		SvgUtil.addInlineStyle(document, OVERLAY_TEXT_CLASS, OVERLAY_TEXT_STYLE);
		SvgUtil.addInlineStyle(document, ANALYSIS_INFO_CLASS, ANALYSIS_INFO_STYLE);

		final GradientSheet gradient = args.getProfiles().getAnalysisSheet().getExpression().getGradient();
		SvgLegendRenderer.legend(document, gradient, summary.getExpression().getMax(), summary.getExpression().getMin(), AnalysisType.EXPRESSION);
		addBottomTextGroup();

		// Analysis info text is centered to ANALINFO group. To get the
		// center of each ANALINFO group we must build the whole document.
		cloned = (Document) document.cloneNode(true);
		builder.build(context, cloned);

		pathways.forEach(stId -> {
			final EntityStatistics stats = entityStats.getOrDefault(stId, null);
			overlayExpression(stId, stats, summary.getExpression());
			analysisInfo(stId, stats);
		});
	}

	private void gsa() {
		SvgUtil.addInlineStyle(document, OVERLAY_TEXT_CLASS, OVERLAY_TEXT_STYLE);
		SvgUtil.addInlineStyle(document, ANALYSIS_INFO_CLASS, ANALYSIS_INFO_STYLE);

		final GradientSheet gradient = args.getProfiles().getAnalysisSheet().getExpression().getGradient();
		SvgLegendRenderer.legend(document, gradient, summary.getExpression().getMax(), summary.getExpression().getMin(), AnalysisType.GSA_REGULATION);
		//SvgLegendRenderer.regulationLegend(document, gradient, AnalysisType.GSA_REGULATION);

		addBottomTextGroup();

		// Analysis info text is centered to ANALINFO group. To get the
		// center of each ANALINFO group we must build the whole document.
		cloned = (Document) document.cloneNode(true);
		builder.build(context, cloned);

		pathways.forEach(stId -> {
			final EntityStatistics stats = entityStats.getOrDefault(stId, null);
			overlayGSA(stId, stats, summary.getExpression());
			analysisInfo(stId, stats);
		});
	}

	private List<String> getRegions() {
		final NodeList groups = document.getRootElement().getElementsByTagNameNS(SVG_NAMESPACE_URI, SVG_G_TAG);
		return IntStream.range(0, groups.getLength())
				.mapToObj(groups::item)
				.filter(SVGElement.class::isInstance)
				.map(SVGElement.class::cast)
				.map(SVGElement::getId)
				.filter(id -> id.startsWith(REGION_))
				.collect(Collectors.toList());
	}

	private void overlayEnrichment(String stId, EntityStatistics stats) {
		double percentage;
		double pValue;
		if (stats == null) {
			percentage = 0.0;
			pValue = 0.0;
		} else {
			int found = stats.getFound();
			int total = stats.getTotal();
			percentage = (double) found / total;
			pValue = stats.getpValue();
		}
		final GradientSheet gradient = args.getProfiles().getAnalysisSheet().getEnrichment().getGradient();
		Color analysisColor = DEFAULT_OVERLAY_COLOR;
		if (pValue >= 0 && pValue <= MAX_P_VALUE) {
			double val = pValue / MAX_P_VALUE;
			analysisColor = ColorFactory.interpolate(gradient, val);
			if (args.getSelected() != null && args.getSelected().contains(stId)) {
				final Color selection = args.getProfiles().getDiagramSheet().getProperties().getSelection();
				SvgLegendRenderer.tick(document, val, selection);
			}

		}
		createClip(stId, percentage);
		createOverlayNodes(analysisColor, stId);
	}

	private void overlayExpression(String stId, EntityStatistics stats, ExpressionSummary expression) {
		double percentage;
		double value;
		double pValue = Double.MAX_VALUE;
		if (stats == null) {
			percentage = 0.0;
			value = 0.0;
		} else {
			int found = stats.getFound();
			int total = stats.getTotal();
			percentage = (double) found / total;
			final int column = args.getColumn() == null ? 0 : args.getColumn();
			value = stats.getExp().get(column);
			pValue = stats.getpValue();
		}
		createClip(stId, percentage);

		final GradientSheet gradient = args.getProfiles().getAnalysisSheet().getExpression().getGradient();
		Color expressionColor = DEFAULT_OVERLAY_COLOR;
		if (pValue <= MAX_P_VALUE && value >= expression.getMin() && value <= expression.getMax()) {
			double val = 1 - (value - expression.getMin()) / (expression.getMax() - expression.getMin());
			expressionColor = ColorFactory.interpolate(gradient, val);
			if (args.getSelected() != null && args.getSelected().contains(stId)) {
				final Color selection = args.getProfiles().getDiagramSheet().getProperties().getSelection();
				SvgLegendRenderer.tick(document, val, selection);
			}
		}

		createOverlayNodes(expressionColor, stId);

	}

	private void overlayGSA(String stId, EntityStatistics stats, ExpressionSummary expression) {
		double percentage;
		double value;
		double pValue = Double.MAX_VALUE;
		if (stats == null) {
			percentage = 0.0;
			value = 0.0;
		} else {
			int found = stats.getFound();
			int total = stats.getTotal();
			percentage = (double) found / total;
			final int column = args.getColumn() == null ? 0 : args.getColumn();
			value = stats.getExp().get(column);
			pValue = stats.getpValue();
		}
		createClip(stId, percentage);

		final GradientSheet gradient = args.getProfiles().getAnalysisSheet().getExpression().getGradient();
		Color expressionColor = DEFAULT_OVERLAY_COLOR;
		if (pValue <= MAX_P_VALUE && value >= expression.getMin() && value <= expression.getMax()) {
			// value - val
			// 2  - 0.1
			// 1  - 0.3
			// 0  - 0.5
			// -1 - 0.7
			// -2 - 0.9
 			//there are 5 rectangles in the regulation legend, place the tick at the middle of each rectangle by the value of val(0.1, 0.3, 0.5, 0.7, 0.9)
			double val =  ((value - expression.getMax()) * (0.90 - 0.10) / (expression.getMin() - expression.getMax())) + 0.10;
			expressionColor = ColorFactory.interpolate(gradient, val);
			if (args.getSelected() != null && args.getSelected().contains(stId)) {
				final Color selection = args.getProfiles().getDiagramSheet().getProperties().getSelection();
				SvgLegendRenderer.tick(document, val, selection);
			}
		}

		createOverlayNodes(expressionColor, stId);

	}

	private void  createClip(String stId, double percentage) {
		if (percentage > 0 && percentage < MIN_OVERLAY_CLIP)
			percentage = MIN_OVERLAY_CLIP;
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
		SvgUtil.appendToDefs(document, clip);
	}

	private void createOverlayNodes(Color color, String stId) {
		// OVERLAY
		//  - OVERLAYBASE (white)
		//  - OVERLAYCLONE (enrichment)
		//  - texts
		final Element overlay = document.getElementById(OVERLAY_ + stId);
		if (overlay == null) {
			LoggerFactory.getLogger("diagram-exporter").warn(stId + " has no overlay");
			return;
		}

		final Element base = (Element) overlay.cloneNode(true);
		// remove text elements
		final NodeList texts = base.getElementsByTagName(SVG_TEXT_TAG);
		List<Node> textNodes = IntStream.range(0, texts.getLength())
				.mapToObj(texts::item).collect(Collectors.toList());
		textNodes.forEach(textNode -> textNode.getParentNode().removeChild(textNode));
		base.setAttribute(SVG_ID_ATTRIBUTE, OVERLAY_BASE_ + stId);
		base.setAttribute(SVG_FILL_ATTRIBUTE, HIT_BASE_COLOR);
		base.setAttribute(SVG_STROKE_ATTRIBUTE, HIT_BASIS_STROKE_COLOUR);
		base.setAttribute(SVG_STROKE_WIDTH_ATTRIBUTE, HIT_BASIS_STROKE_WIDTH);

		final Element clone = (Element) overlay.cloneNode(true);
		clone.setAttribute(SVG_ID_ATTRIBUTE, OVERLAY_CLONE_ + stId);
		clone.setAttribute(SVG_CLIP_PATH_ATTRIBUTE, SvgUtil.toURL(CLIPPING_PATH + stId));
		clone.removeAttribute(SVG_FILTER_ATTRIBUTE);
		clone.removeAttribute(SVG_TRANSFORM_ATTRIBUTE);

		// Batik does not support rgba format colors, only HEX + opacity
		clone.setAttribute(SVG_FILL_ATTRIBUTE, ColorFactory.hex(color));
		clone.setAttribute(SVG_OPACITY_ATTRIBUTE, String.valueOf(OVERLAY_OPACITY));

		final Element group = document.createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
		group.appendChild(base);
		group.appendChild(clone);
		// Add texts to group
		final NodeList textss = clone.getElementsByTagName(SVG_TEXT_TAG);
		textNodes = IntStream.range(0, textss.getLength())
				.mapToObj(textss::item).collect(Collectors.toList());
		textNodes.stream().map(Element.class::cast)
				.forEach(node -> {
					SvgUtil.addClass(node, OVERLAY_TEXT_CLASS);
					group.appendChild(node);
				});
		overlay.appendChild(group);
		removeAttributeFromChildren(base, SVG_CLASS_ATTRIBUTE);
		removeAttributeFromChildren(clone, SVG_CLASS_ATTRIBUTE);
	}

	private void removeAttributeFromChildren(Element element, String attribute) {
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

	private void analysisInfo(String stId, EntityStatistics stats) {
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

	private void makeVisible(Element analysisInfo) {
		SvgUtil.addClass(analysisInfo, ANALYSIS_INFO_CLASS);
	}

	private void setAnalysisInfoText(Element element, EntityStatistics entities) {
		final NodeList texts = element.getElementsByTagName(SVG_TEXT_TAG);
		if (texts.getLength() == 0) return;
		final Element text = (Element) texts.item(0);

		final Integer found = entities.getFound();
		final Integer total = entities.getTotal();
		String msg = String.format("Hit: %d/%d - FDR: %s", found, total,
				NUMBER_FORMAT.format(entities.getFdr()));
		text.setTextContent(msg);

		// Center text
		final Element clonedElement = cloned.getElementById(element.getAttribute(SVG_ID_ATTRIBUTE));
		final GraphicsNode box = builder.build(context, clonedElement);

		double centerX = box.getSensitiveBounds().getCenterX();
		// Vertical centering must be done manually, since BATIK does not
		// support aligment-baseline either dominant-baseline
		double centerY = box.getSensitiveBounds().getCenterY() + TEXT_V_ALIGN;

		text.removeAttribute(SVG_TRANSFORM_ATTRIBUTE);
		text.setAttribute(SVG_TEXT_ANCHOR_ATTRIBUTE, SVG_MIDDLE_VALUE);
		text.setAttribute(SVG_X_ATTRIBUTE, String.valueOf(centerX));
		text.setAttribute(SVG_Y_ATTRIBUTE, String.valueOf(centerY));
	}

	private Stream<Node> streamChildren(Element element) {
		return IntStream.range(0, element.getChildNodes().getLength())
				.mapToObj(element.getChildNodes()::item);
	}

	/**
	 * Change the visible expression column. This will modify the color of the
	 * OVERLAY areas and the ticks in the legend.
	 */
	void setColumn(int expressionColumn) {
		SvgLegendRenderer.clearTicks(document);
		final ExpressionSummary expression = summary.getExpression();
		final GradientSheet gradient = args.getProfiles().getAnalysisSheet().getExpression().getGradient();
		entityStats.forEach((id, stats) -> {
			final double value = stats.getExp().get(expressionColumn);
			final Element overlay = document.getElementById(OVERLAY_CLONE_ + id);
			if (stats.getpValue() <= MAX_P_VALUE && value >= expression.getMin() && value <= expression.getMax()) {
				double val = 1 - (value - expression.getMin()) / (expression.getMax() - expression.getMin());
				final Color color = ColorFactory.interpolate(gradient, val);
				overlay.setAttribute(SVG_FILL_ATTRIBUTE, ColorFactory.hex(color));
				if (args.getSelected() != null && args.getSelected().contains(id)) {
					final Color selection = args.getProfiles().getDiagramSheet().getProperties().getSelection();
					SvgLegendRenderer.tick(document, val, selection);
				}
			}
		});
		updateInfoText(expressionColumn);
	}

	private void updateInfoText(int expressionColumn) {
		final String prefix = String.format("[%s] ", result.getSummary().getSampleName());
		final String info = String.format(Locale.UK, "%d/%d: %s", (expressionColumn + 1),
				summary.getExpression().getColumnNames().size(),
				summary.getExpression().getColumnNames().get(expressionColumn));
		final Element text = document.getElementById(BOTTOM_TEXT);
		text.setTextContent(prefix + info);
	}

	private void addBottomTextGroup() {
		final Rectangle2D viewBox = getViewBox();
		double centerX = viewBox.getCenterX();
		double centerY = viewBox.getHeight() + 15;

		// Use only Arial Bold 12pt
		final Element bottomText = document.createElementNS(SVG_NAMESPACE_URI, SVG_TEXT_TAG);
		bottomText.setAttribute(SVG_ID_ATTRIBUTE, BOTTOM_TEXT);
		bottomText.setAttribute(SVG_FONT_FAMILY_ATTRIBUTE, "Arial");
		bottomText.setAttribute(SVG_FONT_SIZE_ATTRIBUTE, "12px");
		bottomText.setAttribute(SVG_FONT_WEIGHT_ATTRIBUTE, "bold");
		bottomText.setAttribute(SVG_TEXT_ANCHOR_ATTRIBUTE, SVG_MIDDLE_VALUE);
		bottomText.setAttribute(SVG_X_ATTRIBUTE, String.valueOf(centerX));
		bottomText.setAttribute(SVG_Y_ATTRIBUTE, String.valueOf(centerY));

		final Element group = document.createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
		group.setAttribute(SVG_ID_ATTRIBUTE, BOTTOM_TEXT + "-group");
		document.getRootElement().appendChild(group);
		group.appendChild(bottomText);

		// Update viewBox
		final String newVB = String.format(Locale.UK, "0 0 %.3f %.3f", viewBox.getWidth(), viewBox.getHeight() + 30);
		document.getRootElement().setAttribute(SVG_VIEW_BOX_ATTRIBUTE, newVB);
	}

	private Rectangle2D getViewBox() {
		final String viewBox = document.getRootElement().getAttribute(SVG_VIEW_BOX_ATTRIBUTE);
		final String[] split = viewBox.split(" ");
		return new Rectangle2D.Double(
				Double.valueOf(split[0]),
				Double.valueOf(split[1]),
				Double.valueOf(split[2]),
				Double.valueOf(split[3]));
	}

	AnalysisType getAnalysisType() {
		return analysisType;
	}

	public ExpressionSummary getExpressionSummary() {
		return summary.getExpression();
	}
}
