package org.reactome.server.tools.diagram.exporter.common;

import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.data.DiagramFactory;
import org.reactome.server.tools.diagram.data.exception.DeserializationException;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ProfileResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
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
	private static final String DEFAULT_DIAGRAM_PROFILE = "modern";

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
	 *                                             associated to getName, neither a
	 *                                             default diagram
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
			final String json = IOUtils.toString(resource, Charset.defaultCharset());
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
}
