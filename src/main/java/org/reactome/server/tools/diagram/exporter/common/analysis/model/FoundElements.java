package org.reactome.server.tools.diagram.exporter.common.analysis.model;

import java.util.List;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public interface FoundElements {

    List<FoundEntity> getEntities();

    List<FoundInteractor> getInteractors();

    Set<String> getResources();

    List<String> getExpNames();

    Integer getFoundEntities();

    Integer getFoundInteractors();

}