package org.reactome.server.tools.diagram.exporter.common;

import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.data.DiagramFactory;
import org.reactome.server.tools.diagram.data.exception.DeserializationException;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.data.profile.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.DiagramExporter;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourcesFactory {

	private static final Logger logger = LoggerFactory.getLogger("infoLogger");

	public static DiagramProfile getDiagramProfile(String name) throws DiagramProfileException, DiagramJsonDeserializationException {
		logger.info("Getting Profile [{}]", name);
		InputStream is = DiagramExporter.class.getResourceAsStream("/profiles/" + name + ".json");
		try {
			if (is == null) {
				logger.error("Could not read diagram color profile {}", name);
				throw new DiagramProfileException("Could not read diagram color profile " + name);
			}
			//            return DiagramProfileFactory.getModelObject(IOUtils.toString(is, "UTF-8"));
			return DiagramFactory.getProfile(IOUtils.toString(is, "UTF-8"));
		} catch (DeserializationException e) {
			logger.error("Could not deserialize diagram color profile {}", name);
			throw new DiagramJsonDeserializationException("Could not deserialize diagram color profile " + name);
		} catch (IOException e) {
			logger.error("Could not read diagram color profile {}", name);
			throw new DiagramProfileException("Could not read diagram color profile " + name);
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
