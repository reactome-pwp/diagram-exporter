package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.FontProperties;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.TextLayer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;

/**
 * Adds a method for drug classes to render the Rx text.
 * <p><b>Note:</b> Since all Renderable* classes already extend another class
 * (at least {@link RenderableDiagramObject}), we cannot create a RenderableDrug class with the
 * {@link RenderableDiagramObject#draw(DiagramCanvas, ColorProfiles, org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramData, int)} overrided, that's why we
 * implemented this helper class.
 */
public class DrugHelper {

	private static final String RX = "Rx";

	private DrugHelper() {
	}

	// We cannot know the size of the text by just using the font, since it's graphics2D implementation dependent.
	private static double WIDTH = 15;
	private static double HEIGHT = 10;

	/**
	 * Updates the graphics2D information. This method should be call once per diagram.
	 *
	 * @param graphics2D current graphics2D
	 */
	public static void setGraphics2D(Graphics2D graphics2D) {
		final Font defaultFont = FontProperties.DEFAULT_FONT;
		final Font font = defaultFont.deriveFont(defaultFont.getSize2D() - 2);
		final FontMetrics metrics = graphics2D.getFontMetrics(font);
		HEIGHT = metrics.getHeight() + 2;
		WIDTH = metrics.charsWidth(RX.toCharArray(), 0, RX.length()) + 2;
	}

	/**
	 * Adds a Rx text in the bottom right corner of the node.
	 *
	 * @param canvas        the canvas where the text will be added
	 * @param node          the node where to place the text
	 * @param colorProfiles current diagram profile
	 * @param index         the diagram index
	 * @param xOff          distance between the text and the right margin of the node
	 * @param yOff          distance between the text and the bottom margin of the node
	 */
	static void addDrugText(DiagramCanvas canvas, RenderableNode node, ColorProfiles colorProfiles, DiagramIndex index, double xOff, double yOff) {
		final Color text = node.getTextColor(colorProfiles, index.getAnalysis().getType());
		final TextLayer textLayer = node.isFadeOut()
				? canvas.getFadeOutText()
				: canvas.getText();
		final NodeProperties prop = node.getNode().getProp();
		final NodeProperties position = NodePropertiesFactory.get(
				prop.getX() + prop.getWidth() - WIDTH - xOff,
				prop.getY() + prop.getHeight() - HEIGHT - yOff,
				WIDTH, HEIGHT);
		textLayer.add(RX, text, position, 1, 0, FontProperties.DEFAULT_FONT);
	}

}
