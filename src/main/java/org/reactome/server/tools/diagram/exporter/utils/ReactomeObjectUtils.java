package org.reactome.server.tools.diagram.exporter.utils;

import org.reactome.server.graph.domain.model.DatabaseIdentifier;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.helper.RelationshipDirection;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReactomeObjectUtils {

    private static AdvancedDatabaseObjectService advancedDatabaseObjectService;

    @Autowired
    public ReactomeObjectUtils(AdvancedDatabaseObjectService advancedDatabaseObjectService) {
        ReactomeObjectUtils.advancedDatabaseObjectService = advancedDatabaseObjectService;
    }

    public static boolean pharmGKBExists(String id) {
        boolean pharmGKB = false;
        if (advancedDatabaseObjectService == null)
            advancedDatabaseObjectService = ReactomeGraphCore.getService(AdvancedDatabaseObjectService.class);
        DatabaseObject databaseObject = advancedDatabaseObjectService.findById(id, RelationshipDirection.OUTGOING);

        if (databaseObject instanceof Pathway) {
            List<DatabaseIdentifier> databaseNames = ((Pathway) databaseObject).getCrossReference();
            pharmGKB = databaseNames.stream().anyMatch(identifier -> identifier.getDatabaseName().contains("PharmGKB"));
        }
        return pharmGKB;
    }
}
