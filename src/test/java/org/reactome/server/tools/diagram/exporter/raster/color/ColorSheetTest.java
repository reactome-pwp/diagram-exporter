package org.reactome.server.tools.diagram.exporter.raster.color;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ColorSheetTest {

	@Test
	public void testNodeColorSheet() {
		final String nodeSheet = "{" +
				"\"fill\": \"rgba(235, 178, 121, 0.5)\"," +
				"\"fadeOutFill\": \"rgba(242, 203, 165, 0.5)\"," +
				"\"lighterFill\": \"rgba(242, 203, 165, 0.5)\"," +
				"\"stroke\": \"#FF8637\"," +
				"\"fadeOutStroke\": \"#FF8637\"," +
				"\"lighterStroke\": \"#FF8637\"," +
				"\"lineWidth\": \"0.5\"," +
				"\"text\": \"#0000FF\"," +
				"\"fadeOutText\": \"#000000\"," +
				"\"lighterText\": \"#000000\"" +
				" }";
		ObjectMapper mapper = new ObjectMapper();
		try {
			final NodeColorSheet sheet = mapper.readValue(nodeSheet, NodeColorSheetImpl.class);
			Assert.assertEquals(new Color(235, 178, 121, 127), sheet.getFill());
			Assert.assertEquals(new Color(242, 203, 165, 127), sheet.getFadeOutFill());
			Assert.assertEquals(new Color(242, 203, 165, 127), sheet.getLighterFill());
			Assert.assertEquals(new Color(255, 134, 55), sheet.getStroke());
			Assert.assertEquals(new Color(255, 134, 55), sheet.getFadeOutStroke());
			Assert.assertEquals(new Color(255, 134, 55), sheet.getLighterStroke());
			Assert.assertEquals(new Color(0, 0, 255), sheet.getText());
			Assert.assertEquals(new Color(0, 0, 0), sheet.getFadeOutText());
			Assert.assertEquals(new Color(0, 0, 0), sheet.getLighterText());
			Assert.assertEquals(1e-2, 0.5, sheet.getLineWidth());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testThumbnailSheet() {
		final String sheet = "{" +
				"\"node\": \"#FF7700\"," +
				"\"edge\": \"#FFFFFF\"," +
				"\"hovering\": \"#000011\"," +
				"\"highlight\": \"#FF7700\"," +
				"\"selection\": \"#FF5A00\"" +
				"}";
		final ObjectMapper mapper = new ObjectMapper();
		try {
			final ThumbnailColorSheet thumbnailSheet = mapper.readValue(sheet, ThumbnailColorSheetIml.class);
			Assert.assertEquals(new Color(255, 119, 0), thumbnailSheet.getNode());
			Assert.assertEquals(new Color(255, 255, 255), thumbnailSheet.getEdge());
			Assert.assertEquals(new Color(0, 0, 17), thumbnailSheet.getHovering());
			Assert.assertEquals(new Color(255, 119, 0), thumbnailSheet.getHighlight());
			Assert.assertEquals(new Color(255, 90, 0), thumbnailSheet.getSelection());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testPropertiesSheet() {
		final String json = "{" +
				"\"hovering\": \"#DFDF00\"," +
				"\"highlight\": \"#DFDF00\"," +
				"\"selection\": \"#4D4DFF\"," +
				"\"halo\": \"#E2B066\"," +
				"\"flag\": \"#FF00FF\"," +
				"\"trigger\": \"#6666FF\"," +
				"\"text\": \"#000000\"," +
				"\"button\": \"#00FF00\"," +
				"\"disease\": \"#FF0000\"" +
				"}";
		final ObjectMapper mapper = new ObjectMapper();
		try {
			final PropertiesColorSheet propertiesSheet = mapper.readValue(json, PropertiesColorSheetImpl.class);
			Assert.assertEquals(new Color(223, 223, 0), propertiesSheet.getHovering());
			Assert.assertEquals(new Color(223, 223, 0), propertiesSheet.getHighlight());
			Assert.assertEquals(new Color(77, 77, 255), propertiesSheet.getSelection());
			Assert.assertEquals(new Color(226, 176, 102), propertiesSheet.getHalo());
			Assert.assertEquals(new Color(255, 0, 255), propertiesSheet.getFlag());
			Assert.assertEquals(new Color(102, 102, 255), propertiesSheet.getTrigger());
			Assert.assertEquals(new Color(0, 0, 0), propertiesSheet.getText());
			Assert.assertEquals(new Color(0, 255, 0), propertiesSheet.getButton());
			Assert.assertEquals(new Color(255, 0, 0), propertiesSheet.getDisease());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testDiagramSheet() {
		try {
			final InputStream resource = getClass().getResourceAsStream("diagram.json");
			final String json = IOUtils.toString(resource, Charset.defaultCharset());
			final ObjectMapper mapper = new ObjectMapper();
			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
			final DiagramSheet diagramSheet = mapper.readValue(json, DiagramSheetImpl.class);
			Assert.assertEquals("Modern", diagramSheet.getName());
			Assert.assertNotNull(diagramSheet.getProperties());
			Assert.assertNotNull(diagramSheet.getAttachment());
			Assert.assertNotNull(diagramSheet.getChemical());
			Assert.assertNotNull(diagramSheet.getCompartment());
			Assert.assertNotNull(diagramSheet.getComplex());
			Assert.assertNotNull(diagramSheet.getEntity());
			Assert.assertNotNull(diagramSheet.getEntitySet());
			Assert.assertNotNull(diagramSheet.getFlowLine());
			Assert.assertNotNull(diagramSheet.getGene());
			Assert.assertNotNull(diagramSheet.getInteractor());
			Assert.assertNotNull(diagramSheet.getLink());
			Assert.assertNotNull(diagramSheet.getNote());
			Assert.assertNotNull(diagramSheet.getOtherEntity());
			Assert.assertNotNull(diagramSheet.getProcessNode());
			Assert.assertNotNull(diagramSheet.getProtein());
			Assert.assertNotNull(diagramSheet.getReaction());
			Assert.assertNotNull(diagramSheet.getRna());
			Assert.assertNotNull(diagramSheet.getStoichiometry());
			Assert.assertNotNull(diagramSheet.getThumbnail());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGradient() {
		final String json = "{" +
				"\"min\": \"#FFFFCA\"," +
				"\"stop\": null," +
				"\"max\": \"#FFFF50\"" +
				"}";
		final ObjectMapper mapper = new ObjectMapper();
		try {
			final GradientSheet gradientSheet = mapper.readValue(json, GradientSheetImpl.class);
			Assert.assertEquals(new Color(255, 255, 202), gradientSheet.getMin());
			Assert.assertNull(gradientSheet.getStop());
			Assert.assertEquals(new Color(255, 255, 80), gradientSheet.getMax());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testEnrichment() {
		final String json = "{" +
				"\"gradient\": {" +
				" \"min\": \"#FFFFCA\"," +
				" \"stop\": null," +
				" \"max\": \"#FFFF50\"" +
				"}," +
				"\"text\": \"#000000\"" +
				"}";
		final ObjectMapper mapper = new ObjectMapper();
		try {
			final EnrichmentSheet enrichmentSheet = mapper.readValue(json, EnrichmentSheetImpl.class);
			Assert.assertEquals(new Color(0, 0, 0), enrichmentSheet.getText());
			Assert.assertNotNull(enrichmentSheet.getGradient());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testLegend() {
		final String json = "{" +
				"\"median\": \"#000000\"," +
				"\"hover\": \"#FF0000\"" +
				"}";
		final ObjectMapper mapper = new ObjectMapper();
		try {
			final LegendSheet legendSheet = mapper.readValue(json, LegendSheetImpl.class);
			Assert.assertEquals(new Color(0, 0, 0), legendSheet.getMedian());
			Assert.assertEquals(new Color(255, 0, 0), legendSheet.getHover());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testExpression() {
		final String json = "{" +
				"\"gradient\": {" +
				" \"min\": \"#D65C33\"," +
				" \"stop\": \"#FFFF4D\"," +
				" \"max\": \"#85AD33\"" +
				"}," +
				"\"legend\": {" +
				" \"median\": \"#000000\"," +
				" \"hover\": \"#FF0000\"" +
				"}," +
				"\"text\": \"#FFFFFF\"" +
				"}";
		final ObjectMapper mapper = new ObjectMapper();
		try {
			final ExpressionSheetImpl expressionSheet = mapper.readValue(json, ExpressionSheetImpl.class);
			Assert.assertEquals(new Color(255, 255, 255), expressionSheet.getText());
			Assert.assertNotNull(expressionSheet.getGradient());
			Assert.assertNotNull(expressionSheet.getLegend());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testAnalysisDiagram() {
		try {
			final InputStream resource = getClass().getResourceAsStream("analysis.json");
			final String json = IOUtils.toString(resource, Charset.defaultCharset());
			AnalysisSheet analysisSheet = new ObjectMapper().readValue(json, AnalysisSheetImpl.class);
			Assert.assertEquals("Copper Plus", analysisSheet.getName());
			Assert.assertEquals(new Color(251, 140, 0), analysisSheet.getRibbon());
			Assert.assertNotNull(analysisSheet.getEnrichment());
			Assert.assertNotNull(analysisSheet.getExpression());
		} catch (IOException e) {
//			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

}
