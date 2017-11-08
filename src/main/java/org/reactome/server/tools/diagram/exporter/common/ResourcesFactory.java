package org.reactome.server.tools.diagram.exporter.common;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.data.DiagramFactory;
import org.reactome.server.tools.diagram.data.exception.DeserializationException;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDException;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDMalformedException;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ProfileResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGDocument;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides access to project resources: diagrams, graphs and color profiles.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ResourcesFactory {

	private static final Logger logger = LoggerFactory.getLogger("infoLogger");

	private static final String DIAGRAM_SERVICE = "/download/current/diagram/";
	private static final String EHLD_SERVICE = "/download/current/ehld/";

	private static final String DEFAULT_DIAGRAM_PROFILE = "modern";
	private static final SAXSVGDocumentFactory DOCUMENT_FACTORY = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
	private static String host = "http://localhost";

	/**
	 * Set the host. By default is http://localhost
	 *
	 * @param host new host
	 */
	public static void setHost(String host) {
		ResourcesFactory.host = host;
	}

	/**
	 * Loads into memory the DiagramProfile corresponding to getName profile. If
	 * getName is null or there is no profile with getName, a default profile is
	 * returned.
	 *
	 * @param name getName of profile. null to take default
	 *
	 * @return the DiagramProfile for getName, or a default one
	 *
	 * @throws DiagramProfileException             when there is no file
	 *                                             associated to getName,
	 *                                             neither a default diagram
	 * @throws DiagramJsonDeserializationException when profile file is not well
	 *                                             formed
	 */
	public static DiagramProfile getDiagramProfile(String name) throws DiagramProfileException, DiagramJsonDeserializationException {
		if (name == null)
			name = DEFAULT_DIAGRAM_PROFILE;
		logger.info("Getting Profile [{}]", name);
		final String file = "diagram_" + name.toLowerCase() + ".json";
		InputStream resource = ProfileResources.class.getResourceAsStream(file);
		if (resource == null)
			resource = ProfileResources.class.getResourceAsStream(DEFAULT_DIAGRAM_PROFILE);
		try {
			if (resource == null) {
				logger.error("Could not read diagram color profile {}", name);
				throw new DiagramProfileException("Could not read diagram color profile " + name);
			}
			final String json = IOUtils.toString(resource);
			return DiagramFactory.getProfile(json);
		} catch (DeserializationException e) {
			logger.error("Could not deserialize diagram color profile {}", name);
			throw new DiagramJsonDeserializationException("Could not deserialize diagram color profile " + name);
		} catch (IOException e) {
			logger.error("Could not read diagram color profile {}", name);
			throw new DiagramProfileException("Could not read diagram color profile " + name);
		}
	}

	/**
	 * Creates a new Diagram.
	 *
	 * @param diagramPath path where the {stId}.json file is located
	 * @param stId        stable identifier of the diagram
	 *
	 * @return the Diagram of stId
	 *
	 * @throws DiagramJsonDeserializationException if the .json file has a bad
	 *                                             format
	 * @throws DiagramJsonNotFoundException        if the stId has no associated
	 *                                             .json file
	 */
	public static Diagram getDiagram(String diagramPath, String stId) throws DiagramJsonDeserializationException, DiagramJsonNotFoundException {
		// If not specified, use http protocol
		if (diagramPath == null)
			return getDiagram(stId);
		final Path pathway = Paths.get(diagramPath, stId + ".json");
		logger.info("Getting diagram JSON {}", pathway);
		try {
			String json = new String(Files.readAllBytes(pathway));
			return DiagramFactory.getDiagram(json);
		} catch (DeserializationException e) {
			logger.error("Could not deserialize diagram json for pathway {}", pathway);
			throw new DiagramJsonDeserializationException("Could not deserialize diagram json for pathway " + pathway);
		} catch (IOException e) {
			logger.error("Could not read diagram json for pathway {}", pathway);
			throw new DiagramJsonNotFoundException("Could not read diagram json for pathway " + pathway);
		}
	}

	/**
	 * Creates a new Graph.
	 *
	 * @param diagramPath path where the {stId}.graph.json file is located
	 * @param stId        stable identifier of the diagram
	 *
	 * @return the Diagram of stId
	 *
	 * @throws DiagramJsonDeserializationException if the .graph.json file has a
	 *                                             bad format
	 * @throws DiagramJsonNotFoundException        if the stId has no associated
	 *                                             .graph.json file
	 */
	public static Graph getGraph(String diagramPath, String stId) throws DiagramJsonDeserializationException, DiagramJsonNotFoundException {
		final Path pathway = Paths.get(diagramPath, stId + ".graph.json");
		logger.info("Getting graph JSON {}", pathway);
		try {
			String json = new String(Files.readAllBytes(pathway));
			return DiagramFactory.getGraph(json);
		} catch (DeserializationException e) {
			logger.error("Could not deserialize diagram json for pathway {}", pathway);
			throw new DiagramJsonDeserializationException("Could not deserialize diagram json for pathway " + pathway);
		} catch (IOException e) {
			logger.error("Could not read diagram json for pathway {}", pathway);
			throw new DiagramJsonNotFoundException("Could not read diagram json for pathway " + pathway);
		}
	}

	/**
	 * Gets the Diagram of stId
	 *
	 * @param stId diagram stable identifier
	 *
	 * @return the Diagram of stId
	 *
	 * @throws DiagramJsonNotFoundException when stId has no corresponding JSON
	 *                                      file
	 */
	public static Diagram getDiagram(String stId) throws DiagramJsonNotFoundException, DiagramJsonDeserializationException {
		try {
			URL url = new URL(host + DIAGRAM_SERVICE + stId + ".json");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Response-Type", "application/json");

			switch (connection.getResponseCode()) {
				case 200:
					String json = IOUtils.toString(connection.getInputStream());
					return DiagramFactory.getDiagram(json);
				default:
					String error = IOUtils.toString(connection.getErrorStream());
					throw new DiagramJsonNotFoundException(error);
			}
		} catch (IOException e) {
			throw new DiagramJsonNotFoundException(e.getMessage());
		} catch (DeserializationException e) {
			throw new DiagramJsonDeserializationException(e.getMessage());
		}
	}

	/**
	 * Gets the Graph of stId
	 *
	 * @param stId diagram stable identifier
	 *
	 * @return the Graph of stId
	 *
	 * @throws DiagramJsonNotFoundException when stId has no corresponding JSON
	 *                                      graph file
	 */
	public static Graph getGraph(String stId) throws DiagramJsonNotFoundException, DiagramJsonDeserializationException {
		try {
			URL url = new URL(host + DIAGRAM_SERVICE + stId + ".graph.json");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Response-Type", "application/json");

			switch (connection.getResponseCode()) {
				case 200:
					String json = IOUtils.toString(connection.getInputStream());
					return DiagramFactory.getGraph(json);
				default:
					String error = IOUtils.toString(connection.getErrorStream());
					throw new DiagramJsonNotFoundException(error);
			}
		} catch (IOException e) {
			throw new DiagramJsonNotFoundException(stId);
		} catch (DeserializationException e) {
			throw new DiagramJsonDeserializationException(e.getMessage());
		}
	}

	public static SVGDocument getEHLD(String stId) throws EHLDMalformedException, EHLDNotFoundException {
		try {
			final URL url = new URL(host + EHLD_SERVICE + stId + ".svg");
//			final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			return DOCUMENT_FACTORY.createSVGDocument(url.toURI().toString());
		} catch (IOException e) {
			throw new EHLDMalformedException("Diagram " + stId + " has no associated EHLD");
		} catch (URISyntaxException e) {
			throw new EHLDNotFoundException("Diagram " + stId + " has no associated EHLD");
		}
	}

	public static SVGDocument getEHLD(String EHLDPath, String stId) throws EHLDException {
		if (EHLDPath == null)
			return getEHLD(stId);
		final Path path = Paths.get(EHLDPath, stId + ".svg");
		try {
			return DOCUMENT_FACTORY.createSVGDocument(path.toUri().toString());
		} catch (IOException e) {
			throw new EHLDMalformedException("EHLD document is not valid " + stId);
		}

	}
}
