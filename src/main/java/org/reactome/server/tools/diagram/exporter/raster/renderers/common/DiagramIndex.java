package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.graph.GraphNode;
import org.reactome.server.tools.diagram.data.layout.*;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisClient;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.*;
import org.reactome.server.tools.diagram.exporter.raster.AnalysisType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Computes a Diagram to group nodes by renderableClass and RenderType, so all
 * nodes in a group can be rendered with the same colors for filling and
 * stroking.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class DiagramIndex {

	private final Diagram diagram;
	private final Graph graph;
	private Decorator decorator;
	private AnalysisType analysisType;

	private Map<Long, DiagramObject> diagramIndex;
	private Map<Long, GraphNode> graphIndex;

	private Set<Long> selected = new TreeSet<>();
	private Set<Long> flags = new TreeSet<>();
	private Set<Long> haloed = new HashSet<>();
	private Map<String, Double> analysisIndex = new HashMap<>();

	/**
	 * Computes the maps of nodes, reactions and connectors so they are grouped
	 * by renderableClass, renderType and normal, selection, halo or flag
	 * status.
	 *
	 * @param diagram      diagram with nodes and reactions
	 * @param graph        background graph
	 * @param decorator    decorator with selection and flags
	 * @param analysisType type of analysis
	 */
	public DiagramIndex(Diagram diagram, Graph graph, Decorator decorator, AnalysisType analysisType) {
		this.diagram = diagram;
		this.graph = graph;
		this.decorator = decorator;
		this.analysisType = analysisType;
		createIndexes();
		collect();
		analysis();
	}

	private void createIndexes() {
		diagramIndex = new HashMap<>();
		Stream.of(diagram.getEdges(), diagram.getNodes(), diagram.getLinks(),
				diagram.getNotes())
				.flatMap(Collection::stream)
				.forEach(item -> diagramIndex.put(item.getId(), item));
		graphIndex = new HashMap<>();
		Stream.of(graph.getEdges(), graph.getNodes())
				.flatMap(Collection::stream)
				.forEach(item -> graphIndex.put(item.getDbId(), item));
	}

	private void collect() {
		if (decorator != null) {
			selectNodes();
			selectReactions();
		}
	}


	private void selectNodes() {
		diagram.getNodes().forEach(node -> {
			// Select node
			if (decorator.getSelected().contains(node.getReactomeId())) {
				selected.add(node.getId());
				haloed.add(node.getId());
				node.getConnectors().forEach(connector -> {
					final Edge reaction = (Edge) diagramIndex.get(connector.getEdgeId());
					haloEdge(reaction, false);
				});
			}
			// Flag node
			if (decorator.getFlags().contains(node.getReactomeId()))
				flags.add(node.getId());
		});
	}

	private void selectReactions() {
			diagram.getEdges().forEach(reaction -> {
			// Select edge
			if (decorator.getSelected().contains(reaction.getReactomeId())) {
				selected.add(reaction.getId());
				haloEdge(reaction, true);
			}
			// Flag edge
			if (decorator.getFlags().contains(reaction.getReactomeId())) {
				flags.add(reaction.getId());
				flagReaction(reaction);
			}
		});
	}


	/**
	 * Adds the reaction to the haloReaction set, participating nodes to
	 * haloNodes and participating connectors to haloConnectors
	 *
	 * @param reaction      reaction to halo
	 * @param withSelection whether this reaction is selected or not. If true,
	 *                      participating connectors will be added to
	 *                      selectedConnectors as well
	 */
	private void haloEdge(Edge reaction, boolean withSelection) {
		haloed.add(reaction.getId());
		streamParticipants(reaction)
				.forEach(node -> {
					if (node.getIsFadeOut() == null || !node.getIsFadeOut())
						haloed.add(node.getId());
					node.getConnectors().stream()
							.filter(connector -> connector.getEdgeId().equals(reaction.getId()))
							.forEach(connector -> {
								haloed.add(connector.getEdgeId());
								if (withSelection)
									selected.add(connector.getEdgeId());
							});
				});
	}

	/**
	 * Very convenient method to iterate over all of the activators, catalysts,
	 * inhibitors, inputs and outputs in a unique stream
	 *
	 * @param edge the chosen one
	 *
	 * @return a stream with all participant nodes in the reaction
	 */
	private Stream<Node> streamParticipants(Edge edge) {
		return Stream.of(edge.getActivators(), edge.getCatalysts(),
				edge.getInhibitors(), edge.getInputs(), edge.getOutputs())
				.filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.map(part -> diagramIndex.get(part.getId()))
				.map(Node.class::cast);
	}

	private void flagReaction(Edge reaction) {
		flags.add(reaction.getId());
		streamParticipants(reaction).forEach(node ->
				node.getConnectors().stream()
						.filter(connector -> connector.getEdgeId().equals(reaction.getId()))
						.map(Connector::getEdgeId)
						.forEach(flags::add));
	}

//	private RenderType getRenderType(DiagramObject item) {
//		final boolean isDisease = item.getIsDisease() != null;
//		if (item.getIsFadeOut() != null) {
//			return isDisease
//					? RenderType.DISEASE
//					: RenderType.FADE_OUT;
//		}
//		if (analysisType == AnalysisType.NONE) {
//			return isDisease
//					? RenderType.DISEASE
//					: RenderType.NORMAL;
//		}
//		final GraphNode node = getNode(graph, item);
//		if (node == null) {
//			return isDisease
//					? RenderType.NOT_HIT_BY_ANALYSIS_DISEASE
//					: RenderType.NOT_HIT_BY_ANALYSIS_NORMAL;
//		}
//		if (isHit(node)) {
////			return RenderType.HIT_INTERACTORS;
//			switch (analysisType) {
//				case SPECIES_COMPARISON:
//				case OVERREPRESENTATION:
//					return isDisease
//							? RenderType.HIT_BY_ENRICHMENT_DISEASE
//							: RenderType.HIT_BY_ENRICHMENT_NORMAL;
//				case EXPRESSION:
//					return isDisease
//							? RenderType.HIT_BY_EXPRESSION_DISEASE
//							: RenderType.HIT_BY_EXPRESSION_NORMAL;
//				default:
//					return RenderType.HIT_INTERACTORS;
//			}
//		}
//		return isDisease
//				? RenderType.NOT_HIT_BY_ANALYSIS_DISEASE
//				: RenderType.NOT_HIT_BY_ANALYSIS_NORMAL;
//	}
//
//	private RenderType getRenderType(Connector item) {
//		if (item.getIsDisease() != null && item.getIsDisease())
//			return RenderType.DISEASE;
//		if (item.getIsFadeOut() != null && item.getIsFadeOut())
//			return RenderType.FADE_OUT;
//		return RenderType.NORMAL;
//
//	}

	private void analysis() {
		if (decorator == null)
			return;
		final String token = decorator.getToken();
		if (token == null || token.isEmpty())
			return;
		final String stId = graph.getStId();
		System.out.println(token);
		try {
			final AnalysisResult result = AnalysisClient.getAnalysisResult(token);
			final List<ResourceSummary> summaryList = result.getResourceSummary();
			final ResourceSummary resourceSummary = summaryList.size() == 2 ? summaryList.get(1) : summaryList.get(0);
			final String resource = resourceSummary.getResource();
			final FoundElements foundElements = AnalysisClient.getFoundElements(stId, token, resource);
			subPathways(token, resource);
			System.out.printf("elements to analyse (%d)\n", foundElements.getEntities().size());
			foundElements.getEntities().forEach(foundEntity ->
					System.out.println(" - " + getIdsFor(foundEntity.getId())));
//			System.out.printf("found %d entities\n", foundElements.getFoundEntities());
			final List<String> pathways = Arrays.asList(stId);
//			for (PathwaySummary pathwaySummary : pathwaysSummary) {
//				System.out.println(pathwaySummary.getStId() + " " + pathwaySummary.getName());
//			}
		} catch (AnalysisException | AnalysisServerError e) {
			e.printStackTrace();
		}


	}

	private void subPathways(String token, String resource) throws AnalysisServerError, AnalysisException {
		// I need a list of process nodes (id, stid, displayname) with %
		// 1 list of processnodes (stid)
		final List<String> subPathways = diagram.getNodes().stream()
				.filter(node -> node.getRenderableClass().equals("ProcessNode"))
				.map(node -> graphIndex.get(node.getReactomeId()).getStId())
				.collect(Collectors.toList());
		// 2 call analysis
		final PathwaySummary[] pathwaysSummary = AnalysisClient.getPathwaysSummary(subPathways, token, resource);
		// extract %
		for (PathwaySummary summary : pathwaysSummary) {
			final String stId = summary.getStId();
			final EntityStatistics entities = summary.getEntities();
			double percentage = 0.0;
			int found = 0;
			int total = 0;
			if (entities != null) {
				found = entities.getFound();
				total = entities.getTotal();
				percentage = (double) found / total;
			}
			System.out.printf("%s %d/%d(%.3f)\n", stId, found, total, percentage);
			analysisIndex.put(stId, percentage);
		}
		System.out.printf("pathways to analyse (%d)\n", subPathways.size());
		subPathways.forEach(s -> System.out.println(" - " + s));
	}

	private List<Long> getIdsFor(String prot) {
		// Also parents
		return graph.getNodes().stream()
				.filter(node -> Objects.nonNull(node.getIdentifier()))
				.filter(node -> node.getIdentifier().equals(prot))
				.map(GraphNode::getDbId)
				.collect(Collectors.toList());
	}

	private boolean isHit(GraphNode item) {
		return false;
	}

	private GraphNode getNode(Graph graph, DiagramObject item) {
		return graph.getNodes().stream()
				.filter(node -> node.getDbId().equals(item.getId()))
				.findFirst().orElse(null);
	}

	public Set<Long> getSelected() {
		return selected;
	}

	public Set<Long> getFlags() {
		return flags;
	}

	public Set<Long> getHaloed() {
		return haloed;
	}
}
