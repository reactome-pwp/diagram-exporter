package org.reactome.server.tools.diagram.exporter.common.profiles.factory;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class DiagramJsonException extends Exception {

    @SuppressWarnings("UnusedDeclaration")
    public DiagramJsonException() {
    }

    public DiagramJsonException(String message, Throwable cause) {
        super(message, cause);
    }
    public DiagramJsonException(String message) {
        super(message);
    }
}
