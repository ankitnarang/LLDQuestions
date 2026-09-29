package lrucache;

import java.util.*;

/**
 * Validator for the LRUCache implementation.
 * this class asserts expected values and
 * inspects the *real* internal DoublyLinkedList order via
 * LRUCache.keysMostToLeastRecentlyUsed() / size() (small read-only additions
 * made to LRUCache.java / DoublyLinkedList.java so this validator can check
 * actual internal state, not just infer it from get() results).
 * Run with: java Validator
 */
public class Validator {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== LRU Cache Validator ===\n");

        runTest("Basic put/get", Validator::testBasicPutAndGet);
        runTest("Get on missing key returns null", Validator::testGetMissingKeyReturnsNull);
        runTest("Get updates recency order", Validator::testRecencyUpdatesOnGet);
        runTest("Eviction on capacity overflow", Validator::testEvictionOnOverflow);
        runTest("Evicted key is truly gone", Validator::testEvictedKeyReturnsNull);
        runTest("Capacity-1 cache", Validator::testCapacityOneCache);
        runTest("Internal list order after inserts only", Validator::testInternalOrderAfterInsertsOnly);
        runTest("map/list size stay in sync (new keys only)", Validator::testSizeConsistencyWithNewKeysOnly);
        runTest("Updating an existing key (size/order consistency)", Validator::testUpdatingExistingKeyKeepsStateConsistent);
        runTest("Sequential multi-eviction order", Validator::testSequentialEvictionOrder);
        runTest("Re-insert after eviction becomes MRU", Validator::testReinsertAfterEviction);
        runTest("Null value handling", Validator::testNullValueHandling);
        runTest("Zero-capacity cache", Validator::testZeroCapacityCache);
        runTest("Stress test vs reference LinkedHashMap LRU", Validator::testStressAgainstReferenceLru);

        System.out.println("\n=== Summary ===");
        System.out.println("Passed: " + passed + ", Failed: " + failed);
        System.out.println(failed == 0
                ? "All tests passed!"
                : "Some tests failed - see [FAIL] lines above for details.");
    }

    // ------------------------------------------------------------------
    // Test runner / assertion helpers
    // ------------------------------------------------------------------

    private static void runTest(String name, Runnable test) {
        try {
            test.run();
        } catch (Throwable t) {
            failed++;
            System.out.println("[FAIL] " + name + " -> threw "
                    + t.getClass().getSimpleName() + ": " + t.getMessage());
        }
    }

    private static void check(String label, boolean condition, String detail) {
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + label);
        } else {
            failed++;
            System.out.println("  [FAIL] " + label + " -> " + detail);
        }
    }

    private static <T> void assertEqual(String label, T expected, T actual) {
        check(label, Objects.equals(expected, actual),
                "expected=" + expected + " actual=" + actual);
    }

    // ------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------

    private static void testBasicPutAndGet() {
        System.out.println("Test: Basic put/get");
        LRUCache<String, Integer> cache = new LRUCache<>(3);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.put("c", 3);

        assertEqual("get(a) == 1", 1, cache.get("a"));
        assertEqual("get(b) == 2", 2, cache.get("b"));
        assertEqual("get(c) == 3", 3, cache.get("c"));
        assertEqual("size() == 3", 3, cache.size());
    }

    private static void testGetMissingKeyReturnsNull() {
        System.out.println("Test: Get on missing key returns null");
        LRUCache<String, Integer> cache = new LRUCache<>(3);
        cache.put("a", 1);

        assertEqual("get(nonexistent) == null", null, cache.get("nonexistent"));
        assertEqual("get on empty-ish cache for wrong key == null", null, cache.get("z"));
    }

    private static void testRecencyUpdatesOnGet() {
        System.out.println("Test: Get updates recency order");
        LRUCache<String, Integer> cache = new LRUCache<>(3);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.put("c", 3);
        // After 3 puts, MRU -> LRU should be [c, b, a]
        assertEqual("order after 3 puts", List.of("c", "b", "a"), cache.keysMostToLeastRecentlyUsed());

        cache.get("a"); // touching 'a' should make it MRU
        assertEqual("order after get(a)", List.of("a", "c", "b"), cache.keysMostToLeastRecentlyUsed());
    }

    private static void testEvictionOnOverflow() {
        System.out.println("Test: Eviction on capacity overflow");
        LRUCache<String, Integer> cache = new LRUCache<>(3);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.put("c", 3);
        cache.get("a"); // order: [a, c, b], b is now LRU

        cache.put("d", 4); // should evict 'b'
        assertEqual("size stays at capacity", 3, cache.size());
        assertEqual("order after eviction", List.of("d", "a", "c"), cache.keysMostToLeastRecentlyUsed());
    }

    private static void testEvictedKeyReturnsNull() {
        System.out.println("Test: Evicted key is truly gone");
        LRUCache<String, Integer> cache = new LRUCache<>(2);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.put("c", 3); // evicts 'a' (LRU, never touched)

        assertEqual("get(a) == null after eviction", null, cache.get("a"));
        assertEqual("get(b) survives", 2, cache.get("b"));
        assertEqual("get(c) survives", 3, cache.get("c"));
    }

    private static void testCapacityOneCache() {
        System.out.println("Test: Capacity-1 cache");
        LRUCache<String, Integer> cache = new LRUCache<>(1);
        cache.put("a", 1);
        assertEqual("get(a) == 1", 1, cache.get("a"));

        cache.put("b", 2); // must evict 'a' immediately
        assertEqual("get(a) == null", null, cache.get("a"));
        assertEqual("get(b) == 2", 2, cache.get("b"));
        assertEqual("size stays 1", 1, cache.size());
    }

    private static void testInternalOrderAfterInsertsOnly() {
        System.out.println("Test: Internal list order after inserts only (no updates)");
        LRUCache<Integer, String> cache = new LRUCache<>(5);
        for (int i = 1; i <= 5; i++) {
            cache.put(i, "v" + i);
        }
        // Most recently inserted should be at the front.
        assertEqual("order matches insertion (reversed)",
                List.of(5, 4, 3, 2, 1), cache.keysMostToLeastRecentlyUsed());
    }

    private static void testSizeConsistencyWithNewKeysOnly() {
        System.out.println("Test: map/list size stay in sync when only inserting new keys");
        LRUCache<Integer, Integer> cache = new LRUCache<>(4);
        for (int i = 0; i < 10; i++) {
            cache.put(i, i * i);
        }
        assertEqual("size() reports capacity", 4, cache.size());
        assertEqual("internal list length matches size()",
                cache.size(), cache.keysMostToLeastRecentlyUsed().size());
    }

    private static void testUpdatingExistingKeyKeepsStateConsistent() {
        System.out.println("Test: Updating the value of an existing key");
        LRUCache<String, Integer> cache = new LRUCache<>(3);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.put("c", 3);

        cache.put("c", 30); // update existing key's value

        assertEqual("get(c) reflects the update", 30, cache.get("c"));
        assertEqual("size() is still 3 after an update (no new key added)", 3, cache.size());
        // This checks that the internal DLL doesn't silently grow a duplicate
        // node for a key that already existed. If put() unconditionally creates
        // a brand-new Node and re-links it to the front even on the
        // "key already exists" branch (in addition to updating/moving the old
        // node), the list ends up longer than the map - a stale, unreachable
        // duplicate node lingers in the list. That's a real bug to watch for,
        // not a hypothetical: see the note printed below if this fails.
        int listLen = cache.keysMostToLeastRecentlyUsed().size();
        check("internal list length matches map size() after update",
                listLen == cache.size(),
                "list length=" + listLen + " but size()=" + cache.size()
                        + "  --> put() likely creates a NEW Node unconditionally at the end "
                        + "of the method even when the key already existed, instead of only "
                        + "doing that in the 'else' (new-key) branch. The old node for the "
                        + "updated key is updated/moved but never removed from the list, so a "
                        + "stale duplicate accumulates and can later cause the wrong key to be "
                        + "evicted.");
    }

    private static void testSequentialEvictionOrder() {
        System.out.println("Test: Sequential multi-eviction order (inserts only, no updates)");
        LRUCache<Integer, Integer> cache = new LRUCache<>(3);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3); // order: [3,2,1]
        cache.put(4, 4); // evicts 1  -> order: [4,3,2]
        cache.put(5, 5); // evicts 2  -> order: [5,4,3]

        assertEqual("1 evicted", null, cache.get(1));
        assertEqual("2 evicted", null, cache.get(2));
        assertEqual("3 survives", 3, cache.get(3));
        assertEqual("4 survives", 4, cache.get(4));
        assertEqual("5 survives", 5, cache.get(5));
    }

    private static void testReinsertAfterEviction() {
        System.out.println("Test: Re-inserting a key after it was evicted");
        LRUCache<String, Integer> cache = new LRUCache<>(2);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.put("c", 3); // evicts 'a'
        assertEqual("a evicted", null, cache.get("a"));

        cache.put("a", 100); // fresh insert of 'a', should evict 'b' (current LRU) and become MRU
        assertEqual("a re-inserted with new value", 100, cache.get("a"));
        assertEqual("b evicted to make room", null, cache.get("b"));
        assertEqual("c still present", 3, cache.get("c"));
    }

    private static void testNullValueHandling() {
        System.out.println("Test: Storing a null value");
        LRUCache<String, Integer> cache = new LRUCache<>(2);
        cache.put("a", null);

        // Note: get() returns null both when a key is absent AND when a key is
        // present with a null value - this implementation's get() API can't
        // tell those two cases apart. That's a pre-existing design limitation
        // (common in simple LRUCache implementations), not something this test
        // fails on - just documenting it so it isn't a surprise.
        assertEqual("get(a) == null (value itself is null)", null, cache.get("a"));
        assertEqual("but the key IS present", 1, cache.size());
    }

    private static void testZeroCapacityCache() {
        System.out.println("Test: Zero-capacity cache");
        LRUCache<String, Integer> cache = new LRUCache<>(0);
        try {
            cache.put("a", 1);
            check("put() on a 0-capacity cache does not throw", true, "");
            assertEqual("get(a) == null (nothing should ever be retained)", null, cache.get("a"));
        } catch (RuntimeException e) {
            check("put() on a 0-capacity cache does not throw",
                    false,
                    "threw " + e.getClass().getSimpleName() + " (" + e.getMessage() + "). "
                            + "Cause: when map.size() (0) == capacity (0), put() tries to evict via "
                            + "list.removeLast(), but the list is empty, so removeLast() returns null "
                            + "and the very next line does map.remove(lru.key) on a null lru -> NPE. "
                            + "A capacity of 0 is an edge case worth guarding against explicitly.");
        }
    }

    private static void testStressAgainstReferenceLru() {
        System.out.println("Test: Stress test against a reference LRU (LinkedHashMap, inserts/gets only)");
        final int capacity = 5;
        LRUCache<Integer, Integer> cache = new LRUCache<>(capacity);
        LinkedHashMap<Integer, Integer> reference = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
                return size() > capacity;
            }
        };

        Random rand = new Random(42); // fixed seed for reproducibility
        int keySpace = 12;
        int mismatches = 0;

        for (int op = 0; op < 500; op++) {
            int key = rand.nextInt(keySpace);
            if (rand.nextBoolean() && !reference.containsKey(key)) {
                // Only insert *new* keys here, to isolate this test from the
                // known "updating an existing key" bug covered separately above.
                int value = op;
                cache.put(key, value);
                reference.put(key, value);
            } else {
                Integer expected = reference.get(key);
                Integer actual = cache.get(key);
                if (!Objects.equals(expected, actual)) {
                    mismatches++;
                    if (mismatches <= 5) {
                        System.out.println("    mismatch at op " + op + " for key " + key
                                + ": expected=" + expected + " actual=" + actual);
                    }
                }
            }
        }

        check("no mismatches vs reference LRU across 500 random new-key-only operations",
                mismatches == 0,
                mismatches + " mismatch(es) found (see details above)");
    }
}
