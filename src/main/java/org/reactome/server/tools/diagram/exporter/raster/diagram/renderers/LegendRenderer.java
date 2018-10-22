package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers;

import org.apache.commons.io.IOUtils;
import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.analysis.core.result.model.FoundEntity;
import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramAnalysis;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.StrokeStyle;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableCompartment;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableNode;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.GradientSheet;
import org.reactome.server.tools.diagram.exporter.raster.resources.Resources;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.*;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Overlays the legend, the info text and the logo to the diagram.
 */
public class LegendRenderer {

	/**
	 * For measuring text width
	 */
	private static final FontMetrics FONT_METRICS = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
			.createGraphics().getFontMetrics(FontProperties.DEFAULT_FONT);

	private static final double RELATIVE_LOGO_WIDTH = 0.1;
	/**
	 * space from diagram to legend
	 */
	private static final int LEGEND_TO_DIAGRAM_SPACE = 15;
	private static final int LEGEND_WIDTH = 70;
	private static final int LEGEND_HEIGHT = 350;
	private static final DecimalFormat EXPRESSION_FORMAT = new DecimalFormat("#.##E0", DecimalFormatSymbols.getInstance(Locale.UK));
	private static final DecimalFormat ENRICHMENT_FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.UK));
	/**
	 * value to create ticks arrows
	 */
	private static final int ARROW_SIZE = 5;
	/**
	 * space between texts and color bar
	 */
	private static final double TEXT_PADDING = 2;
	/**
	 * space between background and color bar or text, what before
	 */
	private static final double BACKGROUND_PADDING = 10;
	private static final double LOGO_PADDING = 15;
	private static final Color BACKGROUND_BORDER = new Color(175, 175, 175);
	private static final Color BACKGROUND_FILL = new Color(220, 220, 220);
	private static final double MIN_LOGO_WIDTH = 50;
	private static final Map<Long, String> SPECIES = new TreeMap<>();

	// FIXME: hardcoded the species name because it is not reported in AnalysisResult
	static {
		final InputStream resource = Resources.class.getResourceAsStream("texts/species.txt");
		try {
			IOUtils.readLines(resource, Charset.defaultCharset())
					.forEach(s -> {
						final String[] row = s.split("\t");
						SPECIES.put(Long.valueOf(row[0]), row[1]);
					});
		} catch (IOException e) {
			// Shouldn't happen
			e.printStackTrace();
		}
	}

	private final DiagramCanvas canvas;
	private final DiagramIndex index;
	private final ColorProfiles profiles;
	private NodeProperties bottomTextBox;
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

	public void setCol(int col, String title) {
		clearTicks();
		ticks(col);
		infoText(col, title);
	}

	private void clearTicks() {
		canvas.getLegendTicks().clear();
		canvas.getLegendTickArrows().clear();
	}

	private void infoText(int col, String title) {
		if (title == null) return;
		// title [analysis name] 1/5 sample
		// [analysis name] 1/5 sample
		String text = String.format("%s [%s] %d/%d %s",
				title,
				index.getAnalysis().getAnalysisName(),
				(col + 1),
				index.getAnalysis().getResult().getExpression().getColumnNames().size(),
				index.getAnalysis().getResult().getExpression().getColumnNames().get(col));
		canvas.getLegendBottomText().clear();
		canvas.getLegendBottomText().add(text, Color.BLACK, bottomTextBox, 0, 0, FontProperties.LEGEND_FONT);
	}

	/**
	 * Adds a text in the bottom of the image with the name of the analysis.
	 */
	public void infoText(String title) {
		if (title == null) return;
		String text = title;
		if (index.getAnalysis().getType() == AnalysisType.OVERREPRESENTATION) {
			// title: analysis name
			if (index.getAnalysis().getAnalysisName() != null)
				text += ": " + index.getAnalysis().getAnalysisName();
		} else if (index.getAnalysis().getType() == AnalysisType.SPECIES_COMPARISON) {
			// title: species name
			text += ": " + SPECIES.get(index.getAnalysis().getResult().getSummary().getSpecies());
		}
		if (text.trim().isEmpty()) return;
		canvas.getLegendBottomText().clear();
		canvas.getLegendBottomText().add(text, Color.BLACK, bottomTextBox, 0, 0, FontProperties.LEGEND_FONT);

	}

	private void addBackground(double textHeight) {
		double textSpace = textHeight + TEXT_PADDING + BACKGROUND_PADDING;
		final Shape background = new RoundRectangle2D.Double(
				colorBar.getX() - BACKGROUND_PADDING,
				colorBar.getY() - textSpace,
				colorBar.getWidth() + 2 * BACKGROUND_PADDING,
				colorBar.getHeight() + 2 * textSpace,
				20, 20);
		final Stroke stroke = StrokeStyle.SEGMENT.get(false);
		canvas.getLegendBackground().add(background, BACKGROUND_FILL, BACKGROUND_BORDER, stroke);
	}

	private void addColorBar() {
		final GradientSheet gradient;
		gradient = index.getAnalysis().getType() == AnalysisType.EXPRESSION
				? profiles.getAnalysisSheet().getExpression().getGradient()
				: profiles.getAnalysisSheet().getEnrichment().getGradient();

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
		canvas.getLegendBar().add(colorBar, paint);
	}

	private void ticks(int col) {
		if (index.getDecorator().getSelected() == null) return;
		for (Long id : index.getDecorator().getSelected()) {
			final RenderableNode node = (RenderableNode) index.getDiagramObjectsById().get(id);
			// ProcessNode
			if (node.getEnrichment() != null
					&& node.getEnrichment() > 0
					&& node.getExpressionValue() != null) {
				double value = node.getExpressionValue();
				drawTick(value, StrokeStyle.SEGMENT.get(false), profiles.getDiagramSheet().getProperties().getSelection());
			} else {
				// The rest of the world
				final List<FoundEntity> expressions = node.getHitExpressions();
				if (expressions == null || expressions.isEmpty()) continue;
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

				final Stroke stroke = StrokeStyle.SEGMENT.get(false);
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
		}
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
		final double min = index.getAnalysis().getResult().getExpression().getMin();
		final double max = index.getAnalysis().getResult().getExpression().getMax();
		final double val = (value - min) / (max - min);
		// Extrapolate to colorBar height
		// In expression analysis, min is in the bottom
		final double y1 = colorBar.getMaxY() - colorBar.getHeight() * val;
		final Shape line = new Line2D.Double(colorBar.getX(), y1, colorBar.getMaxX(), y1);
		canvas.getLegendTicks().add(line, limitColor, stroke);
		// Notice the -1. It puts the arrow over the line
		final Shape arrow = arrow(colorBar.getMaxX() - 1, y1);
		canvas.getLegendTickArrows().add(arrow, limitColor);
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

		final String topText;
		final String bottomText;
		if (index.getAnalysis().getType() == AnalysisType.EXPRESSION) {
			topText = EXPRESSION_FORMAT.format(index.getAnalysis().getResult().getExpression().getMax());
			bottomText = EXPRESSION_FORMAT.format(index.getAnalysis().getResult().getExpression().getMin());
		} else {
			topText = ENRICHMENT_FORMAT.format(0);
			bottomText = ENRICHMENT_FORMAT.format(DiagramAnalysis.MIN_ENRICHMENT);
		}
		canvas.getLegendText().add(topText, Color.BLACK, top, 0, 0, FontProperties.DEFAULT_FONT);
		canvas.getLegendText().add(bottomText, Color.BLACK, bottom, 0, 0, FontProperties.DEFAULT_FONT);
	}

	/**
	 * Adds a logo in the bottom right corner of the canvas.
	 *
	 * @param args the exporter arguments
	 */
	public void addLogo(RasterArgs args, Diagram diagram) {
		final Rectangle2D bounds = canvas.getBounds();
		final BufferedImage logo = getLogo();
		double logoWidth = bounds.getWidth() * RELATIVE_LOGO_WIDTH;
		if (logoWidth > logo.getWidth()) logoWidth = logo.getWidth();
		if (logoWidth < MIN_LOGO_WIDTH) logoWidth = MIN_LOGO_WIDTH;
		final double logoHeight = logoWidth / logo.getWidth() * logo.getHeight();
		if (!args.getWriteTitle()) {
			final NodeProperties limits = findLogoPlace(bounds, diagram, logoWidth, logoHeight);
			if (limits != null) {
				canvas.getLogoLayer().add(logo, limits);
				return;
			}
		}
		// This point will be reached only if logo couldn't be written inside the diagram or if writeTitle is true
		final NodeProperties limits = NodePropertiesFactory.get(
				bounds.getMaxX() - logoWidth,
				bounds.getMaxY() + LEGEND_TO_DIAGRAM_SPACE,
				logoWidth, logoHeight);
		this.canvas.getLogoLayer().add(logo, limits);
		// Now we can reserve the rest of space for the text
		createBottomTextBox(logoWidth, logoHeight);
	}

	private NodeProperties findLogoPlace(Rectangle2D bounds, Diagram diagram, double width, double height) {
		// Let's find a nice place inside the diagram
		// +----------+
		// |  4  6  3 |
		// |  8  9  7 |
		// |  2  5  1 |
		// +----------+
		final int[] hits = new int[9];
		Arrays.fill(hits, 0);
		final double area = width * height;
		final double hh = 0.5 * height;
		final double hw = 0.5 * width;
		final double x = bounds.getX() + LOGO_PADDING;
		final double y = bounds.getY() + LOGO_PADDING;
		final double mx = bounds.getMaxX() - LOGO_PADDING - width;
		final double my = bounds.getMaxY() - LOGO_PADDING - height;
		final double cx = bounds.getCenterX() - hw;
		final double cy = bounds.getCenterY() - hh;
		final Rectangle2D[] positions = new Rectangle2D[9];
		positions[0] = new Rectangle2D.Double(mx, my, width, height);
		positions[1] = new Rectangle2D.Double(x, my, width, height);
		positions[2] = new Rectangle2D.Double(mx, y, width, height);
		positions[3] = new Rectangle2D.Double(x, y, width, height);
		positions[4] = new Rectangle2D.Double(cx, my, width, height);
		positions[5] = new Rectangle2D.Double(cx, y, width, height);
		positions[6] = new Rectangle2D.Double(mx, cy, width, height);
		positions[7] = new Rectangle2D.Double(x, cy, width, height);
		positions[8] = new Rectangle2D.Double(mx, cy, width, height);
		// Nodes
		for (Node node : diagram.getNodes()) {
			for (int i = 0; i < positions.length; i++) {
				if (positions[i].intersects(toRectangle(node.getProp()))) hits[i]++;
			}
		}
		// Edges
		for (Edge edge : diagram.getEdges()) {
			final double w = edge.getMaxX() - edge.getMinX();
			final double h = edge.getMaxY() - edge.getMinY();
			for (int i = 0; i < positions.length; i++) {
				if (positions[i].intersects(edge.getMinX(), edge.getMinY(), w, h)) hits[i]++;
			}
		}
		// Compartments
		for (Compartment compartment : diagram.getCompartments()) {
			// If comp has inner, then logo can lay on membrane
			if (compartment.getInsets() != null) {
				final Rectangle2D.Double inner = toRectangle(compartment.getInsets());
//				final Rectangle2D.Double outer = toRectangle(compartment.getProp());
				for (int i = 0; i < positions.length; i++) {
					final Rectangle2D intersection = positions[i].createIntersection(inner);

					final double intersectionArea = intersection.getWidth() * intersection.getHeight();
					if (intersectionArea < area && intersectionArea > 0) hits[i]++;
//					if (positions[i].intersects(outer) && positions[i].intersects(inner)) hits[i]++;
				}
			}
			// And we also check for text
			final double tw = FONT_METRICS.stringWidth(compartment.getDisplayName());
			final double th = FONT_METRICS.getHeight();
			final double tx = compartment.getTextPosition().getX() + RenderableCompartment.GWU_CORRECTION.getX();
			final double ty = compartment.getTextPosition().getY() + RenderableCompartment.GWU_CORRECTION.getY();
			for (int i = 0; i < positions.length; i++) {
				if (positions[i].intersects(tx, ty, tw, th)) hits[i]++;
			}
		}

		for (int i = 0; i < positions.length; i++)
			if (hits[i] == 0)
				return NodePropertiesFactory.get(positions[i].getX(), positions[i].getY(), width, height);
		return null;
	}

	private Rectangle2D.Double toRectangle(NodeProperties properties) {
		return new Rectangle2D.Double(properties.getX(), properties.getY(), properties.getWidth(), properties.getHeight());
	}

	private Rectangle2D.Double toRectangle(Bound bound) {
		return new Rectangle2D.Double(bound.getX(), bound.getY(), bound.getWidth(), bound.getHeight());
	}

	private BufferedImage getLogo() {
		final String filename = "images/reactome_logo_100pxW_50T.png";
		final InputStream resource = Resources.class.getResourceAsStream(filename);
		try {
			return ImageIO.read(resource);
		} catch (IOException e) {
			LoggerFactory.getLogger("diagram-exporter").error("Logo not found in resources");
		}
		return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	}

	private void createBottomTextBox(double logoWidth, double logoHeight) {
		final Rectangle2D bounds = canvas.getBounds();
		bottomTextBox = NodePropertiesFactory.get(bounds.getMinX(), bounds.getMaxY() - logoHeight, bounds.getWidth() - logoWidth, logoHeight);
	}
}
