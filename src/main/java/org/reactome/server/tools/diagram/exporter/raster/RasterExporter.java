package org.reactome.server.tools.diagram.exporter.raster;

import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.DiagramRenderer;
import org.reactome.server.tools.diagram.exporter.raster.ehld.EHLDRenderer;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class RasterExporter {

	private static Set<String> hasEHLD = new HashSet<>();

	static {
		try {
			final URL url = new URL("https://reactome.org/download/current/ehld/svgsummary.txt");
			final List<String> lines = IOUtils.readLines(url.openStream());
			hasEHLD.addAll(lines);
		} catch (MalformedURLException e) {
//			e.printStackTrace();
		} catch (IOException e) {
//			e.printStackTrace();
		}
	}

	/**
	 * If you want an animated GIF, then image is generated directly into an
	 * OutputStream. Use this method only in that case.
	 */
	public static void exportToGif(RasterArgs args, OutputStream os, String diagramPath) throws DiagramJsonNotFoundException, DiagramJsonDeserializationException, IOException {
		final DiagramRenderer renderer = new DiagramRenderer(args, diagramPath);
		renderer.renderToAnimatedGif(os);
	}

	/**
	 * Service layer that provides access to the raster exporter. This service
	 * outputs the result as a BufferedImage, not to a File.
	 * <p>
	 * To save the image to an URL: <code>
	 * <pre>
	 *     URL url = new URL("http://host.com/");
	 *     HttpUrlConnection connection = (HttpUrlConnection)
	 * url.openConnection();
	 *     connection.setDoOutput(true);  // your url must support writing
	 *     OutputStream os = connection.getOutputStream();
	 *     ImageIO.write(image, ext, os);
	 * </pre>
	 * </code>
	 * <p>
	 * To save to a File <code>
	 * <pre>
	 * File file = new File(path, stId + ".png");
	 * ImageIO.write(image, ext, file);
	 * </pre>
	 * </code>
	 *
	 * @param args arguments for the export
	 */
	public static BufferedImage export(RasterArgs args) throws DiagramJsonNotFoundException, DiagramJsonDeserializationException, EHLDException {
		return export(args, null, null);
	}

	/**
	 * Service layer that provides access to the raster exporter. This service
	 * outputs the result as a BufferedImage, not to a File.
	 * <p>
	 * To save the image to an URL: <code>
	 * <pre>
	 *     URL url = new URL("http://host.com/");
	 *     HttpUrlConnection connection = (HttpUrlConnection)
	 * url.openConnection();
	 *     connection.setDoOutput(true);  // your url must support writing
	 *     OutputStream os = connection.getOutputStream();
	 *     ImageIO.write(image, ext, os);
	 * </pre>
	 * </code>
	 * <p>
	 * To save to a File <code>
	 * <pre>
	 * File file = new File(path, stId + ".png");
	 * ImageIO.write(image, ext, file);
	 * </pre>
	 * </code>
	 *
	 * @param args        arguments for the export
	 * @param diagramPath
	 * @param ehldPath
	 */
	public static BufferedImage export(RasterArgs args, String diagramPath, String ehldPath) throws DiagramJsonNotFoundException, DiagramJsonDeserializationException, EHLDException {
		if (hasEHLD.contains(args.getStId())) {
			return fromEhld(args, ehldPath);
		} else {
			return fromDiagram(args, diagramPath);
		}
	}

	private static BufferedImage fromDiagram(RasterArgs args, String diagramPath) throws DiagramJsonNotFoundException, DiagramJsonDeserializationException {
		final DiagramRenderer renderer = new DiagramRenderer(args, diagramPath);
		return renderer.render();
	}

	private static BufferedImage fromEhld(RasterArgs args, String ehldPath) throws EHLDException {
		final EHLDRenderer EHLDRenderer = new EHLDRenderer(args, ehldPath);
		return EHLDRenderer.render();
	}
}
