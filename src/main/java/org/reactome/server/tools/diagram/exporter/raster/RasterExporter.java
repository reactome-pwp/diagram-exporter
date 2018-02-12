package org.reactome.server.tools.diagram.exporter.raster;

import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.DiagramRenderer;
import org.reactome.server.tools.diagram.exporter.raster.ehld.EHLDRenderer;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDException;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDMalformedException;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDRuntimeException;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class RasterExporter {

	private static Set<String> hasEHLD = new HashSet<>();

	public static void initialise(String svgSummaryFile) {
		try {
			hasEHLD = new HashSet<>(IOUtils.readLines(new FileInputStream(svgSummaryFile), Charset.defaultCharset()));
		} catch (IOException ignored) {
		}
	}

	/**
	 * Service layer that provides access to the raster exporter. This service
	 * outputs the result as a BufferedImage, not to a File.
	 * <p>
	 * To save the image to an URL use DiagramOutput: <code>
	 * <pre>
	 * BufferedImage image = RasterExporter.export(args, dPath, ePath);
	 * URL url = new URL("http://host.com/");
	 * HttpUrlConnection connection = (HttpUrlConnection)
	 * url.openConnection();
	 * DiagramOutput.save(image, connection);
	 * </pre>
	 * </code>
	 * <p>
	 * To save to a File <code>
	 * <pre>
	 * BufferedImage image = RasterExporter.export(args, dPath, ePath);
	 * File file = new File(path, stId + ".png");
	 * DiagramOutput.save(image, ext, file);
	 * </pre>
	 * </code>
	 *
	 * @param args        arguments for the export
	 * @param diagramPath location of diagrams
	 * @param EHLDPath    location of EHLDs
	 */
	public static BufferedImage export(RasterArgs args, String diagramPath, String EHLDPath) throws Exception {
		try {
			final RasterRenderer renderer = getRenderer(args, diagramPath, EHLDPath);
			final Dimension dimension = renderer.getDimension();
			double size = dimension.getHeight() * dimension.getWidth();
			return renderer.render();
		} catch (DiagramJsonNotFoundException | EHLDNotFoundException e) {
			throw new Exception(String.format("there is no diagram for '%s'", args.getStId()), e);
		} catch (DiagramJsonDeserializationException | EHLDMalformedException e) {
			throw new Exception(String.format("problems reading diagram of '%s'", args.getStId()), e);
//		} catch (AnalysisServerError | AnalysisException e) {
//			throw new Exception(String.format("analysis token not valid '%s'", args.getToken()), e);
		} catch (EHLDRuntimeException e) {
			throw new Exception(String.format("an exception happened rendering %s", args.getStId()), e);
		}
	}

	/**
	 * Generates an animated GIF with as many frames as columns in the analysis
	 * token. args.getColumn() is ignored. Animated GIFs must be written
	 * directly into an <code>{@link OutputStream}</code>. There is no Java
	 * class that supports storing a GIF in memory.
	 * <p>
	 * To save the GIF to an URL: <code>
	 * <pre>
	 * URL url = new URL(...);
	 * HttpUrlConnection connection = (HttpUrlConnection)
	 * url.openConnection();
	 * connection.setDoOutput(true);  // your url must support writing
	 * OutputStream os = connection.getOutputStream();
	 * RasterExporter.exportToGif(args, dPath, ePath, os);
	 * </pre>
	 * </code>
	 * <p>
	 * To save to a File <code>
	 * <pre>
	 * File file = new File(path, stId + ".png");
	 * OutputStream os = new FileOutputStream(file);
	 * RasterExporter.exportToGif(args, dPath, ePath, os);
	 * </pre>
	 * </code>
	 */
	public static void exportToGif(RasterArgs args, String diagramPath, String EHLDPath, OutputStream os) throws Exception {
		try {
			final RasterRenderer renderer = getRenderer(args, diagramPath, EHLDPath);
			renderer.renderToAnimatedGif(os);
		} catch (DiagramJsonNotFoundException | DiagramJsonDeserializationException | EHLDException e) {
			throw new Exception(String.format("there is no diagram for '%s'", args.getStId()), e);
//		} catch (AnalysisServerError | AnalysisException e) {
//			throw new Exception(String.format("analysis token not valid '%s'", args.getToken()), e);
		}

	}


	/**
	 * Creates a SVG document from the diagram, adding, if asked, selection,
	 * flagging and analysis.
	 * <p>
	 * Use DiagramOutput to save your results.<code>
	 * <pre>
	 * SVGDocument document = RasterExporter.exportToSVG(args, diagramPath,
	 * EHLDPath);
	 * DiagramOutput.save(document, new File("output.svg");
	 * </pre>
	 * </code>
	 * <p>
	 * To send through a HTTP connection <code>
	 * <pre>
	 * SVGDocument document = RasterExporter.exportToSVG(args, diagramPath,
	 * EHLDPath);
	 * DiagramOutput.save(document, connection);
	 * </pre>
	 * </code>
	 */
	public static SVGDocument exportToSVG(RasterArgs args, String diagramPath, String EHLDPath) throws Exception {
		try {
			final RasterRenderer renderer = getRenderer(args, diagramPath, EHLDPath);
			return renderer.renderToSVG();
		} catch (EHLDException | DiagramJsonNotFoundException | DiagramJsonDeserializationException e) {
			throw new Exception(String.format("there is no diagram for '%s'", args.getStId()), e);
//		} catch (AnalysisException | AnalysisServerError e) {
//			throw new Exception(String.format("analysis token not valid '%s'", args.getToken()), e);
		}

	}

	/**
	 * Creates a proper RasterRenderer depending on the type of the source
	 * diagram (standard or enhanced).
	 *
	 * @param args        to create the renderer
	 * @param diagramPath path where standard diagrams are located
	 * @param EHLDPath    path where EHLD are located
	 */
	private static RasterRenderer getRenderer(RasterArgs args, String diagramPath, String EHLDPath)
			throws Exception {
		return hasEHLD.contains(args.getStId())
				? new EHLDRenderer(args, EHLDPath)
				: new DiagramRenderer(args, diagramPath);
	}
}
