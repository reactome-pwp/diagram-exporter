package org.reactome.server.tools.diagram.exporter.raster;

import org.apache.batik.transcoder.TranscoderException;
import org.junit.Test;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EhldException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class NodeRendererTest {

	private static final List<String> SCHEMA_CLASSES = Arrays.asList(
			"EntitySet",
			"Protein",
			"Chemical"
// "Gene", "Complex", "ProcessNode", "RNA",
//			"Entity", "EncapsulatedPathway"
	);
	private static final String ANALYSIS_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/analysis";
	private static final String DIAGRAM_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/nodes";
	private static final String EHLD_PATH = "src/test/resources/org/reactome/server/tools/diagram/exporter/ehld";
	private static final String SVG_SUMMARY = "src/test/resources/org/reactome/server/tools/diagram/exporter/svgsummary.txt";


	@Test
	public void test() {
		final RasterExporter exporter = new RasterExporter(DIAGRAM_PATH, EHLD_PATH, ANALYSIS_PATH, SVG_SUMMARY);
		SCHEMA_CLASSES.forEach(schemaClass -> {
//			final InputStream resource = NodeRendererTest.class.getResourceAsStream(s + ".json");
			Stream.of(true, false).forEach(select ->
					Stream.of(true, false).forEach(flag -> {
						final RasterArgs args = new RasterArgs(schemaClass, "png");
						args.setQuality(10);
						if (select)
							args.setSelected(Collections.singleton("2"));
						if (flag)
							args.setFlags(Collections.singleton("2"));
						final String name = schemaClass
								+ (select ? "_s" : "")
								+ (flag ? "_f" : "");
						try (FileOutputStream outputStream = new FileOutputStream(new File(name + ".png"))) {
							exporter.export(args, outputStream);
						} catch (IOException | TranscoderException | DiagramJsonDeserializationException | DiagramJsonNotFoundException | AnalysisException | EhldException e) {
							e.printStackTrace();
						}
					}));


		});
	}


}
