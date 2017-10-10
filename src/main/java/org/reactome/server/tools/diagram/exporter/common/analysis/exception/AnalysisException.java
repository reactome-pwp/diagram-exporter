package org.reactome.server.tools.diagram.exporter.common.analysis.exception;

import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisError;

public class AnalysisException extends Exception {

    AnalysisError error;

    public AnalysisException(AnalysisError error) {
        this.error = error;
    }

    public AnalysisError getError() {
        return error;
    }
}
