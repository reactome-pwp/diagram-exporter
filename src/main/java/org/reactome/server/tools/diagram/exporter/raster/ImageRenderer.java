package org.reactome.server.tools.diagram.exporter.raster;

import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.data.profile.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.pptx.model.Decorator;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ColorProfile;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.ConnectorRenderer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.EdgeAbstractRenderer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.Renderer;
import org.reactome.server.tools.diagram.exporter.raster.renderers.layout.RendererFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.reactome.server.tools.diagram.exporter.raster.renderers.common.ColorProfile.*;

/**
 * Renders Reactome pathway diagrams into <code>RendererImage</code>s.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ImageRenderer {

	private final Diagram diagram;
	private final Graph graph;
	private final DiagramProfile profile;
	private final Decorator decorator;


	// OPTIONS
	private int margin = 15;
	private boolean fixedMargin = true;
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
	 * @return a RenderedImage with the given dimensions
	 */
	public BufferedImage render(double factor) {
		RendererProperties.setFactor(factor);
		ColorProfile.setFactor(factor);

		// Bounds are recalculated reading nodes, we don't trust diagram bounds
		final double minX = getMinX();
		final double maxX = getMaxX();
		final double minY = getMinY();
		final double maxY = getMaxY();
//		System.out.printf("%f, %f, %f, %f\n", minX, minY, maxX, maxY);

		final int width = Double.valueOf(factor * (maxX - minX) + 2 * margin).intValue();
		final int height = Double.valueOf(factor * (maxY - minY) + 2 * margin).intValue();

		final double x = minX * factor;
		final double y = minY * factor;
		graphics = new AdvancedGraphics2D(width, height, factor, x, y, margin);

		if (decorator == null) {
			compartments();
			shadows();
			drawReactionSegments();
			fillReactions();
			drawReactions();
			fillNodes();
			drawNodes();
			textNodes();
			textReactions();
			notes();
		} else {
			compartments();
			shadows();
			haloReactions();
			haloNodes();
			drawReactionSegments();
			selectReactionSegments();
			fillReactions();
			drawReactions();
			selectReactionShapes();
			selectReactionBorders();
			fillNodes();
			drawNodes();
			selectNodes();
			textNodes();
			textReactions();
			flags();
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
				diagram.getLinks(),
				diagram.getCompartments(),
				diagram.getNodes(),
				diagram.getShadows())
				.flatMap(Collection::stream);
	}

	private void compartments() {
		final String renderingClass = "Compartment";
		final Renderer renderer = RendererFactory.get(renderingClass);
		graphics.getGraphics().setPaint(getFillColor(profile, renderingClass, RenderType.NORMAL));
		graphics.getGraphics().setStroke(ColorProfile.DEFAULT_BORDER_STROKE);
		diagram.getCompartments().forEach(compartment ->
				renderer.fill(graphics, compartment));
		graphics.getGraphics().setPaint(getLineColor(profile, renderingClass, RenderType.NORMAL));
		diagram.getCompartments().forEach(compartment ->
				renderer.drawBorder(graphics, compartment));
		graphics.getGraphics().setPaint(getTextColor(profile, renderingClass, RenderType.NORMAL));
		diagram.getCompartments().forEach(compartment ->
				renderer.drawText(graphics, compartment));
	}

	private void selectReactionSegments() {
		graphics.getGraphics().setStroke(SELECTION_STROKE);
		graphics.getGraphics().setPaint(getProfileColor(profile, "selection"));
		index.getSelectedReactions().forEach(reaction -> {
			final EdgeAbstractRenderer renderer = (EdgeAbstractRenderer)
					RendererFactory.get(reaction.getRenderableClass());
			renderer.drawSegments(graphics, reaction);
		});
		index.getSelectedConnectors().forEach(connector ->
				connectorRenderer.drawSegments(graphics, connector));
	}

	private void selectReactionShapes() {
		graphics.getGraphics().setPaint(getProfileColor(profile, "selection"));
		index.getSelectedReactions().forEach(edge -> {
			final EdgeAbstractRenderer renderer = (EdgeAbstractRenderer)
					RendererFactory.get(edge.getRenderableClass());
			if (edge.getEndShape().getEmpty() == null) {
				renderer.fillShape(graphics, edge.getEndShape());
			}
			if (edge.getReactionShape().getEmpty() == null) {
				renderer.fillShape(graphics, edge.getReactionShape());
			}
		});
	}

	private void haloNodes() {
		graphics.getGraphics().setStroke(HALO_STROKE);
		graphics.getGraphics().setPaint(getProfileColor(profile, "halo"));
		index.getHaloNodes().forEach(item ->
				RendererFactory.get(item.getRenderableClass()).drawBorder(graphics, item));
	}

	private void haloReactions() {
		graphics.getGraphics().setStroke(HALO_STROKE);
		graphics.getGraphics().setPaint(ColorProfile.getProfileColor(profile, "halo"));
		index.getHaloReactions().forEach(reaction -> {
			final EdgeAbstractRenderer renderer = (EdgeAbstractRenderer)
					RendererFactory.get(reaction.getRenderableClass());
			renderer.drawBorder(graphics, reaction);
			renderer.drawSegments(graphics, reaction);
		});
		final ConnectorRenderer connectorRenderer = new ConnectorRenderer();
		index.getHaloConnectors().forEach(connector -> {
			connectorRenderer.draw(graphics, connector);
			connectorRenderer.drawSegments(graphics, connector);
		});
	}

	private void fillNodes() {
		index.getClassifiedNodes().forEach((renderingClass, subitems) -> {
			final Renderer renderer = RendererFactory.get(renderingClass);
			subitems.forEach((renderType, objects) -> {
				graphics.getGraphics().setPaint(getFillColor(profile, renderingClass, renderType));
				objects.forEach(diagramObject -> renderer.fill(graphics, diagramObject));
			});
		});
	}

	private void drawNodes() {
		graphics.getGraphics().setStroke(DEFAULT_BORDER_STROKE);
		index.getClassifiedNodes().forEach((renderingClass, subitems) -> {
			final Renderer renderer = RendererFactory.get(renderingClass);
			subitems.forEach((renderType, objects) -> {
				graphics.getGraphics().setPaint(getLineColor(profile, renderingClass, renderType));
				objects.forEach(diagramObject -> renderer.drawBorder(graphics, diagramObject));
			});
		});
	}

	private void selectNodes() {
		graphics.getGraphics().setStroke(SELECTION_STROKE);
		graphics.getGraphics().setPaint(getProfileColor(profile, "selection"));
		index.getSelectedNodes().forEach(node ->
				RendererFactory.get(node.getRenderableClass()).drawBorder(graphics, node));
	}

	private void textNodes() {
		index.getClassifiedNodes().forEach((renderingClass, items) -> {
			final Renderer renderer = RendererFactory.get(renderingClass);
			items.forEach((renderType, nodes) -> {
				graphics.getGraphics().setPaint(getTextColor(profile, renderingClass, renderType));
				nodes.forEach(node -> renderer.drawText(graphics, node));
			});
		});
	}

	private void drawReactionSegments() {
		graphics.getGraphics().setStroke(DEFAULT_LINE_STROKE);
		index.getClassifiedReactions().forEach((renderingClass, items) -> {
			final EdgeAbstractRenderer renderer = (EdgeAbstractRenderer) RendererFactory.get(renderingClass);
			items.forEach((renderType, edges) -> {
				graphics.getGraphics().setPaint(getLineColor(profile, renderingClass, renderType));
				edges.forEach(edge -> renderer.drawSegments(graphics, edge));
			});
		});
		index.getClassifiedConnectors().forEach((renderingClass, items) ->
				items.forEach((renderType, connectors) -> {
					graphics.getGraphics().setPaint(getLineColor(profile, renderingClass, renderType));
					connectors.forEach(connector -> connectorRenderer.drawSegments(graphics, connector));
				}));
	}

	private void textReactions() {
		graphics.getGraphics().setPaint(getLineColor(profile, "Reaction", RenderType.NORMAL));
		diagram.getNodes().stream()
				.map(Node::getConnectors)
				.flatMap(Collection::stream)
				.forEach(connector -> connectorRenderer.drawText(graphics, connector));
		index.getClassifiedReactions().forEach((renderingClass, items) -> {
			final Renderer renderer = RendererFactory.get(renderingClass);
			items.forEach((renderType, edges) -> {
				graphics.getGraphics().setPaint(getLineColor(profile, renderingClass, renderType));
				edges.forEach(edge -> renderer.drawText(graphics, edge));
			});
		});
	}

	private void fillReactions() {
		// Reactions
		index.getClassifiedReactions().forEach((renderingClass, items) -> {
			final EdgeAbstractRenderer renderer = (EdgeAbstractRenderer) RendererFactory.get(renderingClass);
			items.forEach((renderType, edges) -> {
				final Paint fillColor = getFillColor(profile, renderingClass, renderType);
				final Paint lineColor = getLineColor(profile, renderingClass, renderType);
				// separate reactions and ends in black and white
				final List<Shape> reactions = edges.stream()
						.map(EdgeCommon::getReactionShape)
						.filter(Objects::nonNull)
						.collect(Collectors.toList());
				final List<Shape> ends = edges.stream()
						.map(EdgeCommon::getEndShape)
						.filter(Objects::nonNull)
						.collect(Collectors.toList());
				final Map<Boolean, List<Shape>> shapes = Stream.of(reactions, ends)
						.flatMap(Collection::stream)
						.collect(Collectors.groupingBy(shape -> shape.getEmpty() != null));
				// white
				if (shapes.containsKey(true)) {
					graphics.getGraphics().setPaint(fillColor);
					shapes.get(true).forEach(shape -> renderer.fillShape(graphics, shape));
				}
				// black
				if (shapes.containsKey(false)) {
					graphics.getGraphics().setPaint(lineColor);
					shapes.get(false).forEach(shape -> renderer.fillShape(graphics, shape));
				}
			});
		});
		// Connectors
		index.getClassifiedNodes().forEach((renderingClass, map) ->
				map.forEach((renderType, nodes) -> {
					graphics.getGraphics().setStroke(DEFAULT_LINE_STROKE);
					final Paint lineColor = getLineColor(profile, "Reaction", renderType);
					final Paint fillColor = getFillColor(profile, "Reaction", renderType);
					final Map<Boolean, List<Shape>> shapes = nodes.stream()
							.map(Node::getConnectors)
							.flatMap(Collection::stream)
							.map(Connector::getEndShape)
							.filter(Objects::nonNull)
							.collect(Collectors.groupingBy(o -> o.getEmpty() != null));
					// white
					if (shapes.containsKey(true)) {
						graphics.getGraphics().setPaint(fillColor);
						shapes.get(true).forEach(shape -> connectorRenderer.fillShape(graphics, shape));
					}
					// black
					if (shapes.containsKey(false)) {
						graphics.getGraphics().setPaint(lineColor);
						shapes.get(false).forEach(shape -> connectorRenderer.fillShape(graphics, shape));
					}
				}));
	}

	private void drawReactions() {
		graphics.getGraphics().setStroke(DEFAULT_LINE_STROKE);
		index.getClassifiedReactions().forEach((renderingClass, items) -> {
			final EdgeAbstractRenderer renderer = (EdgeAbstractRenderer) RendererFactory.get(renderingClass);
			items.forEach((renderType, edges) -> {
				graphics.getGraphics().setPaint(getLineColor(profile, renderingClass, renderType));
				edges.forEach(edge -> renderer.drawBorder(graphics, edge));
			});
		});
		index.getClassifiedConnectors().forEach((renderingClass, items) -> {
			items.forEach((renderType, connectors) -> {
				graphics.getGraphics().setPaint(getLineColor(profile, renderingClass, renderType));
				connectors.forEach(connector -> connectorRenderer.draw(graphics, connector));
			});
		});
	}

	public void setFixedMargin(boolean fixedMargin) {
		this.fixedMargin = fixedMargin;
	}

	private void flags() {
		graphics.getGraphics().setStroke(ColorProfile.SELECTION_STROKE);
		graphics.getGraphics().setPaint(ColorProfile.getProfileColor(profile, "flag"));
		index.getFlagNodes().forEach(item ->
				RendererFactory.get(item.getRenderableClass()).drawBorder(graphics, item));
	}

	private void selectReactionBorders() {
		graphics.getGraphics().setStroke(DEFAULT_LINE_STROKE);
		graphics.getGraphics().setPaint(getProfileColor(profile, "selection"));
		index.getSelectedReactions().forEach(reaction ->
				RendererFactory.get(reaction.getRenderableClass()).drawBorder(graphics, reaction));
		index.getSelectedConnectors().forEach(connector ->
				connectorRenderer.draw(graphics, connector));
	}

	private void notes() {
		final String renderingClass = "Note";
		final Renderer renderer = RendererFactory.get(renderingClass);
		graphics.getGraphics().setStroke(DEFAULT_BORDER_STROKE);
		graphics.getGraphics().setPaint(getFillColor(profile, renderingClass, RenderType.NORMAL));
		diagram.getNotes().forEach(note -> renderer.fill(graphics, note));
		graphics.getGraphics().setPaint(getLineColor(profile, renderingClass, RenderType.NORMAL));
		diagram.getNotes().forEach(note -> renderer.drawBorder(graphics, note));
		graphics.getGraphics().setPaint(getTextColor(profile, renderingClass, RenderType.NORMAL));
		diagram.getNotes().forEach(note -> renderer.drawText(graphics, note));
	}

	private void shadows() {
		graphics.getGraphics().setFont(ColorProfile.SHADOWS_FONT);
		diagram.getShadows().forEach(shadow -> {
			final Paint shadowFill = ColorProfile.getShadowFill(shadow);
			final Paint shadowLine = ColorProfile.getShadowLine(shadow);
			graphics.getGraphics().setPaint(shadowFill);
			RendererFactory.get(shadow.getRenderableClass()).fill(graphics, shadow);
			graphics.getGraphics().setPaint(shadowLine);
			RendererFactory.get(shadow.getRenderableClass()).drawBorder(graphics, shadow);
			RendererFactory.get(shadow.getRenderableClass()).drawText(graphics, shadow);
		});
	}
}
