package org.reactome.server.tools.diagram.exporter.svg;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.TestUtils;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.w3c.dom.svg.SVGDocument;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SVGExporterTest {

	private static final String DIAGRAM_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/diagram";
	private static final String EHLD_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/ehld";
	private static final File SVG_FOLDER = new File("test-svg");

	// Set to true for visual inspection of tests
	// todo: don't forget to set to false before pushing
	private static final boolean save = true;

	@BeforeClass
	public static void beforeClass() {
		TestUtils.createDir(SVG_FOLDER);
	}

	@AfterClass
	public static void afterClass() {
		if (!save) TestUtils.removeDir(SVG_FOLDER);
	}

	@Test
	public void testOne() {
		RasterArgs args = new RasterArgs("R-HSA-109606", "svg");
		args.setToken(TestUtils.performAnalysis("expression_data.txt"));
		render(args);
	}

	private void render(RasterArgs args) {
		final SVGExporter exporter;
		try {
			exporter = new SVGExporter(args, DIAGRAM_PATH, EHLD_PATH);
		} catch (DiagramJsonNotFoundException | DiagramJsonDeserializationException | AnalysisServerError | AnalysisException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
			return;
		}
		final SVGDocument document = exporter.export();
		final TranscoderInput input = new TranscoderInput(document);
		final SVGTranscoder transcoder = new SVGTranscoder();
//		final TranscodingHints hints = new TranscodingHints();
//		transcoder.setTranscodingHints(hints);

		try (FileWriter writer = new FileWriter(new File(SVG_FOLDER, TestUtils.getFileName(args)))) {
			final TranscoderOutput output = new TranscoderOutput(writer);
			transcoder.transcode(input, output);
		} catch (TranscoderException | IOException e) {
			e.printStackTrace();
		}

	}

}
