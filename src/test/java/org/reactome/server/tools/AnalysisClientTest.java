package org.reactome.server.tools;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisClient;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisResult;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.FoundElements;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.PathwaySummary;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.ResourceSummary;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AnalysisClientTest {

    private static String token;
    private static String resource;

    @BeforeClass
    public static void initialiseTest(){
        AnalysisClient.setServer("http://reactomedev.oicr.on.ca");
        try {
            String sample = IOUtils.toString(AnalysisClientTest.class.getResourceAsStream("GBM_Uniprot.txt"));
            AnalysisResult result = AnalysisClient.performAnalysis(sample);
            token = result.getSummary().getToken();

            List<ResourceSummary> resourceSummary = result.getResourceSummary();
            ResourceSummary summary = resourceSummary.size() == 2 ? resourceSummary.get(1) : resourceSummary.get(0);
            resource = summary.getResource();
        } catch (IOException | AnalysisServerError e) {
            Assert.fail(e.getMessage());
        } catch (AnalysisException e) {
            Assert.fail(e.getError().getReason());
        }
    }

    @Test
    public void getAnalysisResultTest(){
        try {
            FoundElements foundElements = AnalysisClient.getFoundElements("R-HSA-1257604", token, resource);
            Assert.assertTrue("Results expected for R-HSA-1257604", foundElements.getFoundEntities() > 0);
        } catch (AnalysisException e) {
            Assert.fail(e.getError().getReason());
        } catch (AnalysisServerError e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void getPathwaysSummaryTest(){
        try {
            List<String> pathways = Arrays.asList("R-HSA-1257604", "R-HSA-199418");
            PathwaySummary[] summary = AnalysisClient.getPathwaysSummary(pathways, token, resource);
            Assert.assertTrue("Summaries expected for R-HSA-1257604", summary.length > 0);
        } catch (AnalysisException e) {
            Assert.fail(e.getError().getReason());
        } catch (AnalysisServerError e) {
            Assert.fail(e.getMessage());
        }
    }
}
