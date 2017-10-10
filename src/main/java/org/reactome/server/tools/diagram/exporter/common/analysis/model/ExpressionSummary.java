package org.reactome.server.tools.diagram.exporter.common.analysis.model;

import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public interface ExpressionSummary {
    List<String> getColumnNames();
    Double getMin();
    Double getMax();
}
