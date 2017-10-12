package org.reactome.server.tools.diagram.exporter.common;

import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.data.DiagramFactory;
import org.reactome.server.tools.diagram.data.exception.DeserializationException;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.profile.analysis.AnalysisProfile;
import org.reactome.server.tools.diagram.data.profile.diagram.DiagramProfile;
import org.reactome.server.tools.diagram.data.profile.interactors.InteractorProfile;
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

public class ResourcesFactory {

	private static final Logger logger = LoggerFactory.getLogger("infoLogger");

	public static DiagramProfile getDiagramProfile(String name) throws DiagramProfileException, DiagramJsonDeserializationException {
		logger.info("Getting Profile [{}]", name);
		final String file = "diagram_" + name.toLowerCase() + ".json";
		final InputStream resource = ProfileResources.class.getResourceAsStream(file);
		try {
			if (resource == null) {
				logger.error("Could not read diagram color profile {}", name);
				throw new DiagramProfileException("Could not read diagram color profile " + name);
			}
			//            return DiagramProfileFactory.getModelObject(IOUtils.toString(is, "UTF-8"));
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

	public static AnalysisProfile getAnalysisProfile(String name) throws DiagramProfileException, DiagramJsonDeserializationException {
		logger.info("Getting analysis profile [{}]", name);
		final String file = "analysis_" + name.toLowerCase() + ".json";
		final InputStream resource = ProfileResources.class.getResourceAsStream(file);
		try {
			if (resource == null) {
				logger.error("Could not read analysis color profile {}", name);
				throw new DiagramProfileException("Could not read analysis color profile " + name);
			}
			//            return DiagramProfileFactory.getModelObject(IOUtils.toString(is, "UTF-8"));
			final String json = IOUtils.toString(resource, Charset.defaultCharset());
			return DiagramFactory.getAnalysisProfile(json);
		} catch (DeserializationException e) {
			logger.error("Could not deserialize diagram color profile {}", name);
			throw new DiagramJsonDeserializationException("Could not deserialize diagram color profile " + name);
		} catch (IOException e) {
			logger.error("Could not read diagram color profile {}", name);
			throw new DiagramProfileException("Could not read diagram color profile " + name);
		}
	}

	public static InteractorProfile getInteractorsProfile(String name) throws DiagramProfileException, DiagramJsonDeserializationException {
		logger.info("Getting interactors profile [{}]", name);
		final String file = "interactors_" + name.toLowerCase() + ".json";
		final InputStream resource = ProfileResources.class.getResourceAsStream(file);
		try {
			if (resource == null) {
				logger.error("Could not read interactors color profile {}", name);
				throw new DiagramProfileException("Could not read interactors color profile " + name);
			}
			//            return DiagramProfileFactory.getModelObject(IOUtils.toString(is, "UTF-8"));
			final String json = IOUtils.toString(resource, Charset.defaultCharset());
			return DiagramFactory.getInteractorsProfile(json);
		} catch (DeserializationException e) {
			logger.error("Could not deserialize interactors color profile {}", name);
			throw new DiagramJsonDeserializationException("Could not deserialize interactor color profile " + name);
		} catch (IOException e) {
			logger.error("Could not read diagram color profile {}", name);
			throw new DiagramProfileException("Could not read interactor color profile " + name);
		}
	}

	public static Diagram getDiagram(String staticFolder, String stId) throws DiagramJsonDeserializationException, DiagramJsonNotFoundException {
		final Path pathway = Paths.get(staticFolder, stId + ".json");
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
