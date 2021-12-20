package org.reactome.server.tools.diagram.exporter.raster;

import org.junit.jupiter.api.Assertions;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.DiagramRendererTest;
import org.reactome.server.tools.diagram.exporter.raster.ehld.EhldRendererTest;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EhldException;

/**
 * Supporting methods for testing diagram renderering. It is used both in {@link
 * DiagramRendererTest} and {@link EhldRendererTest}. It contains all the common
 * staff, like performing the analysis or creating and deleting the images dir.
 */
public class TestUtils {
	public static final String TOKEN_OVER_1 = "MjAyMTEyMTcxNDQyNDBfMg%253D%253D"; // uniprot (GBM Uniprot)
	public static final String TOKEN_EXPRESSION_1 = "MjAyMTEyMTcxNDU4MDhfNQ%253D%253D";  // microarray (probeset)
	public static final String TOKEN_EXPRESSION_2 = "MjAyMTEyMTcxNDU1NDZfNA%253D%253D";  // HPA (GeneName)
	public static final String TOKEN_SPECIES = "MjAyMTEyMTcxNTE1MDBfNg%253D%253D"; // canis

	public static final String TOKEN_GSA = "MjAyMTEyMTcxNTI2MzJfOA%253D%253D";
	public static final String TOKEN_GSVA = "MjAyMTEyMTcxNTMzNDRfOQ%253D%253D";

	private static final String ANALYSIS_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/analysis";
	private static final String DIAGRAM_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/diagram";
	private static final String EHLD_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/ehld";
	private static final String SVG_SUMMARY = "src/test/resources/org/reactome/server/tools/diagram/exporter/svgsummary.txt";
	private static final TokenUtils TOKEN_UTILS = new TokenUtils(ANALYSIS_PATH);

	private static final RasterExporter EXPORTER;

	static {
		EXPORTER = new RasterExporter(DIAGRAM_PATH, EHLD_PATH, ANALYSIS_PATH, SVG_SUMMARY);
	}

	public static AnalysisStoredResult getResult(String token) {
		return TOKEN_UTILS.getFromToken(token);
	}

	public static void render(RasterArgs args) {
		render(args, null);
	}

	public static void render(RasterArgs args, AnalysisStoredResult result) {
		try {
//			final BufferedImage image = EXPORTER.exportToImage(args);
			EXPORTER.exportToSvg(args, result);
			//ImageIO.write(EXPORTER.exportToImage(args, result), "jpeg", new File("/Users/reactome/test.jpeg"));
		} catch (EhldException | AnalysisException | DiagramJsonDeserializationException | DiagramJsonNotFoundException e) {
			e.printStackTrace();
			Assertions.fail(e.getMessage());
		}
	}
}