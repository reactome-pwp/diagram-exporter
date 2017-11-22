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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Overlays the legend, the info text and the logo to the diagram.
 */
public class LegendRenderer {


	public static final double RELATIVE_LOGO_WIDTH = 0.1;
	/** space from diagram to legend */
	private static final int LEGEND_TO_DIAGRAM_SPACE = 15;
	private static final int LEGEND_WIDTH = 70;
	private static final int LEGEND_HEIGHT = 350;
	private static final DecimalFormat EXPRESSION_FORMAT = new DecimalFormat("#.##E0", DecimalFormatSymbols.getInstance(Locale.UK));
	private static final DecimalFormat ENRICHMENT_FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.UK));
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
	private NodeProperties bottomTextBox;
	private double logo_height;
	private double logo_width;
	private Rectangle2D.Double colorBar;

	public LegendRenderer(DiagramCanvas canvas, DiagramIndex index, ColorProfiles profiles) {
		this.canvas = canvas;
		this.index = index;
		this.profiles = profiles;
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
		final double textHeight = FontProperties.DEFAULT_FONT.getSize() * 2;
		final double centerY = bounds.getMinY() + bounds.getHeight() * 0.5;
		double colorBarX = bounds.getMaxX() + LEGEND_TO_DIAGRAM_SPACE + BACKGROUND_PADDING;
		double colorBarWidth = (legend_width - 2 * BACKGROUND_PADDING);
		double colorBarHeight = legend_height - 2 * (BACKGROUND_PADDING + textHeight);
		double colorBarY = centerY - 0.5 * colorBarHeight;
		colorBar = new Rectangle2D.Double(colorBarX, colorBarY, colorBarWidth, colorBarHeight);

		addColorBar();
		addBackground(textHeight);
		addLabels(textHeight);
	}

	private void createBottomTextBox() {
		final Rectangle2D bounds = canvas.getBounds();
		final double x = bounds.getMinX();
		final double y = bounds.getMaxY() - logo_height;
		final double width = bounds.getWidth() - logo_width;
		final double height = logo_height;
		bottomTextBox = NodePropertiesFactory.get(x, y, width, height);
	}

	public void setCol(int col) {
		clearTicks();
		ticks(col);
		infoText(col);
	}

	private void clearTicks() {
		canvas.getLegendTicks().clear();
		canvas.getLegendTickArrows().clear();
	}

	private void infoText(int col) {
		if (bottomTextBox == null)
			createBottomTextBox();
		final String prefix = index.getAnalysisName() == null
				? ""
				: String.format("[%s] ", index.getAnalysisName());
		final String info = String.format(Locale.UK, "%d/%d: %s", (col + 1),
				index.getExpressionColumnNames().size(),
				index.getExpressionColumnNames().get(col));
		canvas.getLegendBottomText().clear();
		canvas.getLegendBottomText().add(prefix + info, Color.BLACK, bottomTextBox, 0, 0, FontProperties.LEGEND_FONT);
	}

	public void infoText() {
		if (index.getAnalysisName() == null)
			return;
		if (bottomTextBox == null)
			createBottomTextBox();
		canvas.getLegendBottomText().clear();
		canvas.getLegendBottomText().add(index.getAnalysisName(), Color.BLACK, bottomTextBox, 0, 0, FontProperties.LEGEND_FONT);
	}

	private void addBackground(double textHeight) {
		double textSpace = textHeight + TEXT_PADDING + BACKGROUND_PADDING;
		final Shape background = new RoundRectangle2D.Double(
				colorBar.getX() - BACKGROUND_PADDING,
				colorBar.getY() - textSpace,
				colorBar.getWidth() + 2 * BACKGROUND_PADDING,
				colorBar.getHeight() + 2 * textSpace,
				20, 20);
		final Stroke stroke = StrokeProperties.StrokeStyle.SEGMENT.getStroke(false);
		canvas.getLegendBackground().add(BACKGROUND_FILL, BACKGROUND_BORDER, stroke, background);
	}

	private void addColorBar() {
		final GradientSheet gradient;
		if (index.getAnalysisType() == AnalysisType.EXPRESSION) {
			gradient = profiles.getAnalysisSheet().getExpression().getGradient();
		} else
			gradient = profiles.getAnalysisSheet().getEnrichment().getGradient();

		final Paint paint;
		if (gradient.getStop() == null)
			paint = new GradientPaint(
					(float) colorBar.getX(), (float) colorBar.getMaxY(), gradient.getMax(),
					(float) colorBar.getX(), (float) colorBar.getY(), gradient.getMin());
		else {
			paint = new LinearGradientPaint(
					(float) colorBar.getX(), (float) colorBar.getMaxY(),
					(float) colorBar.getX(), (float) colorBar.getY(),
					new float[]{0, 0.5f, 1},
					new Color[]{gradient.getMax(),
							gradient.getStop(),
							gradient.getMin()});
		}
		canvas.getLegendBar().add(paint, colorBar);
	}

	private void ticks(int col) {
		if (index.getSelected() == null) return;
		final List<FoundEntity> expressions = index.getSelected().getHitExpressions();
		if (expressions == null || expressions.isEmpty()) return;
		// Calculate which ticks to draw: (min, median, max) or (value)
		Double nMax;
		Double nMin;
		Double nValue;
		final List<Double> values = expressions.stream()
				.map(value -> value.getExp().get(col))
				.collect(Collectors.toList());
		if (values.size() == 1) {
			nValue = values.get(0);
			nMax = nMin = null;
		} else {
			Collections.sort(values);
			nMin = values.get(0);
			nMax = values.get(values.size() - 1);
			nValue = getMedian(values);
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

	/**
	 * Gets the median value of an already sorted collection. If the number of
	 * elements in the list is odd, then the value in the middle is returned. If
	 * it is odd, then the mean of the two values sharing the centre is
	 * returned.
	 */
	private double getMedian(List<Double> values) {
		final int midPoint = values.size() / 2;
		if (values.size() % 2 == 0)
			return (values.get(midPoint) + values.get(midPoint - 1)) * 0.5;
		else return values.get(midPoint);
	}

	private void drawTick(Double value, Stroke stroke, Color limitColor) {
		if (value == null) return;
		// Interpolate value in expression range
		final double val = (value - index.getMinExpression()) / (index.getMaxExpression() - index.getMinExpression());
		// Extrapolate to colorBar height
		// In expression analysis, min is in the bottom
		final double y1 = colorBar.getMaxY() - colorBar.getHeight() * val;
		final Shape line = new Line2D.Double(colorBar.getX(), y1, colorBar.getMaxX(), y1);
		canvas.getLegendTicks().add(limitColor, stroke, line);
		// Notice the -1. It puts the arrow over the line
		final Shape arrow = arrow(colorBar.getMaxX() - 1, y1);
		canvas.getLegendTickArrows().add(limitColor, arrow);
	}

	private Shape arrow(double x, double y) {
		final Path2D arrow = new Path2D.Double();
		arrow.moveTo(x, y);
		arrow.lineTo(x + ARROW_SIZE, y + ARROW_SIZE);
		arrow.lineTo(x + ARROW_SIZE, y - ARROW_SIZE);
		arrow.closePath();
		return arrow;
	}

	private void addLabels(double textHeight) {
		// We create a box to get the label centered
		float textX = (float) (colorBar.getX() - BACKGROUND_PADDING);
		float textWidth = (float) (colorBar.getWidth() + 2 * BACKGROUND_PADDING);
		final NodeProperties top = NodePropertiesFactory.get(textX,
				colorBar.getY() - textHeight - TEXT_PADDING,
				textWidth, textHeight);
		final NodeProperties bottom = NodePropertiesFactory.get(textX,
				(float) colorBar.getMaxY() + TEXT_PADDING,
				textWidth, textHeight);

		final DecimalFormat formatter = this.index.getAnalysisType() == AnalysisType.EXPRESSION
				? EXPRESSION_FORMAT
				: ENRICHMENT_FORMAT;
		final String topText = formatter.format(index.getMaxExpression());
		final String bottomText = formatter.format(index.getMinExpression());

		canvas.getLegendText().add(topText, Color.BLACK, top, 0, 0, FontProperties.DEFAULT_FONT);
		canvas.getLegendText().add(bottomText, Color.BLACK, bottom, 0, 0, FontProperties.DEFAULT_FONT);
	}

	/**
	 * Adds a logo in the bottom right corner of the canvas.
	 */
	public void addLogo() {
		try {
			final Rectangle2D bounds = canvas.getBounds();
			final BufferedImage logo = getLogo();
			logo_width = bounds.getWidth() * RELATIVE_LOGO_WIDTH;
			if (logo_width > logo.getWidth()) logo_width = logo.getWidth();
			logo_height = logo_width / logo.getWidth() * logo.getHeight();

			final NodeProperties limits = NodePropertiesFactory.get(
					bounds.getMaxX() - logo_width,
					bounds.getMaxY() + LEGEND_TO_DIAGRAM_SPACE,
					logo_width, logo_height);
			this.canvas.getLogoLayer().add(logo, limits);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Returns the first logo that has a width greater than logo_width */
	private BufferedImage getLogo() throws IOException {
		final String filename = "Reactome_Imagotype_Positive_100mm.png";
		final InputStream resource = getClass().getResourceAsStream(filename);
		return ImageIO.read(resource);
	}
}
