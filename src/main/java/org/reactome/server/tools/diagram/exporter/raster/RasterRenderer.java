package org.reactome.server.tools.diagram.exporter.raster;

import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Render Reactome pathway diagrams. Diagrams can be exported to raster
 * (BufferedImage), or SVG (SVGDocument).
 */
public interface RasterRenderer {

	/**
	 * Get the final dimensions of the image
	 */
	@SuppressWarnings("unused")
	Dimension getDimension();

	/**
	 * Renders the diagram into a BufferedImage
	 */
	BufferedImage render();

	/**
	 * Renders the diagram as an animated GIF and sends the result to the
	 * OutputStream. GIFs need to be sent to an OutputStream because they cannot
	 * be stored in a BufferedImage.
	 *
	 * @param outputStream where to stream the GIF
	 *
	 * @throws IOException as thrown in {@link OutputStream#write(byte[])}
	 */
	void renderToAnimatedGif(OutputStream outputStream) throws IOException;

	/**
	 * Renders the diagram into a SVGDocument.
	 */
	SVGDocument renderToSVG();
}
