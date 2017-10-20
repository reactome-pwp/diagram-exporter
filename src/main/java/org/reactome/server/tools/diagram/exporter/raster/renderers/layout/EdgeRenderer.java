package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.raster.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.EdgeRenderInfo;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Renders edges, connectors and links.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class EdgeRenderer extends AbstractRenderer {

	/**
	 * Renders an edge and the connectors associated to it.
	 *
	 * @param canvas where to render
	 * @param item   the edge to render
	 * @param index  the diagram index
	 */
	@Override
	public void draw(DiagramCanvas canvas, DiagramObject item, ColorProfiles colorProfiles, DiagramIndex index) {
		final EdgeCommon edge = (EdgeCommon) item;
		final EdgeRenderInfo info = new EdgeRenderInfo(edge, dashed(edge), colorProfiles, index, canvas);
		segments(canvas, info);
		shapes(canvas, edge, info);
		stoichiometries(canvas, info);
	}

	private void segments(DiagramCanvas canvas, EdgeRenderInfo info) {
		if (info.getDecorator().isHalo())
			info.getSegments().forEach(shape -> canvas.getHalo().add(info.getHaloColor(), info.getHaloStroke(), shape));
		info.getSegments().forEach(shape -> info.getSegmentsLayer().add(info.getLineColor(), info.getSegmentStroke(), shape));
	}

	private void shapes(DiagramCanvas canvas, EdgeCommon edge, EdgeRenderInfo info) {
		LinkedList<Shape> rShapes = new LinkedList<>();
		renderableShapes(edge).stream()
				.filter(Objects::nonNull)
				.forEach(rShapes::add);
		info.getDecorator().getConnectors().stream()
				.map(Connector::getEndShape)
				.filter(Objects::nonNull)
				.forEach(rShapes::add);
		rShapes.forEach(shape -> renderShape(canvas, info, shape));
	}

	private void renderShape(DiagramCanvas canvas, EdgeRenderInfo info, org.reactome.server.tools.diagram.data.layout.Shape shape) {
		final List<java.awt.Shape> javaShapes = ShapeFactory.getShapes(shape);
		// 2.1 halo
		if (info.getDecorator().isHalo())
			javaShapes.forEach(sh -> canvas.getFlags().add(info.getHaloColor(), info.getHaloStroke(), sh));
		// 2.2 fill
		final Color color = shape.getEmpty() != null && shape.getEmpty() ? info.getFillColor() : info.getLineColor();
		javaShapes.forEach(sh -> info.getShapeLayer().add(color, info.getLineColor(), info.getLineStroke(), sh));
		// 2.4 text
		if (shape.getS() != null && !shape.getS().isEmpty()) {
			final NodeProperties limits = NodePropertiesFactory.get(
					shape.getA().getX(), shape.getA().getY(),
					shape.getB().getX() - shape.getA().getX(),
					shape.getB().getY() - shape.getA().getY());
			info.getTextLayer().add(info.getLineColor(), shape.getS(), limits, 1, 0);
		}
	}

	private void stoichiometries(DiagramCanvas canvas, EdgeRenderInfo info) {
		info.getDecorator().getConnectors().stream()
				.map(Connector::getStoichiometry)
				.filter(Objects::nonNull)
				.filter(stoichiometry -> stoichiometry.getShape() != null)
				.forEach(stoichiometry -> renderStoichiometry(canvas, info, stoichiometry));
	}

	private void renderStoichiometry(DiagramCanvas canvas, EdgeRenderInfo info, Stoichiometry stoichiometry) {
		final Shape stShape = stoichiometry.getShape();
		final List<java.awt.Shape> shapes = ShapeFactory.getShapes(stShape);
		if (info.getDecorator().isHalo())
			shapes.forEach(sh -> canvas.getHalo().add(info.getHaloColor(), info.getHaloStroke(), sh));
		final Color fill = stShape.getEmpty() != null && stShape.getEmpty()
				? info.getFillColor() : info.getLineColor();
		shapes.forEach(sh -> info.getShapeLayer().add(fill, info.getLineColor(), info.getLineStroke(), sh));
		final String text = stoichiometry.getValue().toString();
		final NodeProperties limits =
				NodePropertiesFactory.get(
						stShape.getA().getX(), stShape.getA().getY(),
						stShape.getB().getX() - stShape.getA().getX(),
						stShape.getB().getY() - stShape.getA().getY());
		info.getTextLayer().add(info.getLineColor(), text, limits, 1, 0);
	}

	/**
	 * get a list of shapes that this edge needs to render: reaction and end for
	 * reactions, end for links
	 */
	protected List<Shape> renderableShapes(EdgeCommon edge) {
		return Arrays.asList(edge.getReactionShape(), edge.getEndShape());
	}

	/**
	 * links should override this if they want to be dashed
	 */
	protected boolean dashed(EdgeCommon edge) {
		return false;
	}


}
