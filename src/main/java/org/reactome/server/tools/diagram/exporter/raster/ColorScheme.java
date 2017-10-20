package org.reactome.server.tools.diagram.exporter.raster;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ColorScheme {

	private final String diagramProfileName;
	private final String analysisProfileName;
	private final String interactorProfileName;

	public ColorScheme(String diagramProfileName, String analysisProfileName,
	                   String interactorProfileName) {

		this.diagramProfileName = diagramProfileName;
		this.analysisProfileName = analysisProfileName;
		this.interactorProfileName = interactorProfileName;
	}

	public String getAnalysisProfileName() {
		return analysisProfileName;
	}

	public String getDiagramProfileName() {
		return diagramProfileName;
	}

	public String getInteractorProfileName() {
		return interactorProfileName;
	}
}
