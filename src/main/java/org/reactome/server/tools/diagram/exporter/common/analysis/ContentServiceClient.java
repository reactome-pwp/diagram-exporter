package org.reactome.server.tools.diagram.exporter.common.analysis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.data.exception.DeserializationException;
import org.reactome.server.tools.diagram.exporter.common.analysis.content.ContentServiceResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

public class ContentServiceClient {

	private static String HOST = "http://localhost";
	private static String SERVICE = "/ContentService/";
	private static final String QUERY = "data/diagram/%s/entities/%s?pathways=%s";
	private static ObjectMapper mapper = new ObjectMapper();

	public static void setHost(String host) {
		ContentServiceClient.HOST = host;
	}

	public static void setService(String service) {
		ContentServiceClient.SERVICE = service;
	}

	public static List<ContentServiceResponse> getFlagged(String term, String stId, Collection<String> pathways) {
		try {
			final URL url = new URL(HOST + SERVICE + String.format(QUERY, stId, term, String.join(",", pathways)));

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Response-Type", "application/json");
			switch (connection.getResponseCode()) {
				case 200:
					String json = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
					return getObject(new TypeReference<List<ContentServiceResponse>>() {
					}, json);
				default:
					String error = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
					// This is throwing a DeseralizationException
//					throw new AnalysisException(getObject(AnalysisError.class, error));
			}

		} catch (DeserializationException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static <T> T getObject(TypeReference<List<ContentServiceResponse>> cls, String json) throws DeserializationException {
		try {
			return mapper.readValue(json, cls);
		} catch (Throwable e) {
			throw new DeserializationException("Error mapping json string for [" + cls + "]: " + json, e);
		}
	}

}
