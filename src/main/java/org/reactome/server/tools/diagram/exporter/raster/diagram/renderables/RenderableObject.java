package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.profiles.NodeColorSheet;

/**
 * Contains decorator information for a DiagramObject: flag, selection and
 * halo.
 */
public abstract class RenderableObject {
	private final boolean fadeOut;
	private final boolean disease;
	private boolean flag = false;
	private boolean selected = false;
	private boolean halo = false;

	public RenderableObject(DiagramObject object) {
		this.fadeOut = object.getIsFadeOut() != null && object.getIsFadeOut();
		this.disease = object.getIsDisease() != null && object.getIsDisease();
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

	public abstract NodeColorSheet getColorProfile(ColorProfiles colorProfiles);
}
