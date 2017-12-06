package org.reactome.server.tools.diagram.exporter.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LruCache<K, V> {

	private final Map<K, V> map = new HashMap<>();
	private final List<K> lru;
	private final int capacity;

	public LruCache() {
		this.capacity = 5;
		lru = new LinkedList<>();
	}

	public LruCache(int capacity) {
		this.capacity = capacity;
		lru = new LinkedList<>();
	}

	public boolean has(K key) {
		return map.containsKey(key);
	}

	public V get(K key) {
		return map.get(key);
	}

	public void put(K key, V value) {
		lru.remove(key);
		lru.add(key);
		if (lru.size() > capacity) {
			map.remove(lru.get(0));
			lru.remove(0);
		}
		map.put(key, value);

	}

}
