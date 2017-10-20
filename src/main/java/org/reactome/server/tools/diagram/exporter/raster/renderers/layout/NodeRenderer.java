package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.exporter.raster.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.NodeRenderInfo;

public interface NodeRenderer extends Renderer {

	NodeRenderInfo getNodeRenderInfo(DiagramCanvas canvas, DiagramObject item, ColorProfiles colorProfiles, DiagramIndex index);

	void flag(NodeRenderInfo info);

	void halo(NodeRenderInfo info);

	void background(NodeRenderInfo info);

	double analysis(NodeRenderInfo info);

	void foreground(NodeRenderInfo info);

	void attachments(NodeRenderInfo info);

	void border(NodeRenderInfo info);

	void text(NodeRenderInfo info);

	void cross(NodeRenderInfo info);
}
