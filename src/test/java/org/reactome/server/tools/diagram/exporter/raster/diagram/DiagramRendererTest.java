package org.reactome.server.tools.diagram.exporter.raster.diagram;


import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.exporter.common.ResourcesFactory;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.RasterExporter;
import org.reactome.server.tools.diagram.exporter.raster.TestUtils;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class DiagramRendererTest {

	@Test
	public void testSimpleDiagram() {
		// These diagrams contain all the types of nodes:
		// Chemical, Complex, Entity, EntitySet, Gene, ProcessNode, Protein and RNA
		final List<String> stIds = Arrays.asList("R-HSA-5687128", "R-HSA-376176", "R-HSA-69620");
		final List<String> formats = Arrays.asList("PNG", "jpg", "Gif");
		for (String stId : stIds)
			for (String format : formats)
				TestUtils.render(new RasterArgs(stId, format), null);
	}

	@Test
	public void testTooLowQuality() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			final RasterArgs args = new RasterArgs("R-HSA-376176", "jpg");
			args.setQuality(0);
			TestUtils.render(args, null);
		});
	}

	@Test
	public void testTooHighQuality() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			final RasterArgs args = new RasterArgs("R-HSA-376176", "jpg");
			args.setQuality(11);
			TestUtils.render(args, null);
		});
	}

	@Test
	public void testSelectNodes() {
		RasterArgs args = new RasterArgs("R-HSA-5687128", "png");
		// EntitySet, Protein, Chemical
		args.setSelected(Arrays.asList("R-HSA-5692706", "R-HSA-5687026", "R-ALL-29358"));
		TestUtils.render(args, null);

		args = new RasterArgs("R-HSA-376176", "png");
		// Gene, Complex, ProcessNode
		args.setSelected(Arrays.asList("R-HSA-9010561", "R-HSA-428873", "R-HSA-5627117"));
		TestUtils.render(args, null);

		args = new RasterArgs("R-HSA-69620", "png");
		// RNA, Entity
		args.setSelected(Arrays.asList("R-HSA-6803386", "R-ALL-176104"));
		TestUtils.render(args, null);
	}

	@Test
	public void testSelectReactions() {
		RasterArgs args = new RasterArgs("R-HSA-982772", "png");
		// Reactions: Transition, Omitted Process, Uncertain, Association
		// Connectors: filled stop
		args.setSelected(Arrays.asList("R-HSA-1168423", "R-HSA-1168459", "R-HSA-982810", "R-HSA-982775"));
		TestUtils.render(args, null);
		// REPORT: stoichiometry text color is always black in PathwayBrowser

		args = new RasterArgs("R-HSA-877300", "png");
		// Reactions: Association, Dissociation
		// Connectors: empty circle, filled arrow, empty arrow
		args.setSelected(Arrays.asList("R-HSA-877269", "R-HSA-873927"));
		TestUtils.render(args, null);
	}

	@Test
	public void testFlagNodes() {
		RasterArgs args = new RasterArgs("R-HSA-5687128", "png");
		// EntitySet, Protein, Chemical
		args.setFlags(Arrays.asList("R-HSA-5692706", "R-HSA-5687026", "R-ALL-29358"));
		TestUtils.render(args, null);
		// FIXME: chemical flag does not work with name (CTP)

		args = new RasterArgs("R-HSA-376176", "png");
		// Gene, Complex, ProcessNode
		args.setFlags(Arrays.asList("R-HSA-9010561", "R-HSA-428873", "R-HSA-5627117"));
		TestUtils.render(args, null);
		// REPORT: ProcessNode not flagged in PathwayBrowser

		args = new RasterArgs("R-HSA-69620", "png");
		// RNA, Entity
		args.setFlags(Arrays.asList("R-HSA-6803386", "R-ALL-176104"));
		TestUtils.render(args, null);
	}

	@Test
	public void testDiseases() {
		RasterArgs args = new RasterArgs("R-HSA-162587", "png");
		// Entity, RNA, EntitySet, Complex
		args.setSelected(Arrays.asList("R-HIV-165543", "R-HIV-173808", "R-HIV-173120", "R-HSA-167286"));
		args.setFlags(Arrays.asList("R-HIV-165543", "R-HIV-173808", "R-HIV-173120", "R-HSA-167286"));

		TestUtils.render(args, null);
		args = new RasterArgs("R-HSA-5467343", "png");
		// Gene
		args.setSelected(Collections.singletonList("R-HSA-5251547"));
		TestUtils.render(args, null);
		// FIXME: node overlay top-left
	}

	@Test
	public void testDiagramProfiles() {
		final String stId = "R-HSA-5687128";
		final List<String> diagramProfiles = Arrays.asList("Standard", "MODERN", "not valid");
		final List<String> formats = Arrays.asList("png", "jpeg", "gif");
		for (String diagramProfile : diagramProfiles) {
			for (String format : formats) {
				RasterArgs args = new RasterArgs(stId, format);
				args.setProfiles(new ColorProfiles(diagramProfile, null, null));
				TestUtils.render(args, null);
			}
		}
	}

	@Test
	public void testSpeciesComparison() {
		RasterArgs args = new RasterArgs("R-HSA-5687128", "png");
		args.setToken(TestUtils.TOKEN_SPECIES);
		args.setWriteTitle(true);
		TestUtils.render(args, null);
	}

	@Test
	public void testEnrichment() {
		RasterArgs args = new RasterArgs("R-HSA-69620", "png");
		args.setToken(TestUtils.TOKEN_OVER_1);
		args.setWriteTitle(true);
		TestUtils.render(args, null);
	}

	@Test
	public void testExpression() {
		RasterArgs args = new RasterArgs("R-HSA-69620", "png");
		args.setToken(TestUtils.TOKEN_EXPRESSION_1);
		args.setWriteTitle(true);
		TestUtils.render(args, null);
	}

	@Test
	public void testExpressionSelectUnhit() {
		// My favourite diagram had to be here
		final RasterArgs args = new RasterArgs("R-HSA-432047", "gif");
		args.setToken(TestUtils.TOKEN_EXPRESSION_1);
		args.setSelected(Collections.singletonList("R-HSA-432253"));
		TestUtils.render(args, null);
	}

	//@Test
	public void testGSVA() {
		// My favourite diagram had to be here
		final RasterArgs args = new RasterArgs("R-HSA-8935690", "jpeg");
		args.setToken(TestUtils.TOKEN_GSVA);
		args.setSelected(Collections.singletonList("R-HSA-8953390"));
		TestUtils.render(args, null);
	}

	@Test
	public void testAnimatedGif() {
		final ColorProfiles profiles = new ColorProfiles("modern", "copper plus", "teal");
		final RasterArgs args = new RasterArgs("R-HSA-109606", "gif");
		args.setSelected(Collections.singletonList("R-HSA-114255"));
		// takes too long for the HPA analysis
		args.setToken(TestUtils.TOKEN_EXPRESSION_1);
		args.setProfiles(profiles);
		TestUtils.render(args, null);
	}

	@Test
	public void testAnimatedGif2() {
		final ColorProfiles profiles = new ColorProfiles("modern", "copper plus", "teal");
		final RasterArgs args = new RasterArgs("R-HSA-432047", "gif");
		args.setProfiles(profiles);
		args.setSelected(Collections.singleton("R-ALL-879874"));
		args.setToken(TestUtils.TOKEN_EXPRESSION_2);
		TestUtils.render(args, null);
	}

	@Test
	public void testDisease() {
		// EntitySet hiding another EntitySet
		final RasterArgs args = new RasterArgs("R-HSA-5657560", "png");
		args.setSelected(Collections.singletonList("R-HSA-5656438"));
		TestUtils.render(args, null);
	}

	@Test
	public void testDiseaseProcessNodeWithAnalysis() {
		// This could be the definition of a corner case
		final RasterArgs args = new RasterArgs("R-HSA-1643713", "png");
		args.setToken(TestUtils.TOKEN_EXPRESSION_2);
		args.setSelected(Collections.singletonList("R-HSA-5637815"));
		TestUtils.render(args, null);
		// report: processNodes have no outer red border when hit by analysis
	}

	@Test
	public void testDecoratedFadeout() {
		// Fadeout elements can't be decorated (selected, haloed, flagged)
		final RasterArgs args = new RasterArgs("R-HSA-5683371", "jpg");
		args.setSelected(Arrays.asList("29356", "71185"));
		TestUtils.render(args, null);
	}

	@Test
	public void testWithAnalysisResult() {
		final AnalysisStoredResult result = TestUtils.getResult(TestUtils.TOKEN_OVER_1);
		final RasterArgs args = new RasterArgs("R-HSA-1643713", "png");
		TestUtils.render(args, result);
	}

	@Test
	public void testEncapsulatedPathways() {
		final RasterArgs args = new RasterArgs("R-HSA-168164", "png");
		final AnalysisStoredResult result = TestUtils.getResult(TestUtils.TOKEN_OVER_1);
		TestUtils.render(args, result);
	}

	@Test
	public void testSubpathwaySeveralTimes() {
		final RasterArgs args = new RasterArgs("R-HSA-373076", "png");
		final AnalysisStoredResult result = TestUtils.getResult(TestUtils.TOKEN_OVER_1);
		TestUtils.render(args, result);
	}

	@Test
	public void testChemicalDrug() {
		final RasterArgs args = new RasterArgs("R-HSA-2894858", "png");
		args.setQuality(10);
		args.setSelected(Collections.singleton("113582"));
		TestUtils.render(args, null);
	}

	@Test
	public void testEventSelection() {
		final RasterArgs args = new RasterArgs("R-HSA-2682334", "png");
		args.setSelected(Collections.singleton("R-HSA-3928663"));
		TestUtils.render(args, null);
	}

	@Test
	public void testParallelism() {
		// Batik is not thread safe. In this case, we convert a regular diagram
		// with analysis to SVG, invoking batik for that.
		final AnalysisStoredResult result = TestUtils.getResult(TestUtils.TOKEN_OVER_1);
		final RasterArgs args = new RasterArgs("R-HSA-376176", "svg");
		IntStream.range(0, 20).parallel()
				.forEach(value -> TestUtils.render(args, result));
	}

	@Test
	public void testPdf() {
		final RasterArgs args = new RasterArgs("R-HSA-376176", "pdf");
		TestUtils.render(args, null);
	}

	@Test
	public void testPdfGradient() {
		// The 3-color gradient does not show properly
		final RasterArgs args = new RasterArgs("R-HSA-376176", "pdf")
				.setToken(TestUtils.TOKEN_EXPRESSION_1)
				.setProfiles(new ColorProfiles("modern", "copper plus", null));
		TestUtils.render(args);
	}

	@Test
	public void testMargin() {
		// Negative minX and minY
		RasterArgs args = new RasterArgs("R-HSA-112310", "png")
				.setMargin(25)
				.setQuality(8);
		TestUtils.render(args);
		// Positive
		args = new RasterArgs("R-HSA-166058", "svg")
				.setMargin(-45)
				.setQuality(6)
				.setToken(TestUtils.TOKEN_EXPRESSION_1);
		TestUtils.render(args);
	}

	@Test
	public void testUseEhld() {
		final RasterArgs args = new RasterArgs("R-HSA-1500931", "png")
				.setEhld(false);
		TestUtils.render(args);
	}

	@Test
	public void fromDiagramObject() {
		final String DIAGRAM_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/diagram";
		try {
			final String stId = "R-HSA-432047";
			final Diagram diagram = ResourcesFactory.getDiagram(DIAGRAM_PATH, stId);
			final Graph graph = ResourcesFactory.getGraph(DIAGRAM_PATH, stId);
			new RasterExporter().export(diagram, graph, new RasterArgs("png"), null, NullOutputStream.NULL_OUTPUT_STREAM);
		} catch (DiagramJsonDeserializationException | DiagramJsonNotFoundException | TranscoderException | AnalysisException | IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSelectionAndFlagging() {
		// R-HSA-5205682.svg?sel=R-HSA-1267988,992745&flg=MFN2
		final RasterArgs args = new RasterArgs("R-HSA-5205647", "png")
				.setSelected(Arrays.asList("R-HSA-1267988", "992745", "R-HSA-5205682"))
				.setFlags(Arrays.asList(" R-HSA-201579", "R-HSA-992714", "R-HSA-5205668", "R-HSA-5205659", "R-HSA-5205670", "R-HSA-5205648"));
		TestUtils.render(args);
	}
}
