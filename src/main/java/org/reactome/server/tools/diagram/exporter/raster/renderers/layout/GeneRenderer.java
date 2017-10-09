package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.AdvancedGraphics2D;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ScaledNodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.awt.*;
import java.util.Collection;

/**
 * Renderer for genes. These ones are a little bit more complex than the rest.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class GeneRenderer extends NodeAbstractRenderer {

	@Override
	public void fill(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		final Collection<Node> nodes = (Collection<Node>) items;
		nodes.stream()
				.map(node -> shape(graphics, node))
				.forEach(shape -> graphics.getGraphics().fill(shape));
	}

	@Override
	public void border(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		final Collection<Node> nodes = (Collection<Node>) items;
		nodes.stream()
				.map(node -> line(graphics, node))
				.forEach(shape -> graphics.getGraphics().draw(shape));
		nodes.stream()
				.map(node -> arrow(graphics, node))
				.forEach(shape -> graphics.getGraphics().fill(shape));
	}

	@Override
	public void text(AdvancedGraphics2D graphics, Collection<? extends DiagramObject> items) {
		final Collection<Node> nodes = (Collection<Node>) items;
		nodes.forEach(node -> {
			final double x = node.getProp().getX();
			final double width = node.getProp().getWidth();
			final double yOffset = 0.5 * RendererProperties.GENE_SYMBOL_WIDTH / graphics.getFactor();
			final double y = node.getProp().getY() + yOffset;
			final double height = node.getProp().getHeight() - yOffset;
			TextRenderer.drawText(graphics, node.getDisplayName(), x, y, width, height);
		});
	}

	@Override
	protected Shape shape(AdvancedGraphics2D graphics, DiagramObject item) {
		final Node node = (Node) item;
		final NodeProperties prop = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		final double x = prop.getX();
		final double y = prop.getY();
		final double width = prop.getWidth();
		final double height = prop.getHeight();
		return ShapeFactory.getGeneFillShape(x, y, width, height);
	}

	private Shape line(AdvancedGraphics2D graphics, Node node) {
		final NodeProperties prop = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		final double x = prop.getX();
		final double y = prop.getY();
		final double width = prop.getWidth();
		return ShapeFactory.getGeneLine(x, y, width);
	}

	private Shape arrow(AdvancedGraphics2D graphics, Node node) {
		final NodeProperties prop = new ScaledNodeProperties(node.getProp(), graphics.getFactor());
		final double x = prop.getX();
		final double y = prop.getY();
		final double width = prop.getWidth();
		return ShapeFactory.getGeneArrow(x, y, width);
	}

}
