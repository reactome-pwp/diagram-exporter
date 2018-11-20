package org.reactome.server.tools.diagram.exporter.raster;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Assert;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.content.ContentServiceClient;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.DiagramRendererTest;
import org.reactome.server.tools.diagram.exporter.raster.ehld.EhldRendererTest;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EhldException;

import java.io.IOException;

/**
 * Supporting methods for testing diagram renderering. It is used both in {@link
 * DiagramRendererTest} and {@link EhldRendererTest}. It contains all the common
 * staff, like performing the analysis or creating and deleting the images dir.
 */
public class TestUtils {

	public static final String TOKEN_OVER_1 = "MjAxODExMDEwNzI3NDNfOA%253D%253D"; // uniprot (GBM Uniprot)
	public static final String TOKEN_OVER_2 = "MjAxODExMDEwNzMyMDdfOQ%253D%253D"; // Gene NCBI (12 tumors)
	public static final String TOKEN_EXPRESSION_1 = "MjAxODExMDEwNzMyMjJfMTA%253D";  // microarray (probeset)
	public static final String TOKEN_EXPRESSION_2 = "MjAxODEwMzAxMDIzMDBfNQ%253D%253D";  // HPA (GeneName)
	public static final String TOKEN_SPECIES = "MjAxODExMDEwNzMzMTRfMTE%253D"; // canis

	private static final String TODAYS_SERVER = "https://reactomedev.oicr.on.ca";

	private static final String ANALYSIS_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/analysis";
	private static final String DIAGRAM_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/diagram";
	private static final String EHLD_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/ehld";
	private static final String SVG_SUMMARY = "src/test/resources/org/reactome/server/tools/diagram/exporter/svgsummary.txt";
	private static final TokenUtils TOKEN_UTILS = new TokenUtils(ANALYSIS_PATH);

	private static final RasterExporter EXPORTER;

	static {
		EXPORTER = new RasterExporter(DIAGRAM_PATH, EHLD_PATH, ANALYSIS_PATH, SVG_SUMMARY);
		ContentServiceClient.setHost(TODAYS_SERVER);
		ContentServiceClient.setService("/ContentService");

	}

	public static AnalysisStoredResult getResult(String token) {
		return TOKEN_UTILS.getFromToken(token);
	}

	public static void render(RasterArgs args) {
		render(args, null);
	}

	public static void render(RasterArgs args, AnalysisStoredResult result) {
		try {
			EXPORTER.export(args, new NullOutputStream(), result);
		} catch (EhldException | AnalysisException | DiagramJsonDeserializationException | DiagramJsonNotFoundException | TranscoderException | IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

}
