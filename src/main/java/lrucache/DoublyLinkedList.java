package lrucache;

public class DoublyLinkedList<K,V> {
    private final Node<K,V> head;
    private final Node<K,V> tail;

    public DoublyLinkedList() {
        // TODO: Create dummy head and tail nodes with null key/value
        // TODO: Link head.next to tail and tail.prev to head
        head = new Node<>(null,null);
        tail = new Node<>(null,null);
        head.next = tail;
        tail.prev = head;
    }

    public void addFirst(Node<K,V> node){
        //TODO: Insert node as 1st element, right after head
        /*
            Steps
                1. node.next = head.next
                2. node.prev = head
                3. head.next.prev = node
                4. head.next = node
         */
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    public void remove(Node<K,V> node){
        // TODO : Detach node from current position
        /*
            Steps
                1. node.prev.next = node.next
                2. node.next.prev = node.prev
         */
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    public void removeNodeAndAddToFront(Node<K,V> node){
        // TODO : Remove the mode and add to front, after head
        remove(node);
        addFirst(node);
    }

    public Node<K,V> removeLast(){
        // TODO : Remove node from last and return it
        /*
            Steps
                1. Node<K,V> lastNode = tail.prev
                2. remove node using remove() method
                3. return lastNode
         */
        Node<K,V> lastNode = tail.prev;
        remove(lastNode);
        return lastNode;
    }

    // Added for Validator: exposes the real internal ordering (most-recently-used
    // first) by walking the actual list, instead of inferring order indirectly
    // through get()/put(). Read-only, does not change any list behavior.
    public java.util.List<K> keysMostToLeastRecentlyUsed() {
        java.util.List<K> keys = new java.util.ArrayList<>();
        Node<K, V> current = head.next;
        while (current != tail) {
            keys.add(current.key);
            current = current.next;
        }
        return keys;
    }

}
