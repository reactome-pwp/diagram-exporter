package org.reactome.server.tools.diagram.exporter.raster;

import com.sun.org.apache.regexp.internal.RE;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.data.profile.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.*;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.ConnectorRenderer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.Renderer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.RendererFactory;

import java.awt.*;
import java.awt.Shape;
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

		FontProperties.setFactor(factor);
		RendererProperties.setFactor(factor);
		StrokeProperties.setFactor(factor);


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
			drawReactions();
			selectReactions();
			haloNodes();
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
		graphics.getGraphics().setPaint(fillColor);
		renderer.fill(graphics, diagram.getCompartments());
		graphics.getGraphics().setPaint(lineColor);
		graphics.getGraphics().setStroke(StrokeProperties.BORDER_STROKE);
		renderer.border(graphics, diagram.getCompartments());
		graphics.getGraphics().setPaint(textColor);
		renderer.text(graphics, diagram.getCompartments());
	}

	private void haloNodes() {
		graphics.getGraphics().setPaint(getProfileColor(profile, "halo"));
		index.getHaloNodes().forEach((rClass, nodes) -> {
			final Map<Boolean, List<Node>> dashed = nodes.stream()
					.collect(Collectors.groupingBy(this::isDashed));
			if (dashed.containsKey(true)) {
				graphics.getGraphics().setStroke(DASHED_HALO_STROKE);
				RendererFactory.get(rClass).border(graphics, dashed.get(true));
			}
			if (dashed.containsKey(false)) {
				graphics.getGraphics().setStroke(HALO_STROKE);
				RendererFactory.get(rClass).border(graphics, dashed.get(false));
			}
		});
	}

	private void haloReactions() {
		graphics.getGraphics().setPaint(getProfileColor(profile, "halo"));
		graphics.getGraphics().setStroke(HALO_STROKE);
		index.getHaloConnectors().forEach((rClass, items) -> {
			connectorRenderer.segments(graphics, items);
			items.stream()
					.map(Connector::getEndShape)
					.filter(Objects::nonNull)
					.map(shape -> ShapeFactory.createShape(shape, graphics.getFactor()))
					.flatMap(Collection::stream)
					.forEach(graphics.getGraphics()::draw);
		});
		index.getHaloReactions().forEach((rClass, reactions) -> {
			final Renderer renderer = RendererFactory.get(rClass);
			renderer.segments(graphics, reactions);
			reactions.stream()
					.flatMap(reaction -> Stream.of(reaction.getReactionShape(), reaction.getEndShape()))
					.filter(Objects::nonNull)
					.map(shape -> ShapeFactory.createShape(shape, graphics.getFactor()))
					.flatMap(Collection::stream)
					.forEach(graphics.getGraphics()::draw);
		});
	}

	private void links() {
		final List<String> LINKS = Arrays.asList("Interaction", "EntitySetAndEntitySetLink", "EntitySetAndMemberLink");
		index.getLinks().forEach((renderType, items) ->
				items.forEach((rClass, links) -> {
					final Renderer renderer = RendererFactory.get(rClass);
					final Paint lineColor = ColorProfile.getLineColor(profile, rClass, renderType);
					final Paint fillColor = ColorProfile.getFillColor(profile, rClass, renderType);
					if (LINKS.contains(rClass))
						graphics.getGraphics().setStroke(DASHED_SEGMENT_STROKE);
					else
						graphics.getGraphics().setStroke(StrokeProperties.SEGMENT_STROKE);
					graphics.getGraphics().setPaint(lineColor);
					renderer.segments(graphics, links);
					final Map<Boolean, Set<Shape>> divide = divideLinkShapes(links);
					fillBlackAndWhite(lineColor, fillColor, divide);
					// links do not have text
				}));
	}

	private void fillBlackAndWhite(Paint lineColor, Paint fillColor, Map<Boolean, Set<Shape>> shapes) {
		graphics.getGraphics().setPaint(fillColor);
		shapes.get(true).forEach(graphics.getGraphics()::fill);
		graphics.getGraphics().setPaint(lineColor);
		shapes.get(false).forEach(graphics.getGraphics()::fill);
		shapes.values().stream()
				.flatMap(Collection::stream)
				.forEach(graphics.getGraphics()::draw);
	}


	private void drawReactions() {
		// 1 segments
		graphics.getGraphics().setStroke(StrokeProperties.SEGMENT_STROKE);
		index.getClassifiedConnectors().forEach((renderType, items) -> {
			items.forEach((rClass, connectors) -> {
				graphics.getGraphics().setPaint(ColorProfile.getLineColor(profile, rClass, renderType));
				connectorRenderer.segments(graphics, connectors);
			});
		});
		index.getClassifiedReactions().forEach((renderType, items) -> {
			items.forEach((rClass, edges) -> {
				final Renderer renderer = RendererFactory.get(rClass);
				final Paint color = ColorProfile.getLineColor(profile, rClass, renderType);
				graphics.getGraphics().setPaint(color);
				renderer.segments(graphics, edges);
			});
		});
		// 2 Shapes
		index.getClassifiedConnectors().forEach((renderType, items) -> {
			items.forEach((rClass, connectors) -> {
				final Paint fill = ColorProfile.getFillColor(profile, rClass, renderType);
				final Paint border = ColorProfile.getLineColor(profile, rClass, renderType);
				drawConnectors(connectors, fill, border);
			});
		});
		index.getClassifiedReactions().forEach((renderType, reactions) -> {
			reactions.forEach((rClass, edges) -> {
				final Renderer renderer = RendererFactory.get(rClass);
				final Paint fill = ColorProfile.getFillColor(profile, rClass, renderType);
				final Paint border = ColorProfile.getLineColor(profile, rClass, renderType);
				drawEdges(renderer, edges, fill, border);
			});
		});
	}

	private void drawEdges(Renderer renderer, Collection<Edge> edges, Paint fill, Paint border) {
		final Map<Boolean, Set<Shape>> divided = divideShapes(edges);
		fillBlackAndWhite(border, fill, divided);
		// Texts have the same color as lines
		renderer.text(graphics, edges);
	}

	private void drawConnectors(Set<Connector> connectors, Paint fill, Paint border) {
		final Map<Boolean, Set<Shape>> divided = divideConnectorShapes(connectors);
		fillBlackAndWhite(border, fill, divided);
		// Texts have the same color as lines
		connectorRenderer.text(graphics, connectors);
	}

	private Map<Boolean, Set<Shape>> divideLinkShapes(Set<Link> links) {
		return divide(links.stream()
				.map(EdgeCommon::getEndShape));
	}

	private Map<Boolean, Set<Shape>> divideShapes(Collection<Edge> edges) {
		return divide(edges.stream()
				.flatMap(edge -> Stream.of(edge.getReactionShape(), edge.getEndShape())));
	}

	private Map<Boolean, Set<Shape>> divideConnectorShapes(Collection<Connector> connectors) {
		return divide(connectors.stream()
				.flatMap(connector -> Stream.of(connector.getEndShape(), connector.getStoichiometry().getShape())));
	}

	private Map<Boolean, Set<Shape>> divide(Stream<org.reactome.server.tools.diagram.data.layout.Shape> shapeStream) {
		final Map<Boolean, Set<Shape>> shapes = new HashMap<>();
		shapes.put(true, new HashSet<>());
		shapes.put(false, new HashSet<>());
		shapeStream
				.filter(Objects::nonNull)
				.forEach(shape -> {
					final List<Shape> javaShapes = ShapeFactory.createShape(shape, graphics.getFactor());
					shapes.get(isEmpty(shape)).addAll(javaShapes);
				});
		return shapes;
	}

	private boolean isEmpty(org.reactome.server.tools.diagram.data.layout.Shape shape) {
		return shape.getEmpty() != null && shape.getEmpty();
	}

	private void selectReactions() {
		final Paint selection = getProfileColor(profile, "selection");
		graphics.getGraphics().setPaint(selection);
		graphics.getGraphics().setStroke(StrokeProperties.SELECTION_STROKE);
		// 1 segments
		index.getSelectedConnectors().forEach((rClass, items) -> {
			items.forEach((renderType, connectors) -> {
				connectorRenderer.segments(graphics, connectors);
			});
		});
		index.getSelectedReactions().forEach((renderType, items) -> {
			items.forEach((rClass, edges) -> {
				final Renderer renderer = RendererFactory.get(rClass);
				renderer.segments(graphics, edges);
			});
		});
		// 2 Shapes
		index.getSelectedConnectors().forEach((renderType, items) -> {
			items.forEach((rClass, connectors) -> {
				final Paint fill = ColorProfile.getFillColor(profile, rClass, renderType);
				drawConnectors(connectors, fill, selection);
			});
		});
		index.getSelectedReactions().forEach((renderType, items) -> {
			items.forEach((rClass, edges) -> {
				final Renderer renderer = RendererFactory.get(rClass);
				final Paint fill = ColorProfile.getFillColor(profile, rClass, renderType);
				drawEdges(renderer, edges, fill, selection);
			});
		});
	}

	private void drawNodes() {
		index.getClassifiedNodes().forEach((renderType, items) -> {
			items.forEach((rClass, nodes) -> {
				final Renderer renderer = RendererFactory.get(rClass);
				graphics.getGraphics().setPaint(getFillColor(profile, rClass, renderType));
				renderer.fill(graphics, nodes);
				graphics.getGraphics().setPaint(getLineColor(profile, rClass, renderType));
				final Map<Boolean, List<Node>> needDash = nodes.stream().collect(
						Collectors.groupingBy(this::isDashed));
				if (needDash.containsKey(true)) {
					graphics.getGraphics().setStroke(DASHED_BORDER_STROKE);
					renderer.border(graphics, needDash.get(true));
				}
				if (needDash.containsKey(false)) {
					graphics.getGraphics().setStroke(BORDER_STROKE);
					renderer.border(graphics, needDash.get(false));
				}
				graphics.getGraphics().setPaint(getTextColor(profile, rClass, renderType));
				renderer.text(graphics, nodes);
				graphics.getGraphics().setPaint(getProfileColor(profile, "disease"));
				graphics.getGraphics().setStroke(SEGMENT_STROKE);
				renderer.cross(graphics, nodes);
			});
		});
	}

	private void selectNodes() {
		// Just the border
		graphics.getGraphics().setPaint(getProfileColor(profile, "selection"));
		index.getSelectedNodes().forEach((rClass, nodes) -> {
			final Renderer renderer = RendererFactory.get(rClass);
			final Map<Boolean, List<Node>> needDash = nodes.stream().collect(
					Collectors.groupingBy(this::isDashed));
			if (needDash.containsKey(true)) {
				graphics.getGraphics().setStroke(DASHED_SELECTION_STROKE);
				renderer.border(graphics, needDash.get(true));
			}
			if (needDash.containsKey(false)) {
				graphics.getGraphics().setStroke(SELECTION_STROKE);
				renderer.border(graphics, needDash.get(false));
			}
		});
	}

	private void flags() {
		graphics.getGraphics().setPaint(ColorProfile.getProfileColor(profile, "flag"));
		index.getFlagNodes().forEach((rClass, nodes) -> {
			final Renderer renderer = RendererFactory.get(rClass);
			final Map<Boolean, List<Node>> needDash = nodes.stream().collect(
					Collectors.groupingBy(this::isDashed));
			if (needDash.containsKey(true)) {
				graphics.getGraphics().setStroke(DASHED_SELECTION_STROKE);
				renderer.border(graphics, needDash.get(true));
			}
			if (needDash.containsKey(false)) {
				graphics.getGraphics().setStroke(SELECTION_STROKE);
				renderer.border(graphics, needDash.get(false));
			}
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

	private void shadows() {
		graphics.getGraphics().setFont(FontProperties.SHADOWS_FONT);
		graphics.getGraphics().setStroke(StrokeProperties.BORDER_STROKE);
		// Each shadow has a different color and different renderable Class
		diagram.getShadows().forEach(shadow -> {
			final Renderer renderer = RendererFactory.get(shadow.getRenderableClass());
			graphics.getGraphics().setPaint(ColorProfile.getShadowFill(shadow));
			renderer.fill(graphics, Collections.singleton(shadow));
			graphics.getGraphics().setPaint(ColorProfile.getShadowLine(shadow));
			renderer.border(graphics, Collections.singleton(shadow));
			renderer.text(graphics, Collections.singleton(shadow));
		});
	}
}
