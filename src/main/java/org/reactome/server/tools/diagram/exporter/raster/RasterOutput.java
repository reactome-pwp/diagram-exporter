package org.reactome.server.tools.diagram.exporter.raster;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.w3c.dom.svg.SVGDocument;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;

/**
 * Supporting class to output generated diagrams.
 */
@SuppressWarnings("WeakerAccess")
public class RasterOutput {

	/**
	 * Saves document into file.
	 */
	public static void save(SVGDocument document, File file) throws IOException, TranscoderException {
		final SVGTranscoder transcoder = new SVGTranscoder();
		final TranscoderInput input = new TranscoderInput(document);
		final FileWriter writer = new FileWriter(file);
		final TranscoderOutput output = new TranscoderOutput(writer);
		transcoder.transcode(input, output);
	}

	/**
	 * Shortcut for <code>save(document, os, true)</code>. See {@link
	 * RasterOutput#save(SVGDocument, OutputStream, boolean)}
	 */
	public static void save(SVGDocument document, OutputStream os) throws TranscoderException, IOException {
		save(document, os, true);
	}

	/**
	 * Sends document through os. If close is <em>true</em>, closes os.
	 */
	public static void save(SVGDocument document, OutputStream os, boolean close) throws TranscoderException, IOException {
		final SVGTranscoder transcoder = new SVGTranscoder();
		final TranscoderInput input = new TranscoderInput(document);
		final TranscoderOutput output = new TranscoderOutput(new OutputStreamWriter(os));
		transcoder.transcode(input, output);
		if (close) {
			os.flush();
			os.close();
		}
	}

	/**
	 * Shortcut for <code>save(document, connection, true)</code>.
	 *
	 * @see RasterOutput#save(SVGDocument, HttpURLConnection, boolean)
	 */
	public static void save(SVGDocument document, HttpURLConnection connection) throws TranscoderException, IOException {
		save(document, connection, true);
	}

	/**
	 * Sends the document through the OutputStream of the connection. If close
	 * is true, the OutputStream is flushed and closed.
	 */
	public static void save(SVGDocument document, HttpURLConnection connection, boolean close) throws TranscoderException, IOException {
		save(document, connection.getOutputStream(), close);
	}

	/**
	 * Saves image into file.
	 */
	public static void save(BufferedImage image, String ext, File file) throws IOException {
		ImageIO.write(image, ext, file);
	}

	/**
	 * Shortcut for <code>save(image, ext, os, true)</code>.
	 *
	 * @see RasterOutput#save(BufferedImage, String, OutputStream, boolean)
	 */
	public static void save(BufferedImage image, String ext, OutputStream os) throws IOException {
		save(image, ext, os, true);
	}

	/**
	 * Sends image through os. If close is true, flushes and closes os.
	 */
	public static void save(BufferedImage image, String ext, OutputStream os, boolean close) throws IOException {
		ImageIO.write(image, ext, os);
		if (close) {
			os.flush();
			os.close();
		}
	}

	/**
	 * Shortcut for <code>save(image, ext, connection, true)</code>
	 *
	 * @see RasterOutput#save(BufferedImage, String, HttpURLConnection,
	 * boolean)
	 */
	public static void save(BufferedImage image, String ext, HttpURLConnection connection) throws IOException {
		save(image, ext, connection, true);
	}

	/**
	 * Sends image through connection's OutputStream. If close is true, flushes
	 * and closes the OutputStream.
	 */
	public static void save(BufferedImage image, String ext, HttpURLConnection connection, boolean close) throws IOException {
		connection.setDoOutput(true);
		save(image, ext, connection.getOutputStream(), close);
	}

}
