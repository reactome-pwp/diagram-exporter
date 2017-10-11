package org.reactome.server.tools.diagram.exporter.raster;

import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.profile.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.*;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.ConnectorRenderer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.EdgeRenderer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.Renderer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.RendererFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.reactome.server.tools.diagram.exporter.raster.renderers.common.ColorProfile.*;
import static org.reactome.server.tools.diagram.exporter.raster.renderers.common.StrokeProperties.*;

/**
 * Renders Reactome pathway diagrams into <code>BufferedImage</code>s.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class RasterRenderer {

	/*
	 * physical size of image using the best print quality (600 ppi).
	 * magazines are printed at 300 ppi
	 *
	 * MAX_IMG_SIZE | memory |  eur         | uk
	 * -------------|--------|--------------|-------------
	 *  1e7         |  40MB  | 29cm x 29cm  | 11” x 11” (10Mp, as smart phones)
	 *  2.5e7       | 100MB  | 36cm x 36cm  | 14” x 14”
	 *  5e7         | 200MB  | 43cm x 43cm  | 17” x 17”
	 *  1e8         | 400MB  | 51cm x 51cm  | 20” x 20”
	 *  2e8         | 800MB  | 61cm x 61cm  | 24” x 24”
	 */
	/**
	 * Max amount of allowed pixels per image
	 */
	private static final double MAX_IMAGE_SIZE = 1e8; // 100Mpixels
	private static final int MARGIN = 15;

	private final Diagram diagram;
	private final DiagramProfile profile;
	private final Decorator decorator;

	private AdvancedGraphics2D graphics;
	private DiagramIndex index;
	// Connectors do not have renderable class
	private ConnectorRenderer connectorRenderer = new ConnectorRenderer();
	private static final List<String> LINKS = Arrays.asList("Interaction", "EntitySetAndEntitySetLink", "EntitySetAndMemberLink");

	/**
	 * Creates a RasterRenderer with specific diagram, graph, profile and
	 * decorator.
	 *  @param diagram   diagram to render
	 * @param graph     underlying graph
	 * @param decorator elements to decorate
	 * @param profile   colouring profile
	 */
	RasterRenderer(Diagram diagram, Graph graph, Decorator decorator, DiagramProfile profile) {
		this.diagram = diagram;
		this.profile = profile;
		this.decorator = decorator;
		this.index = new DiagramIndex(diagram, graph, decorator, AnalysisType.NONE);
	}

	/**
	 * Renders an Image with given dimensions
	 *
	 * @param factor scale of the image
	 * @param ext    file format. To select the proper in-memory structure
	 *
	 * @return a RenderedImage with the given dimensions
	 */
	public BufferedImage render(double factor, String ext) {

		// Bounds are recalculated reading nodes, we don't trust diagram bounds
		final double minX = getMinX();
		final double maxX = getMaxX();
		final double minY = getMinY();
		final double maxY = getMaxY();

		final double diagramWidth = maxX - minX;
		final double diagramHeight = maxY - minY;

		double width = factor * (diagramWidth + 2 * MARGIN);
		double height = factor * (diagramHeight + 2 * MARGIN);

		// Limit the size of the images by reducing the factor until it fits
		// into the MAX_IMAGE_SIZE, thus ensuring that a maximum of
		// MAX_IMAGE_SIZE pixels are stored in memory.
		double newFactor = factor;
		while (height * width > MAX_IMAGE_SIZE && newFactor > 1) {
			newFactor -= 0.1;
			width = newFactor * (diagramWidth + 2 * MARGIN);
			height = newFactor * (diagramHeight + 2 * MARGIN);
		}
		if (newFactor < factor) {
			Logger.getLogger(getClass().getName())
					.warning(String.format(
							"Image too large. Quality reduced from %.1f to %.1f -> %.0f x %.0f = %.0f (%.2f MP)",
							factor, newFactor, height, width, height * width, height * width / 1e6));
			factor = newFactor;
		}

		FontProperties.setFactor(factor);
		RendererProperties.setFactor(factor);
		StrokeProperties.setFactor(factor);

		final double x = minX * factor;
		final double y = minY * factor;
		final double margin = factor * MARGIN;
		graphics = new AdvancedGraphics2D(width, height, factor, x, y, margin, ext);
		graphics.getGraphics().setFont(FontProperties.DEFAULT_FONT);

		if (decorator == null) {
			compartments();
			links();
			drawReactions();
			drawNodes();
			notes();
		} else {
			compartments();
			links();
			flags();
			haloReactions();
			drawReactions();
			selectReactions();
			haloNodes();
			drawNodes();
			selectNodes();
			notes();
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
				diagram.getCompartments(),
				diagram.getLinks(),
				diagram.getEdges(),
				diagram.getNodes())
				.flatMap(Collection::stream);
	}

	private void compartments() {
		final String rClass = "Compartment";
		final Renderer renderer = RendererFactory.get(rClass);
		final Paint fill = getFillColor(profile, rClass, RenderType.NORMAL);
		final Paint line = getLineColor(profile, rClass, RenderType.NORMAL);
		final Paint text = getTextColor(profile, rClass, RenderType.NORMAL);
		graphics.getGraphics().setPaint(fill);
		renderer.fill(graphics, diagram.getCompartments());
		graphics.getGraphics().setPaint(line);
		graphics.getGraphics().setStroke(StrokeProperties.BORDER_STROKE);
		renderer.border(graphics, diagram.getCompartments());
		graphics.getGraphics().setPaint(text);
		renderer.text(graphics, diagram.getCompartments());
	}

	private void haloNodes() {
		graphics.getGraphics().setPaint(getProfileColor(profile, "halo"));
		index.getHaloNodes().forEach((rClass, nodes) ->
				borderNodes(nodes, RendererFactory.get(rClass), HALO_STROKE, DASHED_HALO_STROKE));
	}

	private void haloReactions() {
		graphics.getGraphics().setPaint(getProfileColor(profile, "halo"));
		graphics.getGraphics().setStroke(HALO_STROKE);
		index.getHaloConnectors().forEach((rClass, items) -> {
			connectorRenderer.segments(graphics, items);
			connectorRenderer.highlight(graphics, items);
		});
		index.getHaloReactions().forEach((rClass, reactions) -> {
			final Renderer renderer = RendererFactory.get(rClass);
			renderer.segments(graphics, reactions);
			renderer.highlight(graphics, reactions);
		});
	}

	private void links() {
		index.getLinks().forEach((renderType, items) ->
				items.forEach((rClass, links) -> {
					final EdgeRenderer renderer = (EdgeRenderer) RendererFactory.get(rClass);
					final Paint lineColor = ColorProfile.getLineColor(profile, rClass, renderType);
					final Paint fillColor = ColorProfile.getFillColor(profile, rClass, renderType);
					if (LINKS.contains(rClass))
						graphics.getGraphics().setStroke(DASHED_SEGMENT_STROKE);
					else
						graphics.getGraphics().setStroke(SEGMENT_STROKE);
					graphics.getGraphics().setPaint(lineColor);
					renderer.segments(graphics, links);
					renderer.draw(graphics, links, fillColor, lineColor);
					// links do not have text
				}));
	}


	private void drawReactions() {
		// 1 segments
		graphics.getGraphics().setStroke(SEGMENT_STROKE);
		index.getClassifiedConnectors().forEach((renderType, items) ->
				items.forEach((rClass, connectors) -> {
					graphics.getGraphics().setPaint(ColorProfile.getLineColor(profile, rClass, renderType));
					connectorRenderer.segments(graphics, connectors);
				}));
		index.getClassifiedReactions().forEach((renderType, items) ->
				items.forEach((rClass, edges) -> {
					final Renderer renderer = RendererFactory.get(rClass);
					final Paint color = ColorProfile.getLineColor(profile, rClass, renderType);
					graphics.getGraphics().setPaint(color);
					renderer.segments(graphics, edges);
				}));
		// 2 Shapes
		index.getClassifiedConnectors().forEach((renderType, items) ->
				items.forEach((rClass, connectors) -> {
					final Paint fill = ColorProfile.getFillColor(profile, rClass, renderType);
					final Paint border = ColorProfile.getLineColor(profile, rClass, renderType);
					connectorRenderer.draw(graphics, connectors, fill, border);
					graphics.getGraphics().setPaint(border);
					connectorRenderer.text(graphics, connectors);
				}));
		index.getClassifiedReactions().forEach((renderType, reactions) ->
				reactions.forEach((rClass, edges) -> {
					final EdgeRenderer renderer = (EdgeRenderer) RendererFactory.get(rClass);
					final Paint fill = ColorProfile.getFillColor(profile, rClass, renderType);
					final Paint border = ColorProfile.getLineColor(profile, rClass, renderType);
					renderer.draw(graphics, edges, fill, border);
					graphics.getGraphics().setPaint(border);
					renderer.text(graphics, edges);
				}));
	}

	private void selectReactions() {
		final Paint selection = getProfileColor(profile, "selection");
		graphics.getGraphics().setPaint(selection);
		graphics.getGraphics().setStroke(SELECTION_STROKE);
		// 1 segments
		index.getSelectedConnectors().forEach((rClass, items) ->
				items.forEach((renderType, connectors) ->
						connectorRenderer.segments(graphics, connectors)));
		index.getSelectedReactions().forEach((renderType, items) ->
				items.forEach((rClass, edges) ->
						RendererFactory.get(rClass).segments(graphics, edges)));
		// 2 Shapes
		index.getSelectedConnectors().forEach((renderType, items) ->
				items.forEach((rClass, connectors) -> {
					final Paint fill = ColorProfile.getFillColor(profile, rClass, renderType);
					connectorRenderer.draw(graphics, connectors, fill, selection);
					graphics.getGraphics().setPaint(selection);
					connectorRenderer.text(graphics, connectors);
				}));
		index.getSelectedReactions().forEach((renderType, items) ->
				items.forEach((rClass, edges) -> {
					final EdgeRenderer renderer = (EdgeRenderer) RendererFactory.get(rClass);
					final Paint fill = ColorProfile.getFillColor(profile, rClass, renderType);
					renderer.draw(graphics, edges, fill, selection);
					graphics.getGraphics().setPaint(selection);
					renderer.text(graphics, edges);
				}));
	}

	private void drawNodes() {
		index.getClassifiedNodes().forEach((renderType, items) ->
				items.forEach((rClass, nodes) -> {
					final Renderer renderer = RendererFactory.get(rClass);
					graphics.getGraphics().setPaint(getFillColor(profile, rClass, renderType));
					renderer.fill(graphics, nodes);
					graphics.getGraphics().setPaint(getLineColor(profile, rClass, renderType));
					borderNodes(nodes, renderer, BORDER_STROKE, DASHED_BORDER_STROKE);
					graphics.getGraphics().setPaint(getTextColor(profile, rClass, renderType));
					renderer.text(graphics, nodes);
					graphics.getGraphics().setPaint(getProfileColor(profile, "disease"));
					graphics.getGraphics().setStroke(SEGMENT_STROKE);
					renderer.cross(graphics, nodes);
				}));
	}

	private void borderNodes(Set<Node> nodes, Renderer renderer, Stroke borderStroke, Stroke dashedStroke) {
		final Map<Boolean, List<Node>> needDash = nodes.stream()
				.collect(Collectors.groupingBy(this::isDashed));
		if (needDash.containsKey(true)) {
			graphics.getGraphics().setStroke(dashedStroke);
			renderer.border(graphics, needDash.get(true));
		}
		if (needDash.containsKey(false)) {
			graphics.getGraphics().setStroke(borderStroke);
			renderer.border(graphics, needDash.get(false));
		}
	}

	private void selectNodes() {
		// Just the border
		graphics.getGraphics().setPaint(getProfileColor(profile, "selection"));
		index.getSelectedNodes().forEach((rClass, nodes) -> {
			final Renderer renderer = RendererFactory.get(rClass);
			borderNodes(nodes, renderer, SELECTION_STROKE, DASHED_SELECTION_STROKE);
		});
	}

	private void flags() {
		graphics.getGraphics().setPaint(ColorProfile.getProfileColor(profile, "flag"));
		index.getFlagNodes().forEach((rClass, nodes) -> {
			final Renderer renderer = RendererFactory.get(rClass);
			borderNodes(nodes, renderer, FLAG_STROKE, DASHED_FLAG_STROKE);
		});

	}

	private boolean isDashed(Node node) {
		return node.getNeedDashedBorder() != null && node.getNeedDashedBorder();
	}

	private void notes() {
		final String renderingClass = "Note";
		final Renderer renderer = RendererFactory.get(renderingClass);
		graphics.getGraphics().setPaint(getTextColor(profile, renderingClass, RenderType.NORMAL));
		renderer.text(graphics, diagram.getNotes());
	}

}
