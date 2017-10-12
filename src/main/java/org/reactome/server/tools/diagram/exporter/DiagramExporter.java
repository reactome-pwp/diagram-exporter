package org.reactome.server.tools.diagram.exporter;

import com.martiansoftware.jsap.*;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.pptx.PowerPointExporter;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main class for the diagram exporter project
 */
public class DiagramExporter {

    public static void main(String[] args) throws JSAPException, DiagramProfileException, DiagramJsonNotFoundException, DiagramJsonDeserializationException {
        // Program Arguments -i, -p, -o, -j, -f and -s
        SimpleJSAP jsap = new SimpleJSAP(DiagramExporter.class.getName(), "Export a given diagram to Power Point",
                new Parameter[]{
                        new FlaggedOption("stId",          JSAP.STRING_PARSER, null,     JSAP.REQUIRED,     'i', "stId",           "Stable Identifier of the diagram"),
                        new FlaggedOption("colourProfile", JSAP.STRING_PARSER, "Modern", JSAP.NOT_REQUIRED, 'p', "colourProfile",  "The colour diagram [Modern or Standard]"),
                        new FlaggedOption("outputFolder",  JSAP.STRING_PARSER, null,     JSAP.REQUIRED,     'o', "output",         "The output folder"),
                        new FlaggedOption("staticFolder",  JSAP.STRING_PARSER, null,     JSAP.REQUIRED,     'j', "static",         "The static json's folder"),
                        new FlaggedOption("license",       JSAP.STRING_PARSER, null,     JSAP.NOT_REQUIRED, 'l', "license",        "Software License file"),
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

        String staticFolder = config.getString("staticFolder");
        String colourProfile = config.getString("colourProfile");
        String stId = config.getString("stId");
        String lic = config.getString("license");

        File outputFolder = new File(config.getString("outputFolder") + "/" + colourProfile);
        if (!outputFolder.exists()) outputFolder.mkdirs();

        List<Long> flgs = Arrays.stream(config.getLongArray("flg")).boxed().collect(Collectors.toList());
        List<Long> sels = Arrays.stream(config.getLongArray("sel")).boxed().collect(Collectors.toList());
        Decorator decorator = new Decorator(flgs, sels, null);

        File finalPptx = PowerPointExporter.export(stId, staticFolder, colourProfile, outputFolder.getPath(), decorator, lic);

        File friendlyName = new File(outputFolder.getPath(), stId + ".pptx");
        finalPptx.renameTo(friendlyName);

        System.out.println("Diagram exporter: " + finalPptx);
    }
}
