package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.layout;

import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableNode;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderables.RenderableObject;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;

public class ObjectRenderer {

	protected Color getStrokeColor(RenderableObject renderableNode, ColorProfiles colorProfiles, DiagramIndex index) {
		// selected -> disease -> fadeout -> analysis -> normal
		if (renderableNode.isSelected())
			return colorProfiles.getDiagramSheet().getProperties().getSelection();
		if (renderableNode.isDisease())
			return colorProfiles.getDiagramSheet().getProperties().getDisease();
		if (renderableNode.isFadeOut())
			return renderableNode.getColorProfile(colorProfiles).getFadeOutStroke();
		if (index.getAnalysis().getType() != AnalysisType.NONE)
			return renderableNode.getColorProfile(colorProfiles).getLighterStroke();
		return renderableNode.getColorProfile(colorProfiles).getStroke();
	}

	protected Color getFillColor(RenderableObject renderableNode, ColorProfiles colorProfiles, DiagramIndex index) {
		//fadeout -> analysis -> normal
		if (renderableNode.isFadeOut())
			return renderableNode.getColorProfile(colorProfiles).getFadeOutFill();
		if (index.getAnalysis().getType() != AnalysisType.NONE)
			return renderableNode.getColorProfile(colorProfiles).getLighterFill();
		return renderableNode.getColorProfile(colorProfiles).getFill();
	}

	protected Color getTextColor(RenderableNode renderableNode, ColorProfiles colorProfiles, DiagramIndex index) {
		//fadeout -> analysis -> normal
		if (renderableNode.isFadeOut())
			return renderableNode.getColorProfile(colorProfiles).getFadeOutText();
		else if (index.getAnalysis().getType() == AnalysisType.NONE)
			return renderableNode.getColorProfile(colorProfiles).getText();
		else
			return renderableNode.getColorProfile(colorProfiles).getLighterText();
	}


}
