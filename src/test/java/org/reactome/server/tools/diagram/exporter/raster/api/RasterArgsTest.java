package org.reactome.server.tools.diagram.exporter.raster.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;

public class RasterArgsTest {

	@Test
	public void testFull() {
		final InputStream resource = getClass().getResourceAsStream("complete.json");
		final ObjectMapper mapper = new ObjectMapper();
		try {
			final RasterArgs args = mapper.readValue(resource, JsonRasterArgs.class);
			Assert.assertEquals("R-HSA-12345", args.getStId());
			Assert.assertEquals("png", args.getFormat());
			Assert.assertEquals(3, args.getFactor(), 0.01);
			Assert.assertNull(args.getToken());
			Assert.assertNotNull(args.getFlags());
			Assert.assertNotNull(args.getSelected());
			Assert.assertNotNull(args.getProfiles());
			Assert.assertEquals(new Color(255, 255, 255), args.getBackground());
			Assert.assertEquals(new HashSet<>(Arrays.asList("3", "4", "5", "R-HSA-12")), args.getSelected());
			Assert.assertEquals(new Integer(1), args.getColumn());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testMinimum() {
		final InputStream resource = getClass().getResourceAsStream("basic.json");
		try {
			final RasterArgs args = new ObjectMapper().readValue(resource, JsonRasterArgs.class);
			Assert.assertEquals("R-MMU-12345", args.getStId());
			Assert.assertEquals("jpg", args.getFormat());
			Assert.assertEquals(1, args.getFactor(), 0.01);
			Assert.assertNotNull(args.getProfiles());
			Assert.assertNull(args.getToken());
			Assert.assertNull(args.getSelected());
			Assert.assertNull(args.getFlags());
			Assert.assertNull(args.getBackground());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
