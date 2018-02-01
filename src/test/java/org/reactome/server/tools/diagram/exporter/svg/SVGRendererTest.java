package org.reactome.server.tools.diagram.exporter.svg;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.RasterRenderer;
import org.reactome.server.tools.diagram.exporter.raster.TestUtils;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.DiagramRenderer;
import org.reactome.server.tools.diagram.exporter.raster.ehld.EHLDRenderer;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDException;
import org.w3c.dom.svg.SVGDocument;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SVGRendererTest {

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
	public void testDiagram() throws AnalysisException, AnalysisServerError, DiagramJsonNotFoundException, DiagramJsonDeserializationException, TranscoderException {
		final RasterArgs args = new RasterArgs("R-HSA-109606", "svg");
		args.setToken(TestUtils.performAnalysis("expression_data.txt"));
		final RasterRenderer renderer = new DiagramRenderer(args, DIAGRAM_PATH);
		final SVGDocument document = renderer.renderToSVG();
		final SVGTranscoder transcoder = new SVGTranscoder();

		final TranscoderInput input = new TranscoderInput(document);
		try (FileWriter writer = new FileWriter(new File(SVG_FOLDER, TestUtils.getFileName(args)))) {
			final TranscoderOutput output = new TranscoderOutput(writer);
			transcoder.transcode(input, output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testEHLD() throws EHLDException, TranscoderException {
		final RasterArgs args = new RasterArgs("R-HSA-74160", "svg");
		args.setToken(TestUtils.performAnalysis("expression_data.txt"));
		final RasterRenderer renderer = new EHLDRenderer(args, EHLD_PATH);
		final SVGDocument document = renderer.renderToSVG();
		final SVGTranscoder transcoder = new SVGTranscoder();

		final TranscoderInput input = new TranscoderInput(document);
		try (FileWriter writer = new FileWriter(new File(SVG_FOLDER, TestUtils.getFileName(args)))) {
			final TranscoderOutput output = new TranscoderOutput(writer);
			transcoder.transcode(input, output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
