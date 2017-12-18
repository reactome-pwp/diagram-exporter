package org.reactome.server.tools.diagram.exporter.raster.ehld;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reactome.server.tools.diagram.exporter.raster.TestUtils;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDException;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.IntStream;

public class EhldRendererTest {

	private static final File IMAGES_FOLDER = new File("test-images");

	private static final String EHLD_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/raster/ehld";

	// Set to true for visual inspection of tests
	// todo: don't forget to set to false before pushing
	private static final boolean save = false;

	// todo: R-HSA-1474165,
	@BeforeClass
	public static void beforeClass() {
		TestUtils.createDir(IMAGES_FOLDER);
	}

	@AfterClass
	public static void afterClass() {
		if (!save) TestUtils.removeDir(IMAGES_FOLDER);
	}

	@Test
	public void testBasicEHLD() {
		final RasterArgs args = new RasterArgs("R-HSA-382551", "png");
		render(args);
	}

	@Test
	public void testFormat() {
		final String[] formats = {"PNg", "JpG", "gif", "JPEG"};
		final String[] stIds = {"R-HSA-449147", "R-HSA-1640170"};
		for (String stId : stIds) {
			for (String format : formats) {
				final RasterArgs args = new RasterArgs(stId, format);
				render(args);
			}
		}
	}

	@Test
	public void testSize() {
		final RasterArgs args = new RasterArgs("R-HSA-109581", "png");
		IntStream.range(1, 11)
				.forEach(quality -> {
					args.setQuality(quality);
					render(args);
				});
	}

	@Test
	public void testSelected() {
		final RasterArgs args = new RasterArgs("R-HSA-109581", "png");
		args.setQuality(8);
		args.setSelected(Collections.singletonList("R-HSA-109606"));
		render(args);
	}

	@Test
	public void testFlags() {
		final RasterArgs args = new RasterArgs("R-HSA-74160", "png");
		args.setSelected(Arrays.asList("R-HSA-109606", "R-HSA-169911"));
		args.setFlags(Collections.singletonList("CTP"));
		render(args);
	}

	@Test
	public void testEnrichment() {
		final RasterArgs args = new RasterArgs("R-HSA-109582", "png");
		args.setToken(TestUtils.performAnalysis("enrichment_data.txt"));
		render(args);
	}

	@Test
	public void testExpression() {
		final RasterArgs args = new RasterArgs("R-HSA-6806667", "gif");
		args.setToken(TestUtils.performAnalysis("expression_data.txt"));
		renderGif(args);
	}

	@Test
	public void testAnimatedGif() {
		final RasterArgs args = new RasterArgs("R-HSA-69278", "gif");
		args.setToken(TestUtils.performAnalysis("expression_data.txt"));
		args.setSelected(Arrays.asList("R-HSA-69242", "R-HSA-68886"));
		args.setProfiles(new ColorProfiles("modern", "copper plus", "cyan"));
		renderGif(args);
	}

	private void render(RasterArgs args) {
		try {
			final EHLDRenderer renderer = new EHLDRenderer(args, EHLD_PATH);
			final BufferedImage image = renderer.render();
			if (save) {
				final String filename = TestUtils.getFileName(args);
				ImageIO.write(image, args.getFormat(), new File(IMAGES_FOLDER, filename));
			}
		} catch (EHLDException | IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	private void renderGif(RasterArgs args) {
		try {
			final EHLDRenderer renderer = new EHLDRenderer(args, EHLD_PATH);
			final File file = new File(IMAGES_FOLDER, TestUtils.getFileName(args));
			final OutputStream os = new FileOutputStream(file);
			renderer.renderToAnimatedGif(os);
		} catch (IOException | EHLDException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}


}
