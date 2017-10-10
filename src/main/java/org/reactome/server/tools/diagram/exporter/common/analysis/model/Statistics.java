package org.reactome.server.tools.diagram.exporter.common.analysis.model;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public interface Statistics {

    String getResource();

    Integer getTotal();

    Integer getFound();

    Double getRatio();
}
