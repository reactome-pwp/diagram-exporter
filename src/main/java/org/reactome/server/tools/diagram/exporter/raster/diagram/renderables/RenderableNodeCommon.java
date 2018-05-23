package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.NodeCommon;

/**
 * Contains extra rendering data for a Node: decorators plus expression values
 * or enrichment value.
 */
public abstract class RenderableNodeCommon<T extends NodeCommon> extends RenderableDiagramObject<T> {

	private final boolean crossed;
	private final boolean dashed;


	RenderableNodeCommon(T nodeCommon) {
		super(nodeCommon);
		this.dashed = nodeCommon.getNeedDashedBorder() != null && nodeCommon.getNeedDashedBorder();
		this.crossed = nodeCommon.getIsCrossed() != null && nodeCommon.getIsCrossed();
	}

	public boolean isCrossed() {
		return crossed;
	}

	public boolean isDashed() {
		return dashed;
	}

}
