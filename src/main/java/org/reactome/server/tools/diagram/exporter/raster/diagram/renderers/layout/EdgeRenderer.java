package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.Shape;
import org.reactome.server.tools.diagram.data.layout.Stoichiometry;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableEdge;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableObject;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.StrokeStyle;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;
import java.util.Objects;

/**
 * Renders edges, connectors and links.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class EdgeRenderer extends ObjectRenderer {

	public void draw(RenderableEdge edge, DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index) {
		final Color linesColor = getStrokeColor(edge, colorProfiles, index);
		final Color fillColor = getFillColor(edge, colorProfiles, index);
		segments(edge, linesColor, canvas, colorProfiles);
		shapes(edge, linesColor, fillColor, canvas, colorProfiles);
		stoichiometryText(edge, linesColor, canvas);
		// stoichiometry text is not in stoichiometry.getShape().getS()
		// but in stoichiometry.getValue()
	}

	@Override
	protected Color getFillColor(RenderableObject renderableNode, ColorProfiles colorProfiles, DiagramIndex index) {
		//fadeout -> analysis -> normal
		if (renderableNode.isFadeOut())
			return renderableNode.getColorProfile(colorProfiles).getFadeOutFill();
		// report: lighterfill of reaction is grey as text
		if (index.getAnalysis().getType() != AnalysisType.NONE)
			return renderableNode.getColorProfile(colorProfiles).getFill();
		return renderableNode.getColorProfile(colorProfiles).getFill();
	}

	private void segments(RenderableEdge edge, Color linesColor, DiagramCanvas canvas, ColorProfiles colorProfiles) {
		if (edge.isHalo())
			edge.getSegments().forEach(shape -> canvas.getHalo().add(shape,
					colorProfiles.getDiagramSheet().getProperties().getHalo(),
					StrokeStyle.HALO.get(edge.isDashed())
			));
		edge.getSegments().forEach(shape -> canvas.getSegments().add(shape,
				linesColor, StrokeStyle.SEGMENT.get(edge.isDashed())));
	}

	private void shapes(RenderableEdge edge, Color linesColor, Color fillColor, DiagramCanvas canvas, ColorProfiles colorProfiles) {
		edge.getShapes().forEach(shape -> {
			final java.awt.Shape awtShape = ShapeFactory.getShape(shape);
			// todo: allow reaction flagging
//			if (edge.isFlag()) flag(edge, canvas, colorProfiles, awtShape);
			if (edge.isHalo()) halo(edge, canvas, colorProfiles, awtShape);
			final Color color = shape.getEmpty() != null && shape.getEmpty()
					? fillColor
					: linesColor;
			canvas.getEdgeShapes().add(awtShape, color, linesColor, StrokeStyle.BORDER.get(edge.isDashed()));
			if (shape.getS() != null && !shape.getS().isEmpty()) {
				final NodeProperties limits = NodePropertiesFactory.get(
						shape.getA().getX(), shape.getA().getY(),
						shape.getB().getX() - shape.getA().getX(),
						shape.getB().getY() - shape.getA().getY());
				canvas.getText().add(shape.getS(), linesColor, limits, 1, 0, FontProperties.DEFAULT_FONT);
			}
		});
	}

	private void flag(RenderableEdge edge, DiagramCanvas canvas, ColorProfiles colorProfiles, java.awt.Shape awtShape) {
		canvas.getFlags().add(awtShape,
				colorProfiles.getDiagramSheet().getProperties().getFlag(),
				StrokeStyle.FLAG.get(edge.isDashed()));
	}

	private void halo(RenderableEdge edge, DiagramCanvas canvas, ColorProfiles colorProfiles, java.awt.Shape awtShape) {
		canvas.getHalo().add(awtShape,
				colorProfiles.getDiagramSheet().getProperties().getHalo(),
				StrokeStyle.BORDER.get(edge.isDashed()));
	}

	private void stoichiometryText(RenderableEdge edge, Color linesColor, DiagramCanvas canvas) {
		edge.getConnectors().stream()
				.map(Connector::getStoichiometry)
				.filter(Objects::nonNull)
				.filter(stoichiometry -> stoichiometry.getShape() != null)
				.forEach(stoichiometry -> stoichiometryText(stoichiometry, linesColor, canvas));
	}

	private void stoichiometryText(Stoichiometry stoichiometry, Color linesColor, DiagramCanvas canvas) {
		final Shape stShape = stoichiometry.getShape();
		final String text = stoichiometry.getValue().toString();
		final NodeProperties limits =
				NodePropertiesFactory.get(
						stShape.getA().getX(), stShape.getA().getY(),
						stShape.getB().getX() - stShape.getA().getX(),
						stShape.getB().getY() - stShape.getA().getY());
		canvas.getText().add(text, linesColor, limits, 1, 0, FontProperties.DEFAULT_FONT);
	}

}
