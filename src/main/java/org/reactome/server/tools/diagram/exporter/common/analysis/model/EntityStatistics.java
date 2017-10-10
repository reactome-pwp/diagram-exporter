package org.reactome.server.tools.diagram.exporter.common.analysis.model;

import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public interface EntityStatistics extends Statistics {

    Integer getCuratedTotal();

    Integer getCuratedFound();

    Integer getInteractorsTotal();

    Integer getInteractorsFound();

    Double getpValue();

    Double getFdr();

    List<Double> getExp();
}
