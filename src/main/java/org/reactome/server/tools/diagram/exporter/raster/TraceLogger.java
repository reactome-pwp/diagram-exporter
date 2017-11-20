package org.reactome.server.tools.diagram.exporter.raster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraceLogger {

	private static final Logger logger = LoggerFactory.getLogger("trace");

	public static void trace(String... message) {
		logger.trace(String.join("\t", message));
	}

}
