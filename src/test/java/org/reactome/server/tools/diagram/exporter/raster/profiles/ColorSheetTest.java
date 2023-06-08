package org.reactome.server.tools.diagram.exporter.raster.profiles;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactome.server.tools.diagram.exporter.BaseTest;


import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ColorSheetTest extends BaseTest {

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
			final NodeColorSheet sheet = mapper.readValue(nodeSheet, NodeColorSheet.class);
			Assertions.assertEquals(new Color(235, 178, 121, 127), sheet.getFill());
			Assertions.assertEquals(new Color(242, 203, 165, 127), sheet.getFadeOutFill());
			Assertions.assertEquals(new Color(242, 203, 165, 127), sheet.getLighterFill());
			Assertions.assertEquals(new Color(255, 134, 55), sheet.getStroke());
			Assertions.assertEquals(new Color(255, 134, 55), sheet.getFadeOutStroke());
			Assertions.assertEquals(new Color(255, 134, 55), sheet.getLighterStroke());
			Assertions.assertEquals(new Color(0, 0, 255), sheet.getText());
			Assertions.assertEquals(new Color(0, 0, 0), sheet.getFadeOutText());
			Assertions.assertEquals(new Color(0, 0, 0), sheet.getLighterText());
			Assertions.assertEquals(1e-2, 0.5, sheet.getLineWidth());
		} catch (IOException e) {
			Assertions.fail(e.getMessage());
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
			final ThumbnailColorSheet thumbnailSheet = mapper.readValue(sheet, ThumbnailColorSheet.class);
			Assertions.assertEquals(new Color(255, 119, 0), thumbnailSheet.getNode());
			Assertions.assertEquals(new Color(255, 255, 255), thumbnailSheet.getEdge());
			Assertions.assertEquals(new Color(0, 0, 17), thumbnailSheet.getHovering());
			Assertions.assertEquals(new Color(255, 119, 0), thumbnailSheet.getHighlight());
			Assertions.assertEquals(new Color(255, 90, 0), thumbnailSheet.getSelection());
		} catch (IOException e) {
			Assertions.fail(e.getMessage());
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
			final PropertiesColorSheet propertiesSheet = mapper.readValue(json, PropertiesColorSheet.class);
			Assertions.assertEquals(new Color(223, 223, 0), propertiesSheet.getHovering());
			Assertions.assertEquals(new Color(223, 223, 0), propertiesSheet.getHighlight());
			Assertions.assertEquals(new Color(77, 77, 255), propertiesSheet.getSelection());
			Assertions.assertEquals(new Color(226, 176, 102), propertiesSheet.getHalo());
			Assertions.assertEquals(new Color(255, 0, 255), propertiesSheet.getFlag());
			Assertions.assertEquals(new Color(102, 102, 255), propertiesSheet.getTrigger());
			Assertions.assertEquals(new Color(0, 0, 0), propertiesSheet.getText());
			Assertions.assertEquals(new Color(0, 255, 0), propertiesSheet.getButton());
			Assertions.assertEquals(new Color(255, 0, 0), propertiesSheet.getDisease());
		} catch (IOException e) {
			Assertions.fail(e.getMessage());
		}
	}

	@Test
	public void testDiagramSheet() {
		try {
			final InputStream resource = getClass().getResourceAsStream("diagram.json");
			final String json = IOUtils.toString(resource, Charset.defaultCharset());
			final ObjectMapper mapper = new ObjectMapper();
			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
			final DiagramSheet diagramSheet = mapper.readValue(json, DiagramSheet.class);
			Assertions.assertEquals("Modern", diagramSheet.getName());
			Assertions.assertNotNull(diagramSheet.getProperties());
			Assertions.assertNotNull(diagramSheet.getAttachment());
			Assertions.assertNotNull(diagramSheet.getChemical());
			Assertions.assertNotNull(diagramSheet.getCompartment());
			Assertions.assertNotNull(diagramSheet.getComplex());
			Assertions.assertNotNull(diagramSheet.getEntity());
			Assertions.assertNotNull(diagramSheet.getEntitySet());
			Assertions.assertNotNull(diagramSheet.getFlowLine());
			Assertions.assertNotNull(diagramSheet.getGene());
			Assertions.assertNotNull(diagramSheet.getInteractor());
			Assertions.assertNotNull(diagramSheet.getLink());
			Assertions.assertNotNull(diagramSheet.getNote());
			Assertions.assertNotNull(diagramSheet.getOtherEntity());
			Assertions.assertNotNull(diagramSheet.getProcessNode());
			Assertions.assertNotNull(diagramSheet.getProtein());
			Assertions.assertNotNull(diagramSheet.getReaction());
			Assertions.assertNotNull(diagramSheet.getRna());
			Assertions.assertNotNull(diagramSheet.getStoichiometry());
			Assertions.assertNotNull(diagramSheet.getThumbnail());
		} catch (IOException e) {
			Assertions.fail(e.getMessage());
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
			final GradientSheet gradientSheet = mapper.readValue(json, GradientSheet.class);
			Assertions.assertEquals(new Color(255, 255, 202), gradientSheet.getMin());
			Assertions.assertNull(gradientSheet.getStop());
			Assertions.assertEquals(new Color(255, 255, 80), gradientSheet.getMax());
		} catch (IOException e) {
			Assertions.fail(e.getMessage());
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
			final EnrichmentSheet enrichmentSheet = mapper.readValue(json, EnrichmentSheet.class);
			Assertions.assertEquals(new Color(0, 0, 0), enrichmentSheet.getText());
			Assertions.assertNotNull(enrichmentSheet.getGradient());
		} catch (IOException e) {
			Assertions.fail(e.getMessage());
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
			final LegendSheet legendSheet = mapper.readValue(json, LegendSheet.class);
			Assertions.assertEquals(new Color(0, 0, 0), legendSheet.getMedian());
			Assertions.assertEquals(new Color(255, 0, 0), legendSheet.getHover());
		} catch (IOException e) {
			Assertions.fail(e.getMessage());
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
			final ExpressionSheet expressionSheet = mapper.readValue(json, ExpressionSheet.class);
			Assertions.assertEquals(new Color(255, 255, 255), expressionSheet.getText());
			Assertions.assertNotNull(expressionSheet.getGradient());
			Assertions.assertNotNull(expressionSheet.getLegend());
		} catch (IOException e) {
			Assertions.fail(e.getMessage());
		}
	}

	@Test
	public void testAnalysisDiagram() {
		try {
			final InputStream resource = getClass().getResourceAsStream("analysis.json");
			final String json = IOUtils.toString(resource, Charset.defaultCharset());
			AnalysisSheet analysisSheet = new ObjectMapper().readValue(json, AnalysisSheet.class);
			Assertions.assertEquals("Copper Plus", analysisSheet.getName());
			Assertions.assertEquals(new Color(251, 140, 0), analysisSheet.getRibbon());
			Assertions.assertNotNull(analysisSheet.getEnrichment());
			Assertions.assertNotNull(analysisSheet.getExpression());
		} catch (IOException e) {
//			e.printStackTrace();
			Assertions.fail(e.getMessage());
		}
	}

	@Test
	public void testInteractorsSheet() {
		try {
			final InputStream resource = getClass().getResourceAsStream("interactors.json");
			final String json = IOUtils.toString(resource, Charset.defaultCharset());
			InteractorsSheet interactorsSheet = new ObjectMapper().readValue(json, InteractorsSheet.class);
			Assertions.assertEquals("Cyan", interactorsSheet.getName());
			Assertions.assertNotNull(interactorsSheet.getChemical());
			Assertions.assertNotNull(interactorsSheet.getProtein());
		} catch (IOException e) {
//			e.printStackTrace();
			Assertions.fail(e.getMessage());
		}
	}

}