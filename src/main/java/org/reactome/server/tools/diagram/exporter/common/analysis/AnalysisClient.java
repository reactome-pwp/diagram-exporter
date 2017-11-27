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
import org.reactome.server.tools.diagram.exporter.raster.TraceLogger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Locale;

public class AnalysisClient {

	public static final String SPECIES_SERVICE = "/species/homoSapiens/%s/?pageSize=1&page1";
	private static String SERVER = "http://localhost";
	private static String SERVICE = "/AnalysisService";

	private static ObjectMapper mapper = null;

	static {
		mapper = ObjectMapperProvider.getMapper();
	}

	public static void setServer(String server) {
		SERVER = server;
	}

	public static void setService(String service) {
		SERVICE = service;
	}

	public static FoundElements getFoundElements(String pathway, String token, String resource) throws AnalysisException, AnalysisServerError {
		try {
			long start = System.nanoTime();
			URL url = new URL(SERVER + SERVICE + "/token/" + URLEncoder.encode(token, "UTF8") + "/found/all/" + pathway + "?resource=" + resource);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Response-Type", "application/json");
			switch (connection.getResponseCode()) {
				case 200:
					String json = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
					TraceLogger.trace(String.format(Locale.UK,
							"[analysis service] %10.3f get values (%d chars received)",
							((System.nanoTime() - start) / 1e6), json.length()));
					return getObject(FoundElements.class, json);
				default:
					String error = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
					// This is throwing a DeseralizationException
					throw new AnalysisException(getObject(AnalysisError.class, error));
			}
		} catch (IOException | DeserializationException e) {
//			throw new AnalysisServerError(e.getMessage());
		}
		return null;
	}

	public static PathwaySummary[] getPathwaysSummary(Collection<String> pathways, String token, String resource) throws AnalysisServerError, AnalysisException {
		try {
			final long start = System.nanoTime();
			URL url = new URL(SERVER + SERVICE + "/token/" + token + "/filter/pathways?resource=" + resource);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "text/plain");
			connection.setRequestProperty("Response-Type", "application/json");

			connection.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(String.join(",", pathways));

			switch (connection.getResponseCode()) {
				case 200:
					String json = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
					TraceLogger.trace(String.format(Locale.UK,
							"[analysis service] %10.3f get subpathways (%d chars sent, %d chars received)",
							((System.nanoTime() - start) / 1e6),
							String.join(",", pathways).length(), json.length()));
					return getObject(PathwaySummary[].class, json);
				default:
					String error = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
					throw new AnalysisException(getObject(AnalysisError.class, error));
			}
		} catch (IOException | DeserializationException e) {
			throw new AnalysisServerError(e.getMessage());
		}
	}

	public static AnalysisResult performAnalysis(String sample) throws AnalysisServerError, AnalysisException {
		try {
			URL url = new URL(AnalysisClient.SERVER + SERVICE + "/identifiers/?pageSize=1&page1");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "text/plain");
			connection.setRequestProperty("Response-Type", "application/json");

			connection.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(sample);

			switch (connection.getResponseCode()) {
				case 200:
					String json = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
					return getObject(AnalysisResult.class, json);
				default:
					String error = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
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

	public static AnalysisResult getAnalysisResult(String token) throws AnalysisServerError {
		try {
			final long start = System.nanoTime();
			URL url = new URL(AnalysisClient.SERVER + SERVICE + "/token/" + token + "?pageSize=0&page=1");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "text/plain");
			connection.setRequestProperty("Response-Type", "application/json");

			switch (connection.getResponseCode()) {
				case 200:
					String json = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
					TraceLogger.trace(String.format(Locale.UK,
							"[analysis service] %10.3f get result (%d chars received)",
							((System.nanoTime() - start) / 1e6), json.length()));
					return getObject(AnalysisResult.class, json);
				default:
					String error = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
					throw new AnalysisException(getObject(AnalysisError.class, error));
			}
		} catch (DeserializationException | AnalysisException | IOException e) {
			throw new AnalysisServerError(e.getMessage());
		}
	}

	public static AnalysisResult preformSpeciesComparison(String species) throws AnalysisException, AnalysisServerError {
		try {
			URL url = new URL(AnalysisClient.SERVER + SERVICE + String.format(SPECIES_SERVICE, species));
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Response-Type", "application/json");
			switch (connection.getResponseCode()) {
				case 200:
					String json = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
					return getObject(AnalysisResult.class, json);
				default:
					String error = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
					throw new AnalysisException(getObject(AnalysisError.class, error));
			}
		} catch (IOException | DeserializationException e) {
			throw new AnalysisServerError(e.getMessage());
		}
	}
}
