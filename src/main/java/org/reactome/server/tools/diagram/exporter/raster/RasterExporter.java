package org.reactome.server.tools.diagram.exporter.raster;

import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.DiagramRenderer;
import org.reactome.server.tools.diagram.exporter.raster.ehld.EHLDRenderer;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class RasterExporter {

    private static Set<String> hasEHLD = new HashSet<>();

    public static void initialise(String svgSummaryFile){
        try {
            final URL url = new File(svgSummaryFile).toURI().toURL();
            final List<String> lines = IOUtils.readLines(url.openStream(), Charset.defaultCharset());
            hasEHLD.addAll(lines);
        } catch (IOException e) {
//			e.printStackTrace();
        }
    }

    /**
     * Service layer that provides access to the raster exporter. This service
     * outputs the result as a BufferedImage, not to a File.
     * <p>
     * To save the image to an URL: <code>
     * <pre>
     * BufferedImage image = RasterExporter.export(args, dPath, ePath);
     * URL url = new URL("http://host.com/");
     * HttpUrlConnection connection = (HttpUrlConnection)
     * url.openConnection();
     * connection.setDoOutput(true);  // your url must support writing
     * OutputStream os = connection.getOutputStream();
     * ImageIO.write(image, ext, os);
     * </pre>
     * </code>
     * <p>
     * To save to a File <code>
     * <pre>
     * BufferedImage image = RasterExporter.export(args, dPath, ePath);
     * File file = new File(path, stId + ".png");
     * ImageIO.write(image, ext, file);
     * </pre>
     * </code>
     *
     * @param args        arguments for the export
     * @param diagramPath location of diagrams
     * @param ehldPath    location of EHLDs
     */
    public static BufferedImage export(RasterArgs args, String diagramPath, String ehldPath) throws DiagramJsonNotFoundException, DiagramJsonDeserializationException, EHLDException, AnalysisServerError, AnalysisException {
        if (hasEHLD.contains(args.getStId())) {
            final EHLDRenderer renderer = new EHLDRenderer(args, ehldPath);
            return renderer.render();
        } else {
            final DiagramRenderer renderer = new DiagramRenderer(args, diagramPath);
            return renderer.render();
        }
    }

    /**
     * Generates an animated GIF with as many frames as columns in the analysis
     * token. args.getColumn() is ignored. Animated GIFs are written directly
     * into an <code>{@link OutputStream}</code>. There is no Java class that
     * supports storing a GIF in memory.
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
     * BufferedImage image = RasterExporter.export(args, dPath, ePath);
     * File file = new File(path, stId + ".png");
     * OutputStream os = new FileOutputStream(file);
     * RasterExporter.exportToGif(args, dPath, ePath, os);
     * </pre>
     * </code>
     */
    public static void exportToGif(RasterArgs args, String diagramPath, String ehldPath, OutputStream os) throws DiagramJsonNotFoundException, DiagramJsonDeserializationException, EHLDException, AnalysisServerError, AnalysisException, IOException {
        if (hasEHLD.contains(args.getStId())) {
            final EHLDRenderer renderer = new EHLDRenderer(args, ehldPath);
            renderer.renderToAnimatedGif(os);
        } else {
            final DiagramRenderer renderer = new DiagramRenderer(args, diagramPath);
            renderer.renderToAnimatedGif(os);
        }
    }

}
