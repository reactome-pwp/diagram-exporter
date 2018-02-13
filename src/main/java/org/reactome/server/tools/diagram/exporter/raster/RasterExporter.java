package org.reactome.server.tools.diagram.exporter.raster;

import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.DiagramRenderer;
import org.reactome.server.tools.diagram.exporter.raster.ehld.EhldRenderer;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDException;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDMalformedException;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDRuntimeException;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Set;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class RasterExporter {


	private final String diagramPath;
	private final String ehldPath;
	private final Set<String> ehld;
	private final TokenUtils tokenUtils;

	public RasterExporter(String diagramPath, String ehldPath, String analysisPath, Set<String> ehld) {
		this.diagramPath = diagramPath;
		this.ehldPath = ehldPath;
		this.ehld = ehld;
		this.tokenUtils = new TokenUtils(analysisPath);
	}

	/**
	 * Service layer that provides access to the raster exporter. This service
	 * outputs the result as a BufferedImage, not to a File.
	 * <p>
	 * To save the image to an URL use RasterOutput: <code>
	 * <pre>
	 * BufferedImage image = RasterExporter.export(args, dPath, ePath);
	 * URL url = new URL("http://host.com/");
	 * HttpUrlConnection connection = (HttpUrlConnection)
	 * url.openConnection();
	 * RasterOutput.save(image, connection);
	 * </pre>
	 * </code>
	 * <p>
	 * To save to a File <code>
	 * <pre>
	 * BufferedImage image = RasterExporter.export(args, dPath, ePath);
	 * File file = new File(path, stId + ".png");
	 * RasterOutput.save(image, ext, file);
	 * </pre>
	 * </code>
	 *
	 * @param args arguments for the export
	 */
	public BufferedImage export(RasterArgs args) throws Exception {
		return export(args, getResult(args.getToken()));
	}

	private AnalysisStoredResult getResult(String token) {
		return token == null
				? null
				: tokenUtils.getFromToken(token);
	}

	public BufferedImage export(RasterArgs args, AnalysisStoredResult result) throws Exception {
		try {
			final RasterRenderer renderer = getRenderer(args, result);
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
		} catch (Exception e) {
			throw new Exception(String.format("analysis token not valid: %s", args.getToken()), e);
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
	public void exportToGif(RasterArgs args, OutputStream os) throws Exception {
		exportToGif(args, os, getResult(args.getToken()));
	}

	public void exportToGif(RasterArgs args, OutputStream os, AnalysisStoredResult result) throws Exception {
		try {
			final RasterRenderer renderer = getRenderer(args, result);
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
	 * Use RasterOutput to save your results.<code>
	 * <pre>
	 * SVGDocument document = RasterExporter.exportToSvg(args, diagramPath,
	 * EHLDPath);
	 * RasterOutput.save(document, new File("output.svg");
	 * </pre>
	 * </code>
	 * <p>
	 * To send through a HTTP connection <code>
	 * <pre>
	 * SVGDocument document = RasterExporter.exportToSvg(args, diagramPath,
	 * EHLDPath);
	 * RasterOutput.save(document, connection);
	 * </pre>
	 * </code>
	 */
	public SVGDocument exportToSvg(RasterArgs args, AnalysisStoredResult result) throws Exception {
		try {
			final RasterRenderer renderer = getRenderer(args, result);
			return renderer.renderToSVG();
		} catch (EHLDException | DiagramJsonNotFoundException | DiagramJsonDeserializationException e) {
			throw new Exception(String.format("there is no diagram for '%s'", args.getStId()), e);
//		} catch (AnalysisException | AnalysisServerError e) {
//			throw new Exception(String.format("analysis token not valid '%s'", args.getToken()), e);
		}
	}

	public SVGDocument exportToSvg(RasterArgs args) throws Exception {
		return exportToSvg(args, getResult(args.getToken()));
	}


	/**
	 * Creates a proper RasterRenderer depending on the type of the source
	 * diagram (standard or enhanced).
	 */
	private RasterRenderer getRenderer(RasterArgs args, AnalysisStoredResult result) throws Exception {
		return ehld.contains(args.getStId())
				? new EhldRenderer(args, ehldPath, result)
				: new DiagramRenderer(args, diagramPath, result);
	}
}
