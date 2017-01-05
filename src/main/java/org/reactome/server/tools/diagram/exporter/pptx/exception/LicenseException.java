package org.reactome.server.tools.diagram.exporter.pptx.exception;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class LicenseException extends Exception {

    public LicenseException() {
        super("Could not export to Power Point. Service is unavailable.");
    }
}
