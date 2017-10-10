package org.reactome.server.tools.diagram.exporter.common.analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.data.exception.DeserializationException;
import org.reactome.server.tools.diagram.data.mapper.ObjectMapperProvider;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisError;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisResult;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.FoundElements;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.PathwaySummary;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;

public class AnalysisClient {

    private static String SERVER = "http://localhost";

    private static ObjectMapper mapper = null;

    static {
        mapper = ObjectMapperProvider.getMapper();
    }

    public static void setServer(String server){
        SERVER = server;
    }

    public static FoundElements getFoundElements(String pathway, String token, String resource) throws AnalysisException, AnalysisServerError {
        try {
            URL url = new URL(SERVER + "/AnalysisService/token/" + URLEncoder.encode(token, "UTF8") + "/found/all/" + pathway + "?resource=" + resource);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Response-Type","application/json");

            switch (connection.getResponseCode()){
                case 200:
                    String json = IOUtils.toString(connection.getInputStream());
                    return getObject(FoundElements.class, json);
                default:
                    String error = IOUtils.toString(connection.getErrorStream());
                    throw new AnalysisException(getObject(AnalysisError.class, error));
            }
        } catch (IOException | DeserializationException e) {
            throw new AnalysisServerError(e.getMessage());
        }
    }

    public static PathwaySummary[] getPathwaysSummary(Collection<String> pathways, String token, String resource) throws AnalysisServerError, AnalysisException {
        try {
            URL url = new URL(SERVER + "/AnalysisService/token/" + token + "/filter/pathways?resource=" + resource);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","text/plain");
            connection.setRequestProperty("Response-Type","application/json");

            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(String.join(",", pathways));

            switch (connection.getResponseCode()){
                case 200:
                    String json = IOUtils.toString(connection.getInputStream());
                    return getObject(PathwaySummary[].class, json);
                default:
                    String error = IOUtils.toString(connection.getErrorStream());
                    throw new AnalysisException(getObject(AnalysisError.class, error));
            }
        } catch (IOException | DeserializationException e) {
            throw new AnalysisServerError(e.getMessage());
        }
    }

    public static AnalysisResult performAnalysis(String sample) throws AnalysisServerError, AnalysisException {
        try {
            URL url = new URL(AnalysisClient.SERVER + "/AnalysisService/identifiers/?pageSize=1&page1");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","text/plain");
            connection.setRequestProperty("Response-Type","application/json");

            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(sample);

            switch (connection.getResponseCode()){
                case 200:
                    String json = IOUtils.toString(connection.getInputStream());
                    return getObject(AnalysisResult.class, json);
                default:
                    String error = IOUtils.toString(connection.getErrorStream());
                    throw new AnalysisException(getObject(AnalysisError.class, error));
            }
        } catch (IOException | DeserializationException e) {
            throw new AnalysisServerError(e.getMessage());
        }
    }

    private static <T> T getObject(Class<T> cls, String json) throws DeserializationException {
        try {
            return mapper.readValue(json, cls);
        } catch (Throwable e) {
            throw new DeserializationException("Error mapping json string for [" + cls + "]: " + json, e);
        }
    }
}
