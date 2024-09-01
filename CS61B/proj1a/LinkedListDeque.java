public class LinkedListDeque<Item> {
    // 双链表节点定义
    private static class Node<Item> {
        public Item item;
        public Node<Item> next;
        public Node<Item> prev;

        public Node(Node<Item> p, Item i, Node<Item> n) {
            item = i;
            next = n;
            prev = p;
        }

    }

    // 数据成员
    private Node<Item> sentinel;
    private int size;

    public LinkedListDeque() {
        sentinel = new Node<Item>(sentinel, null, sentinel);
        size = 0;
    }

    public void addFirst(Item item) {
        size++;
        Node<Item> p = new Node<Item>(sentinel, item, sentinel.next);
        sentinel.next = p;
        if (p.next != null) {
            p.next.prev = p;
        }else{
            p.prev = sentinel;
            sentinel.prev = p;
        }
    }

    public void addLast(Item item) {
        // 或者是新节点插入？
        size++;
        Node<Item> p = new Node<Item>(sentinel.prev, item, sentinel);
        sentinel.prev = p;
        if (p.prev != null) {
            p.prev.next = p;
        }else{
            p.prev = sentinel;
            sentinel.next = p;
        }
    }

    public boolean isEmpty() {
        if (size == 0) {
            return true;
        } else {
            return false;
        }
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        Node<Item> p = sentinel.next;
        while (p != sentinel) {
            System.out.print(p.item);
            System.out.print(" ");
            p = p.next;
        }
        System.out.println("");
    }

    /**
     * Removes and returns the item at the front of the deque.
     * If no such item exists, returns null.
     */
    public Item removeFirst() {
        size--;
        Node<Item> p = sentinel.next;
        sentinel.next = sentinel.next.next;
        return p.item;
    }

    /**
     * Removes and returns the item at the back of the deque.
     * If no such item exists, returns null.
     */
    public Item removeLast() {
        size--;
        Node<Item> p = sentinel.prev;
        sentinel.prev = sentinel.prev.prev;
        return p.item;

    }

    /**
     * Gets the item at the given index, where 0 is the front,
     * 1 is the next item, and so forth.
     * If no such item exists, returns null. Must not alter the deque!
     */
    public Item get(int index) {
        if (index * index > size * size) {
            return null;
        }

        Node<Item> p = sentinel;
        if (index >= 0) {
            while (index >= 0) {
                p = p.next;
                index--;
            }
        } else {
            while (index < 0) {
                p = p.prev;
                index++;
            }
        }

        return p.item;
    }

    public Item getRecursive(int index) {
        return get(index, sentinel.next);
    }

    private Item get(int i, Node<Item> p) {
        if (i == 0) {
            return p.item;
        }
        return get(i - 1, p.next);
    }
}
