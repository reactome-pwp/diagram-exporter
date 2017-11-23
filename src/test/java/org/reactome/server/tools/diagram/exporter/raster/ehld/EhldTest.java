package org.reactome.server.tools.diagram.exporter.raster.ehld;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reactome.server.tools.diagram.exporter.raster.RasterExporter;
import org.reactome.server.tools.diagram.exporter.raster.api.SimpleRasterArgs;

import java.awt.image.BufferedImage;
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
	public void testRaster() {
		final SimpleRasterArgs args = new SimpleRasterArgs("R-HSA-382551", "png");
		try {
			args.setQuality(5);
			final BufferedImage image = RasterExporter.export(args, DIAGRAM_PATH, EHLD_PATH);
//			ImageIO.write(image, args.getFormat(), new File(args.getStId() + "." + args.getFormat()));

		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testDecorator() {
		final SimpleRasterArgs args = new SimpleRasterArgs("R-HSA-109581", "png");
		args.setQuality(8);
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
