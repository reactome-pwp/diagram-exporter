package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.Edge;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.Stoichiometry;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;
import java.util.Objects;

public abstract class RenderableEdge extends RenderableEdgeCommon<Edge> {

	RenderableEdge(Edge edge) {
		super(edge);
	}

	@Override
	public void draw(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		super.draw(canvas, colorProfiles, index, t);
		final Color linesColor = getStrokeColor(colorProfiles, index.getAnalysis().getType());
		stoichiometryText(linesColor, canvas);
	}

	private void stoichiometryText(Color linesColor, DiagramCanvas canvas) {
		getConnectors().stream()
				.map(Connector::getStoichiometry)
				.filter(Objects::nonNull)
				.filter(stoichiometry -> stoichiometry.getShape() != null)
				.forEach(stoichiometry -> stoichiometryText(stoichiometry, linesColor, canvas));
	}

	private void stoichiometryText(Stoichiometry stoichiometry, Color linesColor, DiagramCanvas canvas) {
		final org.reactome.server.tools.diagram.data.layout.Shape stShape = stoichiometry.getShape();
		final String text = stoichiometry.getValue().toString();
		final NodeProperties limits =
				NodePropertiesFactory.get(
						stShape.getA().getX(), stShape.getA().getY(),
						stShape.getB().getX() - stShape.getA().getX(),
						stShape.getB().getY() - stShape.getA().getY());
		canvas.getText().add(text, linesColor, limits, 1, 0, FontProperties.DEFAULT_FONT);
	}
}
