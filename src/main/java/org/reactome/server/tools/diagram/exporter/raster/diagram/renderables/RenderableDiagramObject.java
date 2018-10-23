package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramData;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

import java.awt.*;

/**
 * Contains decorator information for a DiagramObject: flag, selection and
 * halo.
 */
public abstract class RenderableDiagramObject<T extends DiagramObject> {

	private final boolean fadeOut;
	private final boolean disease;
	private final T diagramObject;
	private boolean flag = false;
	private boolean selected = false;
	private boolean halo = false;

	RenderableDiagramObject(T diagramObject) {
		this.diagramObject = diagramObject;
		this.fadeOut = diagramObject.getIsFadeOut() != null && diagramObject.getIsFadeOut();
		this.disease = diagramObject.getIsDisease() != null && diagramObject.getIsDisease();
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isHalo() {
		return halo;
	}

	public void setHalo(boolean halo) {
		this.halo = halo;
	}

	public boolean isDisease() {
		return disease;
	}

	public boolean isFadeOut() {
		return fadeOut;
	}

	public T getDiagramObject() {
		return diagramObject;
	}

	/** Get the proper NodeColorSheet for this Object */
	public abstract NodeColorSheet getColorProfile(ColorProfiles colorProfiles);

	public abstract void draw(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramData data, int t);


	Color getStrokeColor(ColorProfiles colorProfiles, AnalysisType type) {
		// selection -> disease -> fadeout -> analysis -> normal
		if (isSelected())
			return colorProfiles.getDiagramSheet().getProperties().getSelection();
		if (isDisease())
			return colorProfiles.getDiagramSheet().getProperties().getDisease();
		if (isFadeOut())
			return getColorProfile(colorProfiles).getFadeOutStroke();
		if (type != null)
			return getColorProfile(colorProfiles).getLighterStroke();
		return getColorProfile(colorProfiles).getStroke();
	}

	protected Color getFillColor(ColorProfiles colorProfiles, AnalysisType type) {
		//fadeout -> analysis -> normal
		if (isFadeOut())
			return getColorProfile(colorProfiles).getFadeOutFill();
		if (type != null)
			return getColorProfile(colorProfiles).getLighterFill();
		return getColorProfile(colorProfiles).getFill();
	}

	protected Color getTextColor(ColorProfiles colorProfiles, AnalysisType type) {
		//fadeout -> analysis -> normal
		if (isFadeOut())
			return getColorProfile(colorProfiles).getFadeOutText();
		if (type != null)
			return getColorProfile(colorProfiles).getLighterText();
		return getColorProfile(colorProfiles).getText();
	}

}
