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
 * Renders Reactome pathway diagrams into <code>BufferedImage</code>s.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class RasterRenderer {

	/*
	 * # of pixels  |   600ppi     | 1200 ppi
	 * -------------|--------------|-------------
	 *  10MP        |  3m2,  31ft2 | 1.5m2, 15ft2
	 *  50MP        | 14m2, 155ft2 |  7m2,  77ft2
	 * 100MP        | 29m2, 310ft2 | 14m2, 155ft2
	 * 200MP        | 58m2, 621ft2 | 29m2, 310ft2
	 */
	/**
	 * Max amount of allowed pixels per image
	 */
	private static final double MAX_IMAGE_SIZE = 1e8; // 100Mpixels
	// OPTIONS
	private static final int MARGIN = 15;
	private final Diagram diagram;
	private final DiagramProfile profile;
	private final Decorator decorator;
	private AdvancedGraphics2D graphics;
	private DiagramIndex index;
	private ConnectorRenderer connectorRenderer = new ConnectorRenderer();

	/**
	 * Creates a RasterRenderer with specific diagram, graph, profile and
	 * decorator.
	 *
	 * @param diagram   diagram to render
	 * @param graph     underlying graph
	 * @param profile   colouring profile
	 * @param decorator elements to decorate
	 */
	public RasterRenderer(Diagram diagram, Graph graph, DiagramProfile profile, Decorator decorator) {
		this.diagram = diagram;
		this.profile = profile;
		this.decorator = decorator;
		this.index = new DiagramIndex(diagram, graph, decorator, AnalysisType.NONE);
	}

	/**
	 * Renders an Image with given dimensions
	 *
	 * @param factor scale of the image
	 * @param format file format. To select the proper in-memory structure
	 *
	 * @return a RenderedImage with the given dimensions
	 */
	public BufferedImage render(double factor, String format) {

		// Bounds are recalculated reading nodes, we don't trust diagram bounds
		final double minX = getMinX();
		final double maxX = getMaxX();
		final double minY = getMinY();
		final double maxY = getMaxY();

		final double diagramWidth = maxX - minX;
		final double diagramHeight = maxY - minY;

		int width = (int) (factor * (diagramWidth + 2 * MARGIN));
		int height = (int) (factor * (diagramHeight + 2 * MARGIN));

		// As HD images occupy lots of memory, we limit the size of the images
		// by reducing the factor until they fit into the MAX_IMAGE_SIZE, thus
		// ensuring that a maximum of MAX_IMAGE_SIZE integers are stored in
		// memory.
		// For instance, an image of 100M pixels needs 100M integers, that is
		// 400MB of memory
		double newFactor = factor;
		while (height * width > MAX_IMAGE_SIZE && newFactor > 1) {
			newFactor -= 0.1;
			width = (int) (newFactor * (diagramWidth + 2 * MARGIN));
			height = (int) (newFactor * (diagramHeight + 2 * MARGIN));
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
		final double margin = factor * MARGIN;
		graphics = new AdvancedGraphics2D(width, height, factor, x, y, margin, format);

		if (decorator == null) {
			compartments();
			links();
			drawReactions();
			drawNodes();
			notes();
			shadows();
		} else {
			compartments();
			links();
			haloReactions();
			haloNodes();
			drawReactions();
			selectReactions();
			drawNodes();
			selectNodes();
			shadows();
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
		renderer.draw(graphics, diagram.getCompartments(), fillColor, lineColor, textColor, DEFAULT_BORDER_STROKE);
	}

	private void haloNodes() {
		final Paint color = getProfileColor(profile, "halo");
		index.getHaloNodes().forEach((rClass, nodes) -> {
			final Renderer renderer = RendererFactory.get(rClass);
			// Just the border
			renderer.draw(graphics, nodes, null, color, null, HALO_STROKE);
		});
	}

	private void haloReactions() {
		final Paint color = getProfileColor(profile, "halo");
		index.getHaloConnectors().forEach((rClass, items) -> {
			connectorRenderer.connectorSegments(graphics, color, HALO_STROKE, items);
			connectorRenderer.drawConnectors(graphics, items, null, color, HALO_STROKE);
		});
		index.getHaloReactions().forEach((rClass, reactions) -> {
			final EdgeRenderer renderer = (EdgeRenderer) RendererFactory.get(rClass);
			renderer.segments(graphics, color, HALO_STROKE, reactions);
			renderer.draw(graphics, reactions, null, color, null, HALO_STROKE);
		});
	}

	private void links() {
		index.getLinks().forEach((rClass, items) ->
				items.forEach((renderType, links) -> {
					final EdgeRenderer renderer = (EdgeRenderer) RendererFactory.get(rClass);
					final Paint lineColor = ColorProfile.getLineColor(profile, rClass, renderType);
					final Paint textColor = ColorProfile.getTextColor(profile, rClass, renderType);
					renderer.segments(graphics, lineColor, DEFAULT_LINE_STROKE, links);
					renderer.draw(graphics, links, null, lineColor, textColor, ColorProfile.DEFAULT_LINE_STROKE);
				}));
	}

	private void drawReactions() {
		// 1 segments
		index.getClassifiedConnectors().forEach((renderType, connectors) -> {
			final Paint lineColor = ColorProfile.getLineColor(profile, "Reaction", renderType);
			connectorRenderer.connectorSegments(graphics, lineColor, DEFAULT_LINE_STROKE, connectors);
		});
		index.getClassifiedReactions().forEach((rClass, items) ->
				items.forEach((renderType, edges) -> {
					final EdgeRenderer renderer = (EdgeRenderer) RendererFactory.get(rClass);
					final Paint border = ColorProfile.getLineColor(profile, rClass, renderType);
					renderer.segments(graphics, border, DEFAULT_LINE_STROKE, edges);
				}));
		// 2 Shapes
		index.getClassifiedConnectors().forEach((renderType, connectors) -> {
			final Paint fill = ColorProfile.getFillColor(profile, "Reaction", renderType);
			final Paint border = ColorProfile.getLineColor(profile, "Reaction", renderType);
			connectorRenderer.drawConnectors(graphics, connectors, fill, border, DEFAULT_LINE_STROKE);
		});
		index.getClassifiedReactions().forEach((renderableClass, reactions) ->
				reactions.forEach((renderType, edges) -> {
					final Renderer renderer = RendererFactory.get(renderableClass);
					final Paint fill = ColorProfile.getFillColor(profile, renderableClass, renderType);
					final Paint border = ColorProfile.getLineColor(profile, renderableClass, renderType);
					renderer.draw(graphics, edges, fill, border, border, DEFAULT_LINE_STROKE);
				}));
	}

	private void selectReactions() {
		final Paint selection = getProfileColor(profile, "selection");
		final Map<String, List<Edge>> reactions = index.getSelectedReactions().stream()
				.collect(Collectors.groupingBy(DiagramObject::getRenderableClass));
		reactions.forEach((rClass, edges) -> {
			final EdgeRenderer renderer = (EdgeRenderer) RendererFactory.get(rClass);
			final Paint fill = ColorProfile.getFillColor(profile, rClass, RenderType.NORMAL);
			final Paint text = ColorProfile.getTextColor(profile, rClass, RenderType.NORMAL);
			renderer.segments(graphics, selection, SELECTION_STROKE, edges);
			// Whe selecting segments, they can overlay the fill we made before
			// so we must repeat the fill, border and text for the selected
			// reactions
			renderer.draw(graphics, edges, fill, selection, text, SELECTION_STROKE);
		});
		final Paint fill = ColorProfile.getFillColor(profile, "Reaction", RenderType.NORMAL);
		final Paint text = ColorProfile.getTextColor(profile, "Reaction", RenderType.NORMAL);
		connectorRenderer.connectorSegments(graphics, selection, SELECTION_STROKE, index.getSelectedConnectors());
		connectorRenderer.drawConnectors(graphics, index.getSelectedConnectors(), fill, selection, SELECTION_STROKE);
	}

	private void drawNodes() {
		index.getClassifiedNodes().forEach((renderingClass, items) -> {
			final Renderer renderer = RendererFactory.get(renderingClass);
			items.forEach((renderType, objects) -> {
				final Paint lineColor = getLineColor(profile, renderingClass, renderType);
				final Paint fillColor = getFillColor(profile, renderingClass, renderType);
				final Paint textColor = getTextColor(profile, renderingClass, renderType);
				renderer.draw(graphics, objects, fillColor, lineColor, textColor, DEFAULT_BORDER_STROKE);
			});
		});
	}

	private void selectNodes() {
		final Paint color = getProfileColor(profile, "selection");
		index.getSelectedNodes().forEach((rClass, nodes) -> {
			final Renderer renderer = RendererFactory.get(rClass);
			// Just the border
			renderer.draw(graphics, nodes, null, color, null, SELECTION_STROKE);
		});
	}

	private void flags() {
		final Paint flag = ColorProfile.getProfileColor(profile, "flag");
		index.getFlagNodes().forEach((rClass, nodes) ->
				RendererFactory.get(rClass).draw(graphics, nodes, null, flag, null, SELECTION_STROKE));
	}

	private void notes() {
		final String renderingClass = "Note";
		final Renderer renderer = RendererFactory.get(renderingClass);
		final Paint fillColor = getFillColor(profile, renderingClass, RenderType.NORMAL);
		final Paint lineColor = getLineColor(profile, renderingClass, RenderType.NORMAL);
		final Paint textColor = getTextColor(profile, renderingClass, RenderType.NORMAL);
		renderer.draw(graphics, diagram.getNotes(), fillColor, lineColor, textColor, DEFAULT_BORDER_STROKE);
	}

	private void shadows() {
		graphics.getGraphics().setFont(ColorProfile.SHADOWS_FONT);
		// Each shadow has a different color and different renderable Class
		diagram.getShadows().forEach(shadow -> {
			final Renderer renderer = RendererFactory.get(shadow.getRenderableClass());
			final Paint shadowFill = ColorProfile.getShadowFill(shadow);
			final Paint shadowLine = ColorProfile.getShadowLine(shadow);
			renderer.draw(graphics, Collections.singletonList(shadow), shadowFill, shadowLine, shadowLine, DEFAULT_BORDER_STROKE);
		});
	}
}
