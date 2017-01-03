package org.reactome.server.tools.diagram.exporter.pptx.exception;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class DiagramExporterException extends Exception {

    public DiagramExporterException(Throwable cause) {
        super(cause);
    }
    public DiagramExporterException(String message) {
        super(message);
    }
    public DiagramExporterException(String message, Throwable cause) {
        super(message, cause);
    }
}
