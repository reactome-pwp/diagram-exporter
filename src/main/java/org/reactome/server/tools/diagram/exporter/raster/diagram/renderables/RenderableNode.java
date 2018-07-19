package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.analysis.core.result.model.FoundEntity;
import org.reactome.server.analysis.core.result.model.IdentifierSummary;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.ShapeFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.StrokeStyle;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
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
	private Double expressionValue;


	RenderableNode(Node node) {
		super(node);
		this.backgroundShape = backgroundShape();
		this.backgroundArea = new Area(backgroundShape());
	}

	abstract Shape backgroundShape();

	public Double getEnrichment() {
		return enrichment;
	}

	public void setEnrichment(Double enrichment) {
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

	public Double getExpressionValue() {
		return expressionValue;
	}

	public void setExpressionValue(Double expressionValue) {
		this.expressionValue = expressionValue;
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
	public void draw(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		if (isFlag()) flag(canvas, colorProfiles);
		if (isHalo()) halo(canvas, colorProfiles);
		background(canvas, index, colorProfiles);
		double textSplit = analysis(canvas, colorProfiles, index, t);
		text(canvas, colorProfiles, index, textSplit);
		if (isCrossed()) cross(canvas, colorProfiles);
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

	void background(DiagramCanvas canvas, DiagramIndex index, ColorProfiles colorProfiles) {
		final Color fill = getFillColor(colorProfiles, index.getAnalysis().getType());
		final Color border = getStrokeColor(colorProfiles, index.getAnalysis().getType());

		if (isFadeOut()) {
			canvas.getFadeOutNodeForeground().add(backgroundArea, fill);
			canvas.getFadeOutNodeBorder().add(backgroundShape, border, StrokeStyle.BORDER.get(isDashed()));
		} else {
			canvas.getNodeBackground().add(backgroundArea, fill);
			canvas.getNodeBorder().add(backgroundShape, border, StrokeStyle.BORDER.get(isDashed()));
		}
	}

	public double analysis(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		if (isFadeOut() || index.getAnalysis().getType() == null) return 0.0;
		switch (index.getAnalysis().getType()) {
			case SPECIES_COMPARISON:
			case OVERREPRESENTATION:
				return enrichment(canvas, colorProfiles);
			case EXPRESSION:
				return expression(canvas, index, colorProfiles, t);
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
	double expression(DiagramCanvas canvas, DiagramIndex index, ColorProfiles colorProfiles, int t) {
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

			final double max = index.getAnalysis().getResult().getExpression().getMax();
			final double min = index.getAnalysis().getResult().getExpression().getMin();
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

	protected void text(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, double textSplit) {
		final TextLayer layer = isFadeOut()
				? canvas.getFadeOutText()
				: canvas.getText();
		final Color color = getTextColor(colorProfiles, index.getAnalysis().getType());
		layer.add(getNode().getDisplayName(),
				color,
				getNode().getProp(),
				NODE_TEXT_PADDING,
				textSplit,
				FontProperties.DEFAULT_FONT);
	}

}
