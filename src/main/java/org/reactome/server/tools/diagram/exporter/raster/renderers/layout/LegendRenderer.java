package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.FoundEntity;
import org.reactome.server.tools.diagram.exporter.raster.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.color.ExpressionSheet;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
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

	private static final DecimalFormat LEGEND_FORMAT = new DecimalFormat("#.##E0", DecimalFormatSymbols.getInstance(Locale.UK));
	private static final double TEXT_PADDING = 2;
	private static final double BACKGROUND_PADDING = 10;
	private static final int ARROW_SIZE = 5;

	public static void addLegend(DiagramCanvas canvas, ColorProfiles colorProfiles,
	                             Diagram diagram, double margin,
	                             double legend_width, double legend_height,
	                             DiagramIndex index, int col) {
		/*
		 * Legend is disposed in 4 layers:
		 * 1. a FillDrawLayer for the background
		 * 2. a FillLayer for the gradient color bar
		 * 3. a DrawLayer fot the ticks
		 * 4. a TextLayer for the numbers
		 * Everything must fit the legend_height x legend_width
		 */
		final ExpressionSheet sheet = colorProfiles.getAnalysisSheet().getExpression();
		final int textHeight = FontProperties.DEFAULT_FONT.getSize() * 2;
		final double x = (diagram.getMaxX() + margin + 5);
		final double textSpace = TEXT_PADDING + textHeight;
		final double width = (legend_width - 20);
		final double centerY = diagram.getMinY() + (diagram.getMaxY() - diagram.getMinY()) * 0.5;
		final double height = legend_height - 3 * (textSpace);
		final double y = centerY - 0.5 * height;
		background(canvas, x, y, width, height, textSpace);
		colorbar(canvas, sheet, x, width, height, y);
		ticks(canvas, index, colorProfiles, x, y, width, height, col);
		text(canvas, index, textHeight, x, y, width, height, col);

	}

	private static void background(DiagramCanvas canvas, double x, double y, double width, double height, double textSpace) {
		// 1 grey background
		final Shape background = new RoundRectangle2D.Double(
				x - BACKGROUND_PADDING,
				y - (textSpace + BACKGROUND_PADDING),
				width + 2 * BACKGROUND_PADDING,
				height + 3 * textSpace + 2 * BACKGROUND_PADDING,
				20, 20);
		final Color border = new Color(175, 175, 175);
		final Color bg = new Color(220, 220, 220);
		final Stroke stroke = StrokeProperties.StrokeStyle.SEGMENT.getStroke(false);
		canvas.getLegendBackground().add(bg, border, stroke, background);
	}

	private static void colorbar(DiagramCanvas canvas, ExpressionSheet sheet, double x, double width, double height, double y) {
		// 2 color bar
		float baseY = (float) (y + height);
		final Paint paint;
		if (sheet.getGradient().getStop() == null)
			paint = new GradientPaint(
					(float) x, baseY, sheet.getGradient().getMax(),
					(float) x, (float) y, sheet.getGradient().getMin());
		else {
			paint = new LinearGradientPaint(
					(float) x, baseY,
					(float) x, (float) y,
					new float[]{0, 0.5f, 1},
					new Color[]{sheet.getGradient().getMax(),
							sheet.getGradient().getStop(),
							sheet.getGradient().getMin()});
		}
		canvas.getLegendBar().add(paint, new Rectangle2D.Double(x, y, width, height));
	}

	private static void ticks(DiagramCanvas canvas, DiagramIndex index, ColorProfiles colors, double x, double y, double width, double height, int col) {
		if (index.getSelected() == null) return;
		final List<FoundEntity> expressions = index.getSelected().getExpressions();
		if (expressions == null) return;
		// Calculate [min - median - max] | [value]
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
		// Draw ticks
		final Stroke stroke = StrokeProperties.StrokeStyle.SEGMENT.getStroke(false);
		final Color limitColor, valueColor;
		if (nMax != null) {
			limitColor = colors.getDiagramSheet().getProperties().getSelection();
			valueColor = colors.getAnalysisSheet().getExpression().getLegend().getMedian();
		} else {
			limitColor = null;
			valueColor = colors.getDiagramSheet().getProperties().getSelection();
		}
		drawTick(canvas, index, x, y, width, height, nValue, stroke, valueColor);
		drawTick(canvas, index, x, y, width, height, nMax, stroke, limitColor);
		drawTick(canvas, index, x, y, width, height, nMin, stroke, limitColor);
	}

	private static void drawTick(DiagramCanvas canvas, DiagramIndex index, double x, double y, double width, double height, Double value, Stroke stroke, Color limitColor) {
		if (value != null) {
			final double rightX = x + width;
			final double y1 = getY1(index.getMinExpression(), index.getMaxExpression(), y, height, value);
			final Shape line = new Line2D.Double(x, y1, rightX, y1);
			canvas.getLegendTicks().add(limitColor, stroke, line);
			final Shape arrow = arrow(rightX - 1, y1);
			canvas.getLegendTickArrows().add(limitColor, arrow);
		}
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

	private static void text(DiagramCanvas canvas, DiagramIndex index, int textHeight, double x, double y, double width, double height, int col) {
		final float b = (float) (y + height);
		// 4 text
		// There is not TextCenteredLayer, so we use a regular TextLayer
		// and use boxes to center the texts
		final NodeProperties top = NodePropertiesFactory.get(x - 10,
				y - textHeight - TEXT_PADDING, width + 20, textHeight);
		final NodeProperties bottom = NodePropertiesFactory.get(x - 10,
				b + TEXT_PADDING, width + 20, textHeight);
		final double max = index.getMaxExpression();
		final double min = index.getMinExpression();
		canvas.getLegendText().add(Color.BLACK, LEGEND_FORMAT.format(max), top, 0, 0);
		canvas.getLegendText().add(Color.BLACK, LEGEND_FORMAT.format(min), bottom, 0, 0);
		final NodeProperties progress = NodePropertiesFactory.get(x - 10,
				b + TEXT_PADDING + textHeight, width + 20, textHeight);
		canvas.getLegendText().add(Color.BLACK, String.format("%d/%d", col + 1, index.getExpressionSize()), progress, 0,0);
	}
}
