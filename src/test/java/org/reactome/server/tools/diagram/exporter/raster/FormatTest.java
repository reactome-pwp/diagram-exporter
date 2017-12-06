package org.reactome.server.tools.diagram.exporter.raster;

import org.junit.Assert;
import org.junit.Test;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class FormatTest {


	@Test
	public void testExpressionFormat() {
		final DecimalFormat LEGEND_FORMAT = new DecimalFormat("#.##E0", DecimalFormatSymbols.getInstance(Locale.UK));
//		final String formatter = "%4.2G";
		Assert.assertEquals("2E-3", LEGEND_FORMAT.format(0.002));
		Assert.assertEquals("1E1", LEGEND_FORMAT.format(10));
		Assert.assertEquals("1.01E1", LEGEND_FORMAT.format(10.123));
		Assert.assertEquals("1.01E1", LEGEND_FORMAT.format(10.123));
		Assert.assertEquals("1E4", LEGEND_FORMAT.format(10000.126));
	}

	@Test
	public void testEnrichmentFormat() {
		final DecimalFormat FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.UK));
		Assert.assertEquals("0", FORMAT.format(0));
		Assert.assertEquals("0.05", FORMAT.format(0.05));
	}


	@Test
	public void testLegendFormat() {
		final double max = 2.9;
		final DecimalFormat NF = new DecimalFormat("#.##E0", DecimalFormatSymbols.getInstance(Locale.UK));
		Assert.assertEquals("2.9E0", NF.format(max));
	}


}
