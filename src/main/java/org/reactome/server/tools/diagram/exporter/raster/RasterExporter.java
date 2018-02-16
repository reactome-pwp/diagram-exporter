package org.reactome.server.tools.diagram.exporter.raster;

import org.apache.commons.io.IOUtils;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.exception.ResourceGoneException;
import org.reactome.server.analysis.core.result.exception.ResourceNotFoundException;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.DiagramRenderer;
import org.reactome.server.tools.diagram.exporter.raster.ehld.EhldRenderer;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EhldException;
import org.springframework.stereotype.Component;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
@Component
public class RasterExporter {

	private final String diagramPath;
	private final String ehldPath;
	private final Set<String> ehld;
	private final TokenUtils tokenUtils;

	static  {
		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/org/reactome/server/tools/diagram/exporter/raster/fonts/arial.ttf")));
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/org/reactome/server/tools/diagram/exporter/raster/fonts/arialbd.ttf")));
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configures a new RasterExporter setting the resources paths.
	 *
	 * @param diagramPath  path to standard diagrams
	 * @param ehldPath     path to EHLDs
	 * @param analysisPath path to analysis results (aka token)
	 * @param svgSummary   path to the the list of stId that have EHLD
	 */
	public RasterExporter(String diagramPath, String ehldPath, String analysisPath, String svgSummary) {
		this.diagramPath = diagramPath;
		this.ehldPath = ehldPath;
		this.tokenUtils = new TokenUtils(analysisPath);
		Set<String> ehld;
		try {
			ehld = new TreeSet<>(IOUtils.readLines(new FileReader(svgSummary)));
		} catch (IOException e) {
			ehld = new HashSet<>();
		}
		this.ehld = ehld;
	}

	/**
	 * Renders args as a BufferedImage. See {@link RasterOutput} for saving
	 * options.
	 *
	 * @param args image arguments
	 */
	public BufferedImage export(RasterArgs args) throws AnalysisException, EhldException, DiagramJsonNotFoundException, DiagramJsonDeserializationException {
		return export(args, null);
	}

	/**
	 * Renders args as a BufferedImage. See {@link RasterOutput} for saving
	 * options.
	 *
	 * @param args image arguments
	 */
	public BufferedImage export(RasterArgs args, AnalysisStoredResult result) throws AnalysisException, EhldException, DiagramJsonNotFoundException, DiagramJsonDeserializationException {
		final RasterRenderer renderer = getRenderer(args, result);
//		final Dimension dimension = renderer.getDimension();
//		double size = dimension.getHeight() * dimension.getWidth();
		return renderer.render();
	}

	/**
	 * Generates an animated GIF with as many frames as columns in the analysis
	 * token. args.getColumn() is ignored. Animated GIFs must be written
	 * directly into an <code>{@link OutputStream}</code>. There is no Java
	 * class that supports storing a GIF in memory.
	 */
	public void exportToGif(RasterArgs args, OutputStream os) throws IOException, AnalysisException, EhldException, DiagramJsonNotFoundException, DiagramJsonDeserializationException {
		exportToGif(args, os, null);
	}

	/**
	 * Generates an animated GIF with as many frames as columns in the analysis
	 * token. args.getColumn() is ignored. Animated GIFs must be written
	 * directly into an <code>{@link OutputStream}</code>. There is no Java
	 * class that supports storing a GIF in memory.
	 */
	public void exportToGif(RasterArgs args, OutputStream os, AnalysisStoredResult result) throws AnalysisException, EhldException, DiagramJsonNotFoundException, DiagramJsonDeserializationException, IOException {
		final RasterRenderer renderer = getRenderer(args, result);
		renderer.renderToAnimatedGif(os);
	}

	/**
	 * Creates a SVG document from the diagram, adding, if asked, selection,
	 * flagging and analysis.See {@link RasterOutput} for saving options.
	 */
	public SVGDocument exportToSvg(RasterArgs args) throws AnalysisException, EhldException, DiagramJsonNotFoundException, DiagramJsonDeserializationException {
		return exportToSvg(args, null);
	}

	/**
	 * Creates a SVG document from the diagram, adding, if asked, selection,
	 * flagging and analysis.See {@link RasterOutput} for saving options.
	 */
	public SVGDocument exportToSvg(RasterArgs args, AnalysisStoredResult result) throws AnalysisException, EhldException, DiagramJsonNotFoundException, DiagramJsonDeserializationException {
		final RasterRenderer renderer = getRenderer(args, result);
		return renderer.renderToSVG();
	}

	/**
	 * Creates a proper RasterRenderer depending on the type of the source
	 * diagram (standard or enhanced).
	 */
	private RasterRenderer getRenderer(RasterArgs args, AnalysisStoredResult result) throws AnalysisException, EhldException, DiagramJsonNotFoundException, DiagramJsonDeserializationException {
		result = getResult(args.getToken(), result);
		return ehld.contains(args.getStId())
				? new EhldRenderer(args, ehldPath, result)
				: new DiagramRenderer(args, diagramPath, result);
	}

	/**
	 * If result is not null, use result. If not and token is not null try to
	 * get the result using it. If token is also null, returns null.
	 */
	private AnalysisStoredResult getResult(String token, AnalysisStoredResult result) throws AnalysisException {
		if (result != null) return result;
		if (token == null) return null;
		try {
			return tokenUtils.getFromToken(token);
		} catch (ResourceGoneException e) {
			throw new AnalysisException("Token has expired: " + token, e);
		} catch (ResourceNotFoundException e) {
			throw new AnalysisException("Token not valid: " + token, e);
		}
	}
}
