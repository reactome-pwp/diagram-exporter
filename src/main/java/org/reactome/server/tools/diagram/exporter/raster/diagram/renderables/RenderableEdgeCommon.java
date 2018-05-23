package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.EdgeCommon;
import org.reactome.server.tools.diagram.data.layout.Segment;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains extra rendering information for an edge: decorators plus
 * connectors.
 */
public abstract class RenderableEdgeCommon<T extends EdgeCommon> extends RenderableDiagramObject<T> {

	private List<Shape> shapes;
	private List<Connector> connectors = new LinkedList<>();
	private Collection<java.awt.Shape> segments;

	RenderableEdgeCommon(T edge) {
		super(edge);
	}

	public List<Connector> getConnectors() {
		return connectors;
	}

	public T getEdge() {
		return getDiagramObject();
	}

	public boolean isDashed() {
		return false;
	}

	public Collection<java.awt.Shape> getSegments() {
		if (segments == null) createSegments();
		return segments;
	}

	private void createSegments() {
		segments = new LinkedList<>();
		for (Segment segment : getEdge().getSegments())
			segments.add(ShapeFactory.line(segment.getFrom(), segment.getTo()));
		for (Connector connector : connectors)
			for (Segment segment : connector.getSegments())
				segments.add(ShapeFactory.line(segment.getFrom(), segment.getTo()));
	}

	public List<Shape> getShapes() {
		if (shapes == null)
			createShapes();
		return shapes;
	}

	private void createShapes() {
		shapes = new LinkedList<>();
		for (Shape shape : getRenderableShapes())
			if (shape != null) shapes.add(shape);
		for (Connector connector : connectors)
			if (connector.getEndShape() != null)
				shapes.add(connector.getEndShape());
		for (Connector connector : connectors)
			if (connector.getStoichiometry().getShape() != null)
				shapes.add(connector.getStoichiometry().getShape());
	}

	protected List<Shape> getRenderableShapes() {
		return Arrays.asList(getEdge().getReactionShape(), getEdge().getEndShape());
	}
}
