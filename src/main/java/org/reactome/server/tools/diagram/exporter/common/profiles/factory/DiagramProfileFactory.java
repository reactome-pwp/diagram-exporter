package org.reactome.server.tools.diagram.exporter.common.profiles.factory;

import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.data.DiagramFactory;
import org.reactome.server.tools.diagram.data.exception.DeserializationException;
import org.reactome.server.tools.diagram.data.profile.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.DiagramExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class DiagramProfileFactory {

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
}
