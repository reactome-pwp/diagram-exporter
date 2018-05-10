package org.reactome.server.tools.diagram.exporter.raster.ehld.exception;

/**
 * if diagram is EHLD, and the source EHLD is not found or bad format
 */
public class EhldException extends Exception {

	public EhldException(String message) {
		super(message);
	}
}
