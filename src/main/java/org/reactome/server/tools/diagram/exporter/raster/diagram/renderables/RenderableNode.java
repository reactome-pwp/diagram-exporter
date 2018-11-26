package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.analysis.core.result.model.FoundEntity;
import org.reactome.server.analysis.core.result.model.IdentifierSummary;
import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.Segment;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramData;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.StrokeStyle;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.FillDrawLayer;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.TextLayer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorFactory;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.GradientSheet;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class RenderableNode extends RenderableNodeCommon<Node> {

	static final double NODE_TEXT_PADDING = 5;
	private final Area backgroundArea;
	private final Shape backgroundShape;
	private List<FoundEntity> hitExpressions;
	private Double enrichment;
	private Integer totalExpressions;
	private Double enrichmentValue;


	RenderableNode(Node node) {
		super(node);
		this.backgroundShape = backgroundShape();
		this.backgroundArea = new Area(backgroundShape());
	}

	abstract Shape backgroundShape();

	public Double getEnrichment() {
		return enrichment;
	}

	public void setEnrichmentPercentage(Double enrichment) {
		this.enrichment = enrichment;
	}

	public List<FoundEntity> getHitExpressions() {
		return hitExpressions;
	}

	public void setHitExpressions(List<FoundEntity> hitExpressions) {
		this.hitExpressions = hitExpressions.stream()
				.filter(Objects::nonNull)
				.distinct()
				.sorted((Comparator.comparing(IdentifierSummary::getId)))
				.collect(Collectors.toList());
		this.totalExpressions = hitExpressions.size();
	}

	public Double getEnrichmentValue() {
		return enrichmentValue;
	}

	public void setEnrichmentValue(Double enrichmentValue) {
		this.enrichmentValue = enrichmentValue;
	}

	private Integer getTotalExpressions() {
		return totalExpressions;
	}

	Area getBackgroundArea() {
		return backgroundArea;
	}

	/**
	 * Calls {@link RenderableDiagramObject#getDiagramObject()}
	 */
	public Node getNode() {
		return getDiagramObject();
	}

	@Override
	public void draw(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramData data, int t) {
		if (isFlag()) flag(canvas, colorProfiles);
		if (isHalo()) halo(canvas, colorProfiles);
		background(canvas, data, colorProfiles);
		double textSplit = analysis(canvas, colorProfiles, data, t);
		text(canvas, colorProfiles, data, textSplit);
		if (isCrossed()) cross(canvas, colorProfiles);
		connectors(canvas, colorProfiles, data);
	}

	void flag(DiagramCanvas canvas, ColorProfiles colorProfiles) {
		canvas.getFlags().add(backgroundShape,
				colorProfiles.getDiagramSheet().getProperties().getFlag(),
				StrokeStyle.FLAG.get(isDashed()));
	}

	void halo(DiagramCanvas canvas, ColorProfiles colorProfiles) {
		canvas.getHalo().add(backgroundShape,
				colorProfiles.getDiagramSheet().getProperties().getHalo(),
				StrokeStyle.HALO.get(isDashed()));
	}

	void background(DiagramCanvas canvas, DiagramData data, ColorProfiles colorProfiles) {
		final Color fill = getFillColor(colorProfiles, data.getAnalysis().getType());
		final Color border = getStrokeColor(colorProfiles, data.getAnalysis().getType());

		if (isFadeOut()) {
			canvas.getFadeOutNodeForeground().add(backgroundArea, fill);
			canvas.getFadeOutNodeBorder().add(backgroundShape, border, StrokeStyle.BORDER.get(isDashed()));
		} else {
			canvas.getNodeBackground().add(backgroundArea, fill);
			canvas.getNodeBorder().add(backgroundShape, border, StrokeStyle.BORDER.get(isDashed()));
		}
	}

	public double analysis(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramData data, int t) {
		if (isFadeOut() || data.getAnalysis().getType() == null) return 0.0;
		switch (data.getAnalysis().getType()) {
			case SPECIES_COMPARISON:
			case OVERREPRESENTATION:
				return enrichment(canvas, colorProfiles);
			case EXPRESSION:
				return expression(canvas, data, colorProfiles, t);
			default:
				return 0.0;
		}
	}

	private double enrichment(DiagramCanvas canvas, ColorProfiles colorProfiles) {
		final Double percentage = getEnrichment();
		final NodeProperties prop = getNode().getProp();
		if (percentage != null && percentage > 0) {
			final Color color = colorProfiles.getAnalysisSheet().getEnrichment().getGradient().getMax();
			final Area enrichmentArea = new Area(backgroundShape);
			final Rectangle2D clip = new Rectangle2D.Double(
					prop.getX(),
					prop.getY(),
					prop.getWidth() * percentage,
					prop.getHeight());
			enrichmentArea.intersect(new Area(clip));
			backgroundArea.subtract(enrichmentArea);
			canvas.getNodeAnalysis().add(enrichmentArea, color);
		}
		return 0.0;
	}

	/**
	 * Adds expression strips for the node in info.
	 *
	 * @return a number, between 0 and 1 indicating where to split the text for
	 * this node. If 0, text will not be modified. If 1, all the text will be
	 * white.
	 */
	double expression(DiagramCanvas canvas, DiagramData data, ColorProfiles colorProfiles, int t) {
		final List<FoundEntity> expressions = getHitExpressions();
		double textSplit = 0.0;
		if (expressions != null) {
			final List<Double> values = expressions.stream()
					.map(participant -> participant.getExp().get(t))
					.collect(Collectors.toList());
			final int size = getTotalExpressions();

			final NodeProperties prop = getNode().getProp();
			final double x = prop.getX();
			final double y = prop.getY();
			final double height = prop.getHeight();
			final double partSize = prop.getWidth() / size;
			textSplit = (double) values.size() / size;

			final double max = data.getAnalysis().getResult().getExpression().getMax();
			final double min = data.getAnalysis().getResult().getExpression().getMin();
			final double delta = 1 / (max - min);  // only one division
			for (int i = 0; i < values.size(); i++) {
				final double val = values.get(i);
				final double value = 1 - (val - min) * delta;
				final GradientSheet gradient = colorProfiles.getAnalysisSheet().getExpression().getGradient();
				final Color color = ColorFactory.interpolate(gradient, value);
				final Rectangle2D rect = new Rectangle2D.Double(
						x + i * partSize, y, partSize, height);
				final Area expressionArea = new Area(rect);
				expressionArea.intersect(new Area(backgroundShape));
				canvas.getNodeAnalysis().add(expressionArea, color);
				backgroundArea.subtract(expressionArea);
			}
		}
		if (this instanceof RenderableEntitySet || this instanceof RenderableComplex)
			return textSplit;
		// report: if proteins have expression values their text should change to white
		return 0.0;
	}

	private void cross(DiagramCanvas canvas, ColorProfiles colorProfiles) {
		final Color color = colorProfiles.getDiagramSheet().getProperties().getDisease();
		final List<Shape> cross = ShapeFactory.cross(getNode().getProp());
		final Stroke stroke = StrokeStyle.BORDER.get(false);
		cross.forEach(line -> canvas.getCross().add(line, color, stroke));
	}

	protected void text(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramData data, double textSplit) {
		final TextLayer layer = isFadeOut()
				? canvas.getFadeOutText()
				: canvas.getText();
		final Color color = getTextColor(colorProfiles, data.getAnalysis().getType());
		layer.add(getNode().getDisplayName(),
				color,
				getNode().getProp(),
				NODE_TEXT_PADDING,
				textSplit,
				FontProperties.DEFAULT_FONT);
	}

	private void connectors(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramData data) {
		if (getNode().getConnectors() == null) return;
		for (Connector connector : getNode().getConnectors()) {
			final boolean fadeOut = connector.getIsFadeOut() != null && connector.getIsFadeOut();
			final RenderableEdge edge = data.getIndex().getEdgesById().get(connector.getEdgeId());
			final Color lineColor = getConnectorColor(colorProfiles, connector, edge);
			final DrawLayer segmentsLayer = fadeOut ? canvas.getFadeOutSegments() : canvas.getSegments();
			final FillDrawLayer shapeLayer = fadeOut ? canvas.getFadeOutEdgeShapes() : canvas.getEdgeShapes();
			final TextLayer shapeTextLayer = fadeOut ? canvas.getFadeOutText() : canvas.getText();
			final Stroke stroke;
			if (fadeOut) stroke = StrokeStyle.SEGMENT.getNormal();
			else if (edge.isSelected()) stroke = StrokeStyle.SELECTION.getNormal();
			else stroke = StrokeStyle.SEGMENT.getNormal();
			for (Segment segment : connector.getSegments()) {
				final Shape line = ShapeFactory.createLine(segment);
				segmentsLayer.add(line, lineColor, stroke);
				if (edge.isSelected() || isSelected())
					canvas.getHalo().add(line, colorProfiles.getDiagramSheet().getProperties().getHalo(), StrokeStyle.HALO.getNormal());
			}
			if (connector.getEndShape() != null) {
				drawShape(connector.getEndShape(), lineColor, connector.getEndShape().getS(), shapeLayer, shapeTextLayer, canvas, colorProfiles, edge);
			}
			if (connector.getStoichiometry() != null && connector.getStoichiometry().getValue() > 1) {
				drawShape(connector.getStoichiometry().getShape(), lineColor, connector.getStoichiometry().getValue().toString(), shapeLayer, shapeTextLayer, canvas, colorProfiles, edge);
			}
		}
	}

	private void drawShape(org.reactome.server.tools.diagram.data.layout.Shape rShape, Color lineColor, String s, FillDrawLayer shapeLayer, TextLayer textLayer, DiagramCanvas canvas, ColorProfiles colorProfiles, RenderableEdge edge) {
		final Shape shape = ShapeFactory.getShape(rShape);
		final Color fillColor = rShape.getEmpty() != null && rShape.getEmpty() ? Color.WHITE : lineColor;
		shapeLayer.add(shape, fillColor, lineColor, StrokeStyle.SEGMENT.get(false));
		if (edge.isSelected() || isSelected())
			canvas.getHalo().add(shape, colorProfiles.getDiagramSheet().getProperties().getHalo(), StrokeStyle.HALO.getNormal());
		if (s != null && !s.isEmpty()) {
			final NodeProperties limits = NodePropertiesFactory.get(
					rShape.getA().getX(), rShape.getA().getY(),
					rShape.getB().getX() - rShape.getA().getX(),
					rShape.getB().getY() - rShape.getA().getY());
			textLayer.add(s, lineColor, limits, 1, 0, FontProperties.DEFAULT_FONT);
		}
	}

	private Color getConnectorColor(ColorProfiles colorProfiles, Connector connector, RenderableEdge edge) {
		if (connector.getIsFadeOut() != null && connector.getIsFadeOut())
			return colorProfiles.getDiagramSheet().getReaction().getFadeOutStroke();
		if (edge.isSelected())
			return colorProfiles.getDiagramSheet().getProperties().getSelection();
		if (connector.getIsDisease() != null && connector.getIsDisease())
			return colorProfiles.getDiagramSheet().getProperties().getDisease();
		return colorProfiles.getDiagramSheet().getReaction().getStroke();
	}

}
