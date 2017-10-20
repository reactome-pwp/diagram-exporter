package org.reactome.server.tools.diagram.exporter.raster.renderers.layout;

import org.reactome.server.tools.diagram.data.layout.DiagramObject;
import org.reactome.server.tools.diagram.data.layout.NodeCommon;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.raster.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.NodeRenderInfo;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.RendererProperties;
import org.reactome.server.tools.diagram.exporter.raster.renderers.common.ShapeFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class NodeRendererAbstractImpl implements NodeRenderer {

	@Override
	public void draw(DiagramCanvas canvas, DiagramObject item, ColorProfiles colorProfiles, DiagramIndex index) {
		NodeRenderInfo info = getNodeRenderInfo(canvas, item, colorProfiles, index);
		flag(info);
		halo(info);
		background(info);
		analysis(info);
		foreground(info);
		attachments(info);
		border(info);
		cross(info);
	}

	@Override
	public NodeRenderInfo getNodeRenderInfo(DiagramCanvas canvas, DiagramObject item, ColorProfiles colorProfiles, DiagramIndex index) {
		final NodeCommon node = (NodeCommon) item;
		return new NodeRenderInfo(node, index, colorProfiles, canvas, backgroundShape(node), foregroundShape(node), foregroundFill(colorProfiles, index));
	}

	protected Color foregroundFill(ColorProfiles colorProfiles, DiagramIndex index) {
		return null;
	}

	@Override
	public void flag(NodeRenderInfo info) {
		if (info.getDecorator().isFlag())
			info.getFlagLayer().add(info.getFlagColor(), info.getFlagStroke(),
					info.getBackgroundShape());
	}

	@Override
	public void halo(NodeRenderInfo info) {
		if (info.getDecorator().isHalo())
			info.getHaloLayer().add(info.getHaloColor(), info.getHaloStroke(),
					info.getBackgroundShape());
	}

	@Override
	public void background(NodeRenderInfo info) {
		if (info.getBackgroundArea() != null)
			info.getBgLayer().add(info.getBackgroundColor(), info.getBackgroundArea());
	}

	@Override
	public double analysis(NodeRenderInfo info) {
		return 0;
	}

	@Override
	public void foreground(NodeRenderInfo info) {
		if (info.getForegroundShape() != null)
			info.getFgLayer().add(info.getForegroundColor(),
					info.getForegroundShape());
	}

	@Override
	public void border(NodeRenderInfo info) {
		if (info.getBackgroundShape() != null)
			info.getBorderLayer().add(info.getBorderColor(), info.getBorderStroke(), info.getBackgroundShape());
		if (info.getForegroundShape() != null)
			info.getBorderLayer().add(info.getBorderColor(), info.getBorderStroke(), info.getForegroundShape());
	}

	@Override
	public void attachments(NodeRenderInfo info) {

	}

	@Override
	public void text(NodeRenderInfo info) {
		if (info.getNode().getDisplayName() != null) {
			final NodeProperties prop = info.getNode().getProp();
			if (info.getForegroundShape() != null) {
				final Rectangle2D bounds = info.getForegroundShape().getBounds2D();
				final NodeProperties limits = NodePropertiesFactory.get(
						bounds.getX(), bounds.getY(), bounds.getWidth(),
						bounds.getHeight());
				// as splitText is in background dimensions,
				// we need to change to foreground percentage
				double splitText = (prop.getX() + info.getTextSplit() * prop.getWidth() - limits.getX()) / limits.getWidth();
				info.getTextLayer().add(info.getTextColor(), info.getNode().getDisplayName(), limits, 1, splitText);
			} else
				info.getTextLayer().add(info.getTextColor(), info.getNode().getDisplayName(),
						prop, RendererProperties.NODE_TEXT_PADDING,
						info.getTextSplit());
		}
	}

	@Override
	public void cross(NodeRenderInfo info) {
		if (info.isCrossed())
			ShapeFactory.cross(info.getNode().getProp()).forEach(line ->
					info.getCrossLayer().add(info.getCrossColor(), info.getBorderStroke(), line));
	}

	protected Shape foregroundShape(NodeCommon node) {
		return null;
	}

	protected Shape backgroundShape(NodeCommon node) {
		return ShapeFactory.rectangle(node.getProp(), 0.0);
	}
}
