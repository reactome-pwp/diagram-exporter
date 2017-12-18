package org.reactome.server.tools.diagram.exporter.raster.diagram.renderables;

import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.FoundEntity;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.IdentifierSummary;
import org.reactome.server.tools.diagram.exporter.raster.diagram.common.DiagramIndex;
import org.reactome.server.tools.diagram.exporter.raster.diagram.layers.DiagramCanvas;
import org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.NodeAbstractRenderer;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;
import java.awt.geom.Area;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Contains extra rendering data for a Node: decorators plus expression values
 * or enrichment value.
 */
public abstract class RenderableNode extends RenderableObject {

	private final boolean crossed;
	private final boolean dashed;

	private final Shape backgroundShape;
	private final Node node;
	private final Area backgroundArea;

	private List<FoundEntity> hitExpressions;
	private Double enrichment;
	private Integer totalExpressions;
	private Double expressionValue;

	public RenderableNode(Node node) {
		super(node);
		this.node = node;
		this.backgroundShape = backgroundShape();
		this.backgroundArea = new Area(backgroundShape);
		this.dashed = node.getNeedDashedBorder() != null && node.getNeedDashedBorder();
		this.crossed = node.getIsCrossed() != null && node.getIsCrossed();
	}

	public Double getEnrichment() {
		return enrichment;
	}

	public void setEnrichment(Double enrichment) {
		this.enrichment = enrichment;
	}

	public List<FoundEntity> getHitExpressions() {
		return hitExpressions;
	}

	public void setHitExpressions(List<FoundEntity> hitExpressions) {
		this.hitExpressions = hitExpressions.stream()
				.filter(Objects::nonNull)
				.distinct()
				.sorted((Comparator.comparing(IdentifierSummary::getId)))
				.collect(Collectors.toList());
		this.totalExpressions = hitExpressions.size();
	}

	abstract Shape backgroundShape();

	abstract NodeAbstractRenderer getRenderer();

	public Integer getTotalExpressions() {
		return totalExpressions;
	}

	public Node getNode() {
		return node;
	}

	public Shape getBackgroundShape() {
		return backgroundShape;
	}

	public Area getBackgroundArea() {
		return backgroundArea;
	}

	public boolean isCrossed() {
		return crossed;
	}

	public void render(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		getRenderer().draw(this, canvas, colorProfiles, index, t);
	}

	public void renderAnalysis(DiagramCanvas canvas, ColorProfiles colorProfiles, DiagramIndex index, int t) {
		getRenderer().analysis(this, canvas, index, colorProfiles, t);
	}
	public boolean isDashed() {
		return dashed;
	}

	public void setExpressionValue(Double expressionValue) {
		this.expressionValue = expressionValue;
	}

	public Double getExpressionValue() {
		return expressionValue;
	}
}
