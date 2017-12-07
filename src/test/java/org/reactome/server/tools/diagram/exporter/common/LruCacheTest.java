package org.reactome.server.tools.diagram.exporter.common;

import org.junit.Assert;
import org.junit.Test;

public class LruCacheTest {

	@Test
	public void test() {
		final LruCache<String, String> lruCache = new LruCache<>(3);
		lruCache.put("a", "aaaa");
		lruCache.put("b", "bbbb");
		lruCache.put("c", "cccc");
		Assert.assertTrue(lruCache.has("a"));
		Assert.assertTrue(lruCache.has("b"));
		Assert.assertTrue(lruCache.has("c"));
		lruCache.put("d", "dddd");
		Assert.assertFalse(lruCache.has("a"));
		Assert.assertTrue(lruCache.has("d"));
		lruCache.put("b", "newB");  // b updated
		lruCache.put("e", "eeee");
		Assert.assertFalse(lruCache.has("c"));
		Assert.assertTrue(lruCache.has("b"));
		Assert.assertTrue(lruCache.has("e"));
	}

	@Test
	public void testGet() {
		final LruCache<String, String> lruCache = new LruCache<>();
		lruCache.put("a", "aaaa");
		lruCache.put("b", "nnnn");
		Assert.assertEquals("aaaa", lruCache.get("a"));
		Assert.assertEquals("nnnn", lruCache.get("b"));
	}

}
