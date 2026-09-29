package lrucache;

import java.util.HashMap;
import java.util.Map;

public class LRUCache<K,V> {
    private final int capacity;
    private final Map<K,Node<K,V>> map;
    private final DoublyLinkedList<K,V> list;

    public LRUCache(int capacity) {
        // TODO: Initialize capacity, create empty HashMap, create new DoublyLinkedList
        this.capacity = capacity;
        this.map = new HashMap<>();
        this.list = new DoublyLinkedList<>();
    }

    public synchronized V get(K key){
        // TODO: Implement get operation
        /*
            Steps
                1. If map doesn't contain key , return null
                2. Get the node from the map
                3. move node as 1st node (MRU)
                4. return node's value
         */

        if(!map.containsKey(key)){
            return null;
        }

        Node<K,V> node = map.get(key);
        list.removeNodeAndAddToFront(node);
        return node.value;
    }

    public synchronized void put(K key, V value) {
        // TODO: Implement put operation
        /*
            Steps
                1. if key exists in map, update value and move to front (most recently used)
                2.If key is new , and if capacity is max, evict LRU item
                    i. Remove the node from last
                    ii. Remove from map
                    ii. create new node with key and value and add to front
                    ii. add new node in map
         */

        if (capacity <= 0) {
            return; // nothing can be cached
        }

        if(map.containsKey(key)){
            Node<K,V> node = map.get(key);
            node.value = value;
            list.removeNodeAndAddToFront(node);
        } else {
            if(map.size() == capacity) {
                Node<K,V> lruNode = list.removeLast();
                map.remove(lruNode.key);
            }
            Node<K,V> newNode = new Node<>(key,value);
            list.addFirst(newNode);
            map.put(key,newNode);
        }

    }

    // Added for Validator: read-only introspection, does not change cache behavior.
    public synchronized int size() {
        return map.size();
    }

    // Added for Validator: exposes the real internal DLL order (most-recently-used
    // first) so tests can check the actual data structure, not just get()/put() results.
    public synchronized java.util.List<K> keysMostToLeastRecentlyUsed() {
        return list.keysMostToLeastRecentlyUsed();
    }

}
