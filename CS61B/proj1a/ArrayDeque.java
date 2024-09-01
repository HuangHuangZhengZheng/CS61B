public class ArrayDeque<T> {
    /**
     * The starting size of your array should be 8.
     * The amount of memory that your program uses at any given time
     * must be proportional to the number of items.
     * For example, if you add 10,000 items to the deque,
     * and then remove 9,999 items, you shouldn’t still be using
     * an array of length 10,000ish.
     * For arrays of length 16 or more, your usage factor should always
     * be at least 25%. For smaller arrays, your usage factor can
     * be arbitrarily low.
     */
    // 数据成员
    private T[] items;
    private int size;

    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 8;
    }

    // constant
    public void addFirst(T item) {
        T[] arr = (T[]) new Object[size + 1];
        System.arraycopy(items, 0, arr, 1, size);
        items = arr;
        items[0] = item;
        size++;
    }

    // constant
    public void addLast(T item) {
        T[] arr = (T[]) new Object[size + 1];
        System.arraycopy(items, 0, arr, 0, size);
        items = arr;
        items[size + 1 - 1] = item;
        size++;
    }

    public boolean isEmpty() {
        if (size == 0)
            return true;
        return false;
    }

    // constant
    public int size() {
        return size;
    }

    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(items[i]);
            System.out.print(" ");
        }
        System.out.println("");
    }

    // constant
    public T removeFirst() {
        T f = items[0];
        // resize?
        T[] arr = (T[]) new Object[size - 1];
        System.arraycopy(items, 1, arr, 0, size-1);
        items = arr;
        size--;
        return f;
    }

    // constant
    public T removeLast() {
        T last = items[size - 1];
        // resize?
        T[] arr = (T[]) new Object[size - 1];
        System.arraycopy(items, 0, arr, 0, size-1);
        items = arr;
        size--;
        return last;
    }

    // constant
    public T get(int index) {
        return items[index-1];
    }
}
