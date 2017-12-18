package org.reactome.server.tools.diagram.exporter.raster;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Render Reactome pathway diagrams into {@link BufferedImage}.
 */
public interface RasterRenderer {

	/**
	 * Get the final dimensions of the image
	 * @return the diagram final dimensions
	 */
	Dimension getDimension();

	/**
	 * Renders the diagram into a BufferedImage
	 *
	 * @return a BufferedImage that contains the diagram
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
}
