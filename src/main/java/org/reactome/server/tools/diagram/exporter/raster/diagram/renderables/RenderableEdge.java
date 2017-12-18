package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.EdgeCommon;
import org.reactome.server.tools.diagram.data.layout.Segment;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.EdgeRenderer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains extra rendering information for an edge: decorators plus
 * connectors.
 */
public abstract class RenderableEdge extends RenderableObject {

	private List<Shape> shapes;
	private List<Connector> connectors = new LinkedList<>();
	private EdgeCommon edge;
	private Collection<java.awt.Shape> segments;

	public RenderableEdge(EdgeCommon edge) {
		super(edge);
		this.edge = edge;
	}

	public List<Connector> getConnectors() {
		return connectors;
	}

	public EdgeCommon getEdge() {
		return edge;
	}

	public void render(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index) {
		getRenderer().draw(this, canvas, colorProfiles, index);
	}

	public abstract EdgeRenderer getRenderer();

	public boolean isDashed() {
		return false;
	}

	public Collection<java.awt.Shape> getSegments() {
		if (segments == null) createSegments();
		return segments;
	}

	private void createSegments() {
		segments = new LinkedList<>();
		for (Segment segment : edge.getSegments())
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
		return Arrays.asList(edge.getReactionShape(), edge.getEndShape());
	}
}
