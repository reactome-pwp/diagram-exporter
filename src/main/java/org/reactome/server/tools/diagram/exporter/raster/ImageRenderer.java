package org.reactome.server.tools.diagram.exporter.raster;

import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Edge;
import org.reactome.server.tools.diagram.data.profile.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ColorProfile;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.ConnectorRenderer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.EdgeRenderer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.Renderer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.RendererFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.reactome.server.tools.diagram.exporter.raster.renderers.common.ColorProfile.*;

/**
 * Renders Reactome pathway diagrams into <code>RendererImage</code>s.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ImageRenderer {

	/*
	 * # of pixels  |   600ppi     | 1200 ppi
	 * -------------|--------------|-------------
	 *  10MP        |  3m2,  31ft2 | 1.5m2, 15ft2
	 *  50MP        | 14m2, 155ft2 |  7m2,  77ft2
	 * 100MP        | 29m2, 310ft2 | 14m2, 155ft2
	 * 200MP        | 58m2, 621ft2 | 29m2, 310ft2
	 */
	/**
	 * Max amount of pixels per image
	 * Using a hq printing quality (600ppi) you can have:
	 */
	private static final double MAX_IMAGE_SIZE = 0.1 * 1024 * 1024 * 1024; // 100 Mpixels
	// OPTIONS
	private static final int MARGIN = 15;
	private final Diagram diagram;
	private final Graph graph;
	private final DiagramProfile profile;
	private final Decorator decorator;
	private AdvancedGraphics2D graphics;
	private DiagramIndex index;
	private ConnectorRenderer connectorRenderer = new ConnectorRenderer();

	/**
	 * Creates a ImageRenderer with specific diagram, graph, profile and
	 * decorators.
	 *
	 * @param diagram   diagram to render
	 * @param graph     underlying graph
	 * @param profile   colouring profile
	 * @param decorator elements to decorate
	 *
	 * @return a Canvas with the diagram render
	 */
	public ImageRenderer(Diagram diagram, Graph graph, DiagramProfile profile, Decorator decorator) {
		this.diagram = diagram;
		this.graph = graph;
		this.profile = profile;
		this.decorator = decorator;
		this.index = new DiagramIndex(diagram, graph, decorator, AnalysisType.NONE);
	}

	/**
	 * Renders an Image with given dimensions
	 *
	 * @param factor scale of the image
	 *
	 * @return a RenderedImage with the given dimensions
	 */
	public BufferedImage render(double factor) {

		// Bounds are recalculated reading nodes, we don't trust diagram bounds
		final double minX = getMinX();
		final double maxX = getMaxX();
		final double minY = getMinY();
		final double maxY = getMaxY();

		int width = Double.valueOf(factor * (maxX - minX) + 2 * MARGIN).intValue();
		int height = Double.valueOf(factor * (maxY - minY) + 2 * MARGIN).intValue();

		// Avoid too large images
		double newFactor = factor;
		while (height * width > MAX_IMAGE_SIZE && newFactor > 1) {
			newFactor -= 0.1;
			width = Double.valueOf(newFactor * (maxX - minX) + 2 * MARGIN).intValue();
			height = Double.valueOf(newFactor * (maxY - minY) + 2 * MARGIN).intValue();
		}
		if (newFactor < factor) {
			Logger.getLogger(getClass().getName())
					.warning(String.format(
							"Image too large. Quality reduced from %.1f to %.1f -> %d x %d = %d (%.2f MP)",
							factor, newFactor, height, width, height * width, height * width / 1e6));
			factor = newFactor;
		}
		RendererProperties.setFactor(factor);
		ColorProfile.setFactor(factor);

		final double x = minX * factor;
		final double y = minY * factor;
		graphics = new AdvancedGraphics2D(width, height, factor, x, y, MARGIN);

		if (decorator == null) {
			compartments();
			drawReactions();
			drawNodes();
			notes();
			shadows();
		} else {
			compartments();
			shadows();
			haloReactions();
			haloNodes();
			drawReactions();
			selectReactions();
			drawNodes();
			selectNodes();
			notes();
			flags();
		}
		return graphics.getImage();
	}

	private double getMinY() {
		return streamObjects()
				.mapToDouble(DiagramObject::getMinY)
				.min().orElse(0);
	}

	private double getMinX() {
		return streamObjects()
				.mapToDouble(DiagramObject::getMinX)
				.min().orElse(0);
	}

	private double getMaxX() {
		return streamObjects()
				.mapToDouble(DiagramObject::getMaxX)
				.max().orElse(0);
	}

	private double getMaxY() {
		return streamObjects()
				.mapToDouble(DiagramObject::getMaxY)
				.max().orElse(0);
	}

	private Stream<DiagramObject> streamObjects() {
		return Stream.of(
				diagram.getLinks(),
				diagram.getCompartments(),
				diagram.getNodes(),
				diagram.getShadows())
				.flatMap(Collection::stream);
	}

	private void compartments() {
		final String renderingClass = "Compartment";
		final Renderer renderer = RendererFactory.get(renderingClass);
		final Paint fillColor = getFillColor(profile, renderingClass, RenderType.NORMAL);
		final Paint lineColor = getLineColor(profile, renderingClass, RenderType.NORMAL);
		final Paint textColor = getTextColor(profile, renderingClass, RenderType.NORMAL);
		renderer.draw(graphics, diagram.getCompartments(), fillColor, lineColor, textColor, DEFAULT_BORDER_STROKE, DEFAULT_BORDER_STROKE);
	}

	private void selectReactions() {
		final Paint selection = getProfileColor(profile, "selection");
		final Map<String, List<Edge>> reactions = index.getSelectedReactions().stream()
				.collect(Collectors.groupingBy(DiagramObject::getRenderableClass));
		reactions.forEach((rClass, edges) -> {
			final EdgeRenderer renderer = (EdgeRenderer) RendererFactory.get(rClass);
			final Paint fill = ColorProfile.getFillColor(profile, rClass, RenderType.NORMAL);
			renderer.draw(graphics, edges, fill, selection, null, SELECTION_STROKE, DEFAULT_BORDER_STROKE);
		});
		final Paint fill = ColorProfile.getFillColor(profile, "Reaction", RenderType.NORMAL);
		connectorRenderer.drawConnectors(graphics, index.getSelectedConnectors(), fill, selection, null, SELECTION_STROKE, DEFAULT_BORDER_STROKE);
	}

	private void drawNodes() {
		graphics.getGraphics().setStroke(DEFAULT_BORDER_STROKE);
		index.getClassifiedNodes().forEach((renderingClass, subitems) -> {
			final Renderer renderer = RendererFactory.get(renderingClass);
			subitems.forEach((renderType, objects) -> {
				final Paint lineColor = getLineColor(profile, renderingClass, renderType);
				final Paint fillColor = getFillColor(profile, renderingClass, renderType);
				final Paint textColor = getTextColor(profile, renderingClass, renderType);
				renderer.draw(graphics, objects, fillColor, lineColor, textColor, DEFAULT_BORDER_STROKE, DEFAULT_BORDER_STROKE);
			});
		});
	}

	private void selectNodes() {
		final Paint color = getProfileColor(profile, "selection");
		index.getSelectedNodes().forEach((rClass, nodes) -> {
			final Renderer renderer = RendererFactory.get(rClass);
			renderer.draw(graphics, nodes, null, color, null, SELECTION_STROKE, SELECTION_STROKE);
		});
	}

	private void drawReactions() {
		index.getClassifiedConnectors().forEach((rClass, items) ->
				items.forEach((renderType, connectors) -> {
					final Paint fill = ColorProfile.getFillColor(profile, rClass, renderType);
					final Paint border = ColorProfile.getLineColor(profile, rClass, renderType);
					connectorRenderer.drawConnectors(graphics, connectors, fill, border, border, DEFAULT_LINE_STROKE, DEFAULT_LINE_STROKE);
				}));
		index.getClassifiedReactions().forEach((renderableClass, reactions) ->
				reactions.forEach((renderType, edges) -> {
					final Renderer renderer = RendererFactory.get(renderableClass);
					final Paint fill = ColorProfile.getFillColor(profile, renderableClass, renderType);
					final Paint border = ColorProfile.getLineColor(profile, renderableClass, renderType);
					renderer.draw(graphics, edges, fill, border, border, DEFAULT_LINE_STROKE, DEFAULT_LINE_STROKE);
				}));
	}

	public void setFixedMargin(boolean fixedMargin) {
	}

	private void haloNodes() {
		final Paint color = getProfileColor(profile, "halo");
		index.getHaloNodes().forEach((rClass, nodes) -> {
			final Renderer renderer = RendererFactory.get(rClass);
			renderer.draw(graphics, nodes, null, color, null, HALO_STROKE, HALO_STROKE);
		});
	}

	private void haloReactions() {
		final Paint color = getProfileColor(profile, "halo");
		index.getHaloConnectors().forEach((rClass, items) ->
				connectorRenderer.drawConnectors(graphics, items, null, color, null, HALO_STROKE, HALO_STROKE));
		index.getHaloReactions().forEach((rClass, reactions) -> {
			final EdgeRenderer renderer = (EdgeRenderer) RendererFactory.get(rClass);
			renderer.draw(graphics, reactions, null, color, null, HALO_STROKE, HALO_STROKE);
		});
	}

	private void flags() {
		final Paint flag = ColorProfile.getProfileColor(profile, "flag");
		index.getFlagNodes().forEach((rClass, nodes) ->
				RendererFactory.get(rClass).draw(graphics, nodes, null, flag, null, SELECTION_STROKE, SELECTION_STROKE));
	}

	private void notes() {
		final String renderingClass = "Note";
		final Renderer renderer = RendererFactory.get(renderingClass);
		final Paint fillColor = getFillColor(profile, renderingClass, RenderType.NORMAL);
		final Paint lineColor = getLineColor(profile, renderingClass, RenderType.NORMAL);
		final Paint textColor = getTextColor(profile, renderingClass, RenderType.NORMAL);
		renderer.draw(graphics, diagram.getNotes(), fillColor, lineColor, textColor, DEFAULT_LINE_STROKE, DEFAULT_BORDER_STROKE);
	}

	private void shadows() {
		graphics.getGraphics().setFont(ColorProfile.SHADOWS_FONT);
		diagram.getShadows().forEach(shadow -> {
			final Paint shadowFill = ColorProfile.getShadowFill(shadow);
			final Paint shadowLine = ColorProfile.getShadowLine(shadow);
			RendererFactory.get(shadow.getRenderableClass()).draw(graphics, Collections.singletonList(shadow), shadowFill, shadowLine, shadowLine, DEFAULT_LINE_STROKE, DEFAULT_BORDER_STROKE);
		});
	}
}
