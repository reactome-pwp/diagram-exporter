package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.FoundEntity;
import org.reactome.server.tools.diagram.exporter.raster.diagram.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.GradientSheet;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.StrokeProperties;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class LegendRenderer {

	/** space from diagram to legend */
	private static final int LEGEND_TO_DIAGRAM_SPACE = 15;
	private static final int LEGEND_WIDTH = 70;
	private static final int LEGEND_HEIGHT = 350;
	private static final DecimalFormat LEGEND_FORMAT = new DecimalFormat("#.##E0", DecimalFormatSymbols.getInstance(Locale.UK));
	/** value to create ticks arrows */
	private static final int ARROW_SIZE = 5;
	/** space between texts and color bar */
	private static final double TEXT_PADDING = 2;
	/** space between background and color bar or text, what before */
	private static final double BACKGROUND_PADDING = 10;
	private static final Color BACKGROUND_BORDER = new Color(175, 175, 175);
	private static final Color BACKGROUND_FILL = new Color(220, 220, 220);

	private final DiagramCanvas canvas;
	private final DiagramIndex index;
	private final ColorProfiles profiles;
	private double colorBarX;
	private double colorBarY;
	private double colorBarHeight;
	private double colorBarWidth;
	private NodeProperties bottomTextBox;

	public LegendRenderer(DiagramCanvas canvas, DiagramIndex index, ColorProfiles profiles) {
		this.canvas = canvas;
		this.index = index;
		this.profiles = profiles;
	}

	private static Shape arrow(double x, double y) {
		final Path2D arrow = new Path2D.Double();
		arrow.moveTo(x, y);
		arrow.lineTo(x + ARROW_SIZE, y + ARROW_SIZE);
		arrow.lineTo(x + ARROW_SIZE, y - ARROW_SIZE);
		arrow.closePath();
		return arrow;
	}

	private static double getY1(double min, double max, double y, double height, double val) {
		//  y1 - y     max - val
		// -------- = -----------
		//  height     max - min
		return (max - val) / (max - min) * (height) + y;
	}

	public void addLegend() {
		/*
		 * Legend is disposed in 4 layers:
		 * 1. a FillDrawLayer for the background
		 * 2. a FillLayer for the gradient color bar
		 * 3. a DrawLayer fot the ticks
		 * 4. a TextLayer for the numbers
		 */
		final Rectangle2D bounds = canvas.getBounds();

		double legend_width;
		double legend_height;
		if (bounds.getHeight() < LEGEND_HEIGHT) {
			// If diagram is too short, we use all the height available
			legend_height = bounds.getHeight();
			legend_width = LEGEND_WIDTH * legend_height / LEGEND_HEIGHT;
		} else {
			legend_width = LEGEND_WIDTH;
			legend_height = LEGEND_HEIGHT;
		}

		final int textHeight = FontProperties.DEFAULT_FONT.getSize() * 2;
		final double textSpace = TEXT_PADDING + textHeight;
		double total_width = bounds.getWidth() + legend_width;
		bottomTextBox = NodePropertiesFactory.get(bounds.getMinX(), bounds.getMaxY(), total_width, textHeight);

		final double centerY = bounds.getMinY() + bounds.getHeight() * 0.5;

		colorBarX = bounds.getMaxX() + LEGEND_TO_DIAGRAM_SPACE + BACKGROUND_PADDING;
		colorBarWidth = (legend_width - 2 * BACKGROUND_PADDING);
		colorBarHeight = legend_height - 2 * (BACKGROUND_PADDING + textSpace);
		colorBarY = centerY - 0.5 * colorBarHeight;

		colorbar();
		background(textSpace);
		text(textSpace);
		bottomText(0);
	}

	public void setCol(int col) {
		clearTicks();
		ticks(col);
		bottomText(col);

	}

	private void clearTicks() {
		canvas.getLegendTicks().clear();
		canvas.getLegendTickArrows().clear();
	}

	private void bottomText(int col) {
		final String text = String.format(Locale.UK, "%d/%d: %s", (col + 1),
				index.getExpressionColumns().size(),
				index.getExpressionColumns().get(col));
		canvas.getLegendBottomText().clear();
		canvas.getLegendBottomText().add(Color.BLACK, text, bottomTextBox, 0, 0);

	}

	private void background(double textSpace) {
		final Shape background = new RoundRectangle2D.Double(
				colorBarX - BACKGROUND_PADDING,
				colorBarY - (textSpace + BACKGROUND_PADDING),
				colorBarWidth + 2 * BACKGROUND_PADDING,
				colorBarHeight + 2 * textSpace + 2 * BACKGROUND_PADDING,
				20, 20);
		final Stroke stroke = StrokeProperties.StrokeStyle.SEGMENT.getStroke(false);
		canvas.getLegendBackground().add(BACKGROUND_FILL, BACKGROUND_BORDER, stroke, background);
	}

	private void colorbar() {
		final GradientSheet gradient;
		if (index.getAnalysisType() == AnalysisType.EXPRESSION) {
			gradient = profiles.getAnalysisSheet().getExpression().getGradient();
		} else
			gradient = profiles.getAnalysisSheet().getEnrichment().getGradient();

		float baseY = (float) (colorBarY + colorBarHeight);
		final Paint paint;
		if (gradient.getStop() == null)
			paint = new GradientPaint(
					(float) colorBarX, baseY, gradient.getMax(),
					(float) colorBarX, (float) colorBarY, gradient.getMin());
		else {
			paint = new LinearGradientPaint(
					(float) colorBarX, baseY,
					(float) colorBarX, (float) colorBarY,
					new float[]{0, 0.5f, 1},
					new Color[]{gradient.getMax(),
							gradient.getStop(),
							gradient.getMin()});
		}
		canvas.getLegendBar().add(paint, new Rectangle2D.Double(colorBarX, colorBarY, colorBarWidth, colorBarHeight));
	}

	private void ticks(int col) {
		if (index.getSelected() == null) return;
		final List<FoundEntity> expressions = index.getSelected().getHitExpressions();
		if (expressions == null) return;
		// Calculate which ticks to draw: (min, median, max) or (value)
		Double nMax;
		Double nMin;
		Double nValue;
		final List<Double> values = expressions.stream()
				.filter(Objects::nonNull)
				.map(value -> value.getExp().get(col))
				.collect(Collectors.toList());
		if (values.size() == 1) {
			nValue = values.get(0);
			nMax = nMin = null;
		} else {
			Collections.sort(values);
			nMin = values.get(0);
			nMax = values.get(values.size() - 1);
			final int half = values.size() / 2;
			if (values.size() % 2 == 0)
				nValue = (values.get(half) + values.get(half - 1)) * 0.5;
			else nValue = values.get(half);
		}

		final Stroke stroke = StrokeProperties.StrokeStyle.SEGMENT.getStroke(false);
		final Color limitColor, valueColor;
		if (nMax == null) {
			limitColor = null;
			valueColor = profiles.getDiagramSheet().getProperties().getSelection();
		} else {
			limitColor = profiles.getDiagramSheet().getProperties().getSelection();
			valueColor = profiles.getAnalysisSheet().getExpression().getLegend().getMedian();
		}
		drawTick(nValue, stroke, valueColor);
		drawTick(nMax, stroke, limitColor);
		drawTick(nMin, stroke, limitColor);
	}

	private void drawTick(Double value, Stroke stroke, Color limitColor) {
		if (value != null) {
			final double rightX = colorBarX + colorBarWidth;
			final double y1 = getY1(index.getMinExpression(), index.getMaxExpression(), colorBarY, colorBarHeight, value);
			final Shape line = new Line2D.Double(colorBarX, y1, rightX, y1);
			canvas.getLegendTicks().add(limitColor, stroke, line);
			final Shape arrow = arrow(rightX - 1, y1);
			canvas.getLegendTickArrows().add(limitColor, arrow);
		}
	}

	private void text(double textSpace) {
		final float b = (float) (colorBarY + colorBarHeight);
		// There is not TextCenteredLayer, so we use a regular TextLayer
		// and use boxes to center the texts
		final NodeProperties top = NodePropertiesFactory.get(colorBarX - 10,
				colorBarY - textSpace - TEXT_PADDING, colorBarWidth + 20, textSpace);
		final NodeProperties bottom = NodePropertiesFactory.get(colorBarX - 10,
				b + TEXT_PADDING, colorBarWidth + 20, textSpace);
		final double topValue;
		final double bottomValue;
		topValue = index.getMaxExpression();
		bottomValue = index.getMinExpression();
		canvas.getLegendText().add(Color.BLACK, LEGEND_FORMAT.format(topValue), top, 0, 0);
		canvas.getLegendText().add(Color.BLACK, LEGEND_FORMAT.format(bottomValue), bottom, 0, 0);
	}
}
