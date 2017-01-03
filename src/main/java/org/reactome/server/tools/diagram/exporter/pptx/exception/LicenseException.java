package org.reactome.server.tools.diagram.exporter.pptx.exception;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class LicenseException extends Exception {

    public LicenseException() {
        super("ERROR-10100 Could export to Power Point. Please contact help@reactome.org and provide the given error code");
    }
}
