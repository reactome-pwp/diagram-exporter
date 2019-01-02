package org.reactome.server.tools.diagram.exporter.raster.diagram.common;

import org.reactome.server.tools.diagram.data.graph.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Indexes graph elements by dbId and stId. Indexes are unmodifiable.
 */
class GraphIndex {

	private final Map<Long, EventNode> eventsByDbId;
	private final Map<Long, EntityNode> nodesByDbId;
	private final Map<Long, SubpathwayNode> pathwaysByDbId;
	private final Map<String, EventNode> eventsByStId;
	private final Map<String, EntityNode> nodesByStId;
	private final Map<String, SubpathwayNode> pathwaysByStId;

	/**
	 * Creates an unmodifiable graph index.
	 * @param graph graph to index
	 */
	GraphIndex(Graph graph) {
		eventsByDbId = createIndex(graph.getEdges(), EventNode::getDbId);
		nodesByDbId = createIndex(graph.getNodes(), GraphNode::getDbId);
		eventsByStId = createIndex(graph.getEdges(), EventNode::getStId);
		nodesByStId = createIndex(graph.getNodes(), GraphNode::getStId);
		pathwaysByDbId = createIndex(graph.getSubpathways(), SubpathwayNode::getDbId);
		pathwaysByStId = createIndex(graph.getSubpathways(), SubpathwayNode::getStId);

	}

	Map<String, EventNode> getEventsByStId() {
		return eventsByStId;
	}

	Map<String, EntityNode> getNodesByStId() {
		return nodesByStId;
	}

	Map<String, SubpathwayNode> getPathwaysByStId() {
		return pathwaysByStId;
	}

	Map<Long, EntityNode> getNodesByDbId() {
		return nodesByDbId;
	}

	public Map<Long, EventNode> getEventsByDbId() {
		return eventsByDbId;
	}

	Map<Long, SubpathwayNode> getPathwaysByDbId() {
		return pathwaysByDbId;
	}

	/**
	 * Helper method to make creating the indexes look nicer. The keys of the index are provided by the <em>key</em>
	 * param, the values are taken using {@link Function#identity}. If two elements have the same key, the first one
	 * remains.
	 *
	 * @param collection elements to be indexed
	 * @param key a function that will provide the key of each element
	 * @param <T> class of keys
	 * @param <V> class of values
	 * @return a map wrapped by {@link Collections#unmodifiableMap(Map)}
	 */
	private <T, V> Map<T, V> createIndex(Collection<V> collection, Function<V, T> key) {
		return Collections.unmodifiableMap(collection.stream().collect(Collectors.toMap(key, Function.identity(), (a,b) -> a)));
	}
}
