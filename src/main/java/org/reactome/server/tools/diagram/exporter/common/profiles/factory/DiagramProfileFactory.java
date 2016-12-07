package org.reactome.server.tools.diagram.exporter.common.profiles.factory;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import org.reactome.server.tools.diagram.exporter.common.profiles.model.DiagramProfile;
import org.reactome.server.tools.diagram.exporter.common.profiles.model.DiagramProfileNode;
import org.reactome.server.tools.diagram.exporter.common.profiles.model.DiagramProfileProperties;
import org.reactome.server.tools.diagram.exporter.common.profiles.model.DiagramProfileThumbnail;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class DiagramProfileFactory {

    public static DiagramProfile getModelObject(String json) throws DiagramProfileException {
        try {
            AutoBeanFactory factory = AutoBeanFactorySource.create(ModelAutoBeanFactory.class);
            AutoBean<DiagramProfile> bean = AutoBeanCodex.decode(factory, DiagramProfile.class, json);
            return bean.as();
        } catch (Throwable e) {
            throw new DiagramProfileException("Error mapping json string for [" + DiagramProfile.class + "]: " + json, e);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    interface ModelAutoBeanFactory extends AutoBeanFactory {
        AutoBean<DiagramProfile> profile();
        AutoBean<DiagramProfileProperties> profileProperties();
        AutoBean<DiagramProfileNode> profileNode();
        AutoBean<DiagramProfileThumbnail> profileThumbnail();
    }
}
