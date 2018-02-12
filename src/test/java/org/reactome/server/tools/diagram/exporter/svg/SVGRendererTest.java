package org.reactome.server.tools.diagram.exporter.svg;

import org.apache.batik.transcoder.TranscoderException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reactome.server.tools.diagram.exporter.raster.DiagramOutput;
import org.reactome.server.tools.diagram.exporter.raster.RasterRenderer;
import org.reactome.server.tools.diagram.exporter.raster.TestUtils;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.diagram.DiagramRenderer;
import org.reactome.server.tools.diagram.exporter.raster.ehld.EHLDRenderer;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDException;
import org.w3c.dom.svg.SVGDocument;

import java.io.File;
import java.io.IOException;

public class SVGRendererTest {

	private static final String DIAGRAM_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/diagram";
	private static final String EHLD_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/ehld";
	private static final File SVG_FOLDER = new File("test-svg");

	// Set to true for visual inspection of tests
	// todo: don't forget to set to false before pushing
	private static final boolean save = false;

	@BeforeClass
	public static void beforeClass() {
		TestUtils.createDir(SVG_FOLDER);
	}

	@AfterClass
	public static void afterClass() {
		if (!save) TestUtils.removeDir(SVG_FOLDER);
	}

	@Test
	public void testDiagram() throws Exception {
		// Create SVG
		final RasterArgs args = new RasterArgs("R-HSA-109606", "svg");
		args.setToken(TestUtils.TOKEN_OVER_2);
		final RasterRenderer renderer = new DiagramRenderer(args, DIAGRAM_PATH);
		final SVGDocument document = renderer.renderToSVG();
		// Save to file
		final File file = new File(SVG_FOLDER, TestUtils.getFileName(args));
		DiagramOutput.save(document, file);
	}

	@Test
	public void testEHLD() throws EHLDException, TranscoderException, IOException {
		// Create svg
		final RasterArgs args = new RasterArgs("R-HSA-74160", "svg");
		args.setToken(TestUtils.TOKEN_EXPRESSION_1);
		final RasterRenderer renderer = new EHLDRenderer(args, EHLD_PATH);
		final SVGDocument document = renderer.renderToSVG();
		// Save to file
		final File file = new File(SVG_FOLDER, TestUtils.getFileName(args));
		DiagramOutput.save(document, file);
	}

	@Test
	public void testEhldFont() throws EHLDException, IOException, TranscoderException {
		// Create svg
		final RasterArgs args = new RasterArgs("R-HSA-69278", "svg");
		final RasterRenderer renderer = new EHLDRenderer(args, EHLD_PATH);
		final SVGDocument document = renderer.renderToSVG();
		// Save to file
		final File file = new File(SVG_FOLDER, TestUtils.getFileName(args));
		DiagramOutput.save(document, file);
		// REPORT: partial fix. EHLDs don't follow SVG standards (Illustrator)
	}

}
