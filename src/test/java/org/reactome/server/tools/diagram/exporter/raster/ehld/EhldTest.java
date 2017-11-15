package org.reactome.server.tools.diagram.exporter.raster.ehld;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reactome.server.tools.diagram.exporter.raster.RasterExporter;
import org.reactome.server.tools.diagram.exporter.raster.api.SimpleRasterArgs;

import java.util.Arrays;

public class EhldTest {

	private static final String DIAGRAM_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/raster/diagram";
	private static final String EHLD_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/raster/ehld";
	private static final String SVG_SUMMARY = "src/test/resources/org/reactome/server/tools/diagram/exporter/raster/ehld/ehlds.txt";

	@BeforeClass
	public static void init(){
		RasterExporter.initialise(SVG_SUMMARY);
	}

	@Test
	public void testPng() {
		final SimpleRasterArgs args = new SimpleRasterArgs("R-HSA-449147", "png");
		try {
			args.setFactor(3.);
			RasterExporter.export(args, DIAGRAM_PATH, EHLD_PATH);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testPngDecorated() {
		final SimpleRasterArgs args = new SimpleRasterArgs("R-HSA-109581", "png");
		args.setFactor(2.0);
		args.setSelected(Arrays.asList("R-HSA-109606"));
		args.setFlags(Arrays.asList("R-HSA-109606"));
		try {
			RasterExporter.export(args, DIAGRAM_PATH, EHLD_PATH);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testJpeg() {
		final SimpleRasterArgs args = new SimpleRasterArgs("R-HSA-109581", "jpeg");
		args.setSelected(Arrays.asList("R-HSA-109606"));
		try {
			RasterExporter.export(args, DIAGRAM_PATH, EHLD_PATH);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGif() {
		final SimpleRasterArgs args = new SimpleRasterArgs("R-HSA-109581", "gif");
		args.setSelected(Arrays.asList("R-HSA-109606"));
		try {
			RasterExporter.export(args, DIAGRAM_PATH, EHLD_PATH);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

}
