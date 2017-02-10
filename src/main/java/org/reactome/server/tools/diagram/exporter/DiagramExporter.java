package org.reactome.server.tools.diagram.exporter;

import com.martiansoftware.jsap.*;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.pptx.PowerPointExporter;
import org.reactome.server.tools.diagram.exporter.pptx.exception.LicenseException;
import org.reactome.server.tools.diagram.exporter.pptx.model.Decorator;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main class for the diagram exporter project
 */
public class DiagramExporter {

    public static void main(String[] args) throws JSAPException, LicenseException, DiagramProfileException, DiagramJsonNotFoundException, DiagramJsonDeserializationException {
        // Program Arguments -h, -p, -u, -k
        SimpleJSAP jsap = new SimpleJSAP(DiagramExporter.class.getName(), "Export a given diagram to Power Point",
                new Parameter[]{
                        new FlaggedOption("stId",         JSAP.STRING_PARSER, "R-HSA-69620", JSAP.REQUIRED,     'i', "stId",          "Stable Identifier of the diagram"),
                        new FlaggedOption("colorProfile", JSAP.STRING_PARSER, "Modern",      JSAP.NOT_REQUIRED, 'p', "color profile", "The color profile"),
                        new FlaggedOption("outputFolder", JSAP.STRING_PARSER, null,          JSAP.REQUIRED,     'o', "output",        "The output folder"),
                        new FlaggedOption("staticFolder", JSAP.STRING_PARSER, null,          JSAP.REQUIRED,     'j', "static",        "The static json's folder")
                }
        );

        FlaggedOption flag =  new FlaggedOption("flg", JSAP.LONG_PARSER, null, JSAP.NOT_REQUIRED, 'f', "flag", "Flag nodes");
        flag.setList(true);
        flag.setListSeparator(',');
        jsap.registerParameter(flag);

        FlaggedOption selection =  new FlaggedOption("sel", JSAP.LONG_PARSER, null, JSAP.NOT_REQUIRED, 's', "selection", "Select nodes or edges");
        selection.setList(true);
        selection.setListSeparator(',');
        jsap.registerParameter(selection);

        JSAPResult config = jsap.parse(args);
        if (jsap.messagePrinted()) System.exit(1);

        String colorProfile = config.getString("colorProfile");
        String stId = config.getString("stId");
        File path = new File(config.getString("outputFolder") + colorProfile);
        if (!path.exists()) path.mkdirs();
        String outputFile = path.getPath() + "/" + stId + ".pptx";
        List<Long> flgs = Arrays.stream(config.getLongArray("flg")).boxed().collect(Collectors.toList());
        List<Long> sels = Arrays.stream(config.getLongArray("sel")).boxed().collect(Collectors.toList());
        Decorator decorator = new Decorator(flgs, sels);
        PowerPointExporter.export(config.getString("staticFolder") + stId, colorProfile, outputFile, decorator);

        System.out.println("Diagrams exported.");

    }
}
