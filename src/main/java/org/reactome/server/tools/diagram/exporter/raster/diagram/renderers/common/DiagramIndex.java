package org.reactome.server.tools.diagram.exporter.raster.diagram.renderers.common;

import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Connector;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.FoundEntity;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.IdentifierSummary;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Creates a DiagramObjectDecorator per Node in the diagram. Computes all the
 * information that modifies each node basic rendering: selection, flag, halo
 * and analysis (enrichments and expressions). This data is not in the Node
 * class.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class DiagramIndex {

	private final HashMap<Long, DiagramObjectDecorator> index;
	private final DiagramDecorator decorator;
	private final DiagramAnalysis analysis;

	/**
	 * Creates a new DiagramIndex with the information for each node in maps.
	 *
	 * @param diagram diagram with nodes and reactions
	 * @param graph   background graph
	 */
	public DiagramIndex(Diagram diagram, Graph graph, RasterArgs args) throws AnalysisServerError, AnalysisException {
		this.index = new HashMap<>();
		decorator = new DiagramDecorator(this, args, graph, diagram);
		analysis = new DiagramAnalysis(this, args, graph, diagram);
	}

	public NodeDecorator getNodeDecorator(Long id) {
		return (NodeDecorator) index.computeIfAbsent(id, k -> new NodeDecorator());
	}

	public EdgeDecorator getEdgeDecorator(Long id) {
		return (EdgeDecorator) index.computeIfAbsent(id, k -> new EdgeDecorator());
	}

	public DiagramAnalysis getAnalysis() {
		return analysis;
	}

	public DiagramDecorator getDecorator() {
		return decorator;
	}

	/**
	 * Contains decorator information for a DiagramObject: flag, selection and
	 * halo.
	 */
	public abstract class DiagramObjectDecorator {
		private boolean flag = false;
		private boolean selected = false;
		private boolean halo = false;

		public boolean isFlag() {
			return flag;
		}

		void setFlag(boolean flag) {
			this.flag = flag;
		}

		public boolean isSelected() {
			return selected;
		}

		void setSelected(boolean selected) {
			this.selected = selected;
		}

		public boolean isHalo() {
			return halo;
		}

		void setHalo(boolean halo) {
			this.halo = halo;
		}
	}

	/**
	 * Contains extra rendering information for an edge: decorators plus
	 * connectors.
	 */
	public class EdgeDecorator extends DiagramObjectDecorator {
		private List<Connector> connectors = new LinkedList<>();

		public List<Connector> getConnectors() {
			return connectors;
		}
	}

	/**
	 * Contains extra rendering data for a Node: decorators plus expression
	 * values or enrichment value.
	 */
	public class NodeDecorator extends DiagramObjectDecorator {
		private List<FoundEntity> hitExpressions = null;
		private Double enrichment = null;
		private Integer totalExpressions;

		public Double getEnrichment() {
			return enrichment;
		}

		void setEnrichment(Double enrichment) {
			this.enrichment = enrichment;
		}

		public List<FoundEntity> getHitExpressions() {
			return hitExpressions;
		}

		void setHitExpressions(List<FoundEntity> hitExpressions) {
			this.hitExpressions = hitExpressions.stream()
					.filter(Objects::nonNull)
					.sorted((Comparator.comparing(IdentifierSummary::getId)))
					.collect(Collectors.toList());
			this.totalExpressions = hitExpressions.size();
		}

		public Integer getTotalExpressions() {
			return totalExpressions;
		}
	}
}
