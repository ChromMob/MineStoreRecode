package me.chrommob.minestore.common.placeholder;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class IndexedBlockDeque<T> implements Iterable<T>, Deque<T> {
    private final int BLOCK_SIZE;

    @Override
    public @NotNull Iterator<T> iterator() {
        return new IndexedBlockDequeIterator(false);
    }

    private class IndexedBlockDequeIterator implements Iterator<T> {
        public final boolean reverse;
        private int index;
        public IndexedBlockDequeIterator(boolean reverse) {
            this.reverse = reverse;
            if (reverse) {
                index = size - 1;
            } else {
                index = 0;
            }
        }

        @Override
        public boolean hasNext() {
            return has(index);
        }

        @Override
        public T next() {
            if (reverse) {
                return get(index--);
            }
            return get(index++);
        }
    }

    private final RootBlock root;
    private final List<NormalBlock> front = new ArrayList<>();
    private final List<NormalBlock> back = new ArrayList<>();
    private int size = 0;

    public IndexedBlockDeque(int blockSize) {
        if (blockSize % 2 == 1) {
            blockSize++;
        }
        BLOCK_SIZE = blockSize;
        root = new RootBlock();
    }

    public IndexedBlockDeque() {
        this(1000);
    }

    public int getBlockCount() {
        return front.size() + 2 + back.size();
    }

    public boolean has(int index) {
        return get(index) != null;
    }

    @Override
    public T pollFirst() {
        MutableWrapper mutableWrapper = getMutableWrapper(0);
        if (mutableWrapper == null) {
            return null;
        }
        mutableWrapper.set(null);
        return mutableWrapper.get();
    }

    @Override
    public T pollLast() {
        MutableWrapper mutableWrapper = getMutableWrapper(size - 1);
        if (mutableWrapper == null) {
            return null;
        }
        mutableWrapper.set(null);
        return mutableWrapper.get();
    }

    public void pushFirst(T value) {
        size++;
        if (!root.isFrontFull()) {
            root.addFront(value);
            return;
        }
        NormalBlock block = getLast(front);
        if (block == null || block.isFull()) {
            block = new NormalBlock(false);
            front.add(block);
        }
        block.add(value);
    }

    public void pushLast(T value) {
        size++;
        if (!root.isBackFull()) {
            root.addBack(value);
            return;
        }
        NormalBlock block = getLast(back);
        if (block == null || block.isFull()) {
            block = new NormalBlock(true);
            back.add(block);
        }
        block.add(value);
    }

    private <F> F getLast(List<F> list) {
        if (list.isEmpty()) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    private <F> F getFirst(List<F> list) {
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public boolean set(int originalIndex, T value) {
        MutableWrapper mutableWrapper = getMutableWrapper(originalIndex);
        if (mutableWrapper != null) {
            mutableWrapper.set(value);
            return true;
        }

        return false;
    }

    public T get(int originalIndex) {
        MutableWrapper mutableWrapper = getMutableWrapper(originalIndex);
        if (mutableWrapper != null) {
            return mutableWrapper.get();
        }
        return null;
    }

    private MutableWrapper getMutableWrapper(int originalIndex) {
        int frontSize = front.size() * BLOCK_SIZE;
        NormalBlock last = getLast(front);
        if (last != null) {
            frontSize -= last.space();
        }

        int index = originalIndex;
        if (originalIndex < frontSize) {
            if (last != null) {
                index += last.space();
            }
            int blockLocation = index / BLOCK_SIZE;
            NormalBlock block = front.get(front.size() - blockLocation - 1);
            if (block != null) {
                return new MutableWrapper(block, index % BLOCK_SIZE, true);
            } else {
                return null;
            }
        }

        index -= frontSize;
        int rootSize = root.size();
        if (index < rootSize) {
            return new MutableWrapper(root, root.getFirst() + index, false);
        }

        index -= rootSize;
        int backSize = back.size() * BLOCK_SIZE;
        NormalBlock first = getFirst(back);
        if (first != null) {
            backSize -= first.space();
        }
        if (index < backSize) {
            int blockLocation = index / BLOCK_SIZE;
            if (first != null) {
                index += first.space();
            }
            NormalBlock block = back.get(blockLocation);
            if (block != null) {
                return new MutableWrapper(block, index % BLOCK_SIZE, false);
            }
        }
        return null;
    }

    class MutableWrapper {
        private final Block<T> block;
        private final int index;
        private final T value;
        private final boolean isFront;
        public MutableWrapper(Block<T> block, int index, boolean isFront) {
            this.block = block;
            this.index = index;
            this.value = block.get(index);
            this.isFront = isFront;
        }

        public T get() {
            return value;
        }
        public void set(T value) {
            block.set(index, value);
            if (!block.isEmpty()) {
                return;
            }
            if (isFront) {
                front.remove(front.size() - 1);
            } else {
                back.remove(0);
            }
        }
    }

    interface Block<T> {
        T get(int index);
        boolean set(int index, T value);
        boolean isEmpty();
    }

    class NormalBlock implements Block<T> {
        private final boolean reverse;
        private int index;
        @SuppressWarnings("unchecked")
        private final T[] values = (T[]) new Object[BLOCK_SIZE];

        public NormalBlock(boolean reverse) {
            this.reverse = reverse;
            if (reverse) {
                index = 0;
            } else {
                index = BLOCK_SIZE - 1;
            }
        }

        public void add(T value) {
            if (reverse) {
                values[index++] = value;
            } else {
                values[index--] = value;
            }
        }

        public boolean isFull() {
            if (reverse) {
                return index == BLOCK_SIZE;
            }
            return index == -1;
        }

        @Override
        public T get(int index) {
            return values[index];
        }

        private int space() {
            if (reverse) {
                return BLOCK_SIZE - index;
            }
            return index + 1;
        }

        @Override
        public boolean set(int index, T value) {
            if (values[index] != null) {
                values[index] = value;
                if (value == null) {
                    if (index != this.index + 1 && !reverse) {
                        throw new IllegalStateException("You can only remove from the front!");
                    }
                    if (this.index - 1 != index && reverse) {
                        throw new IllegalStateException("You can only remove from the back!");
                    }
                    if (reverse) {
                        this.index--;
                    } else {
                        this.index++;
                    }
                    size--;
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean isEmpty() {
            if (reverse) {
                return index == 0;
            }
            return index == BLOCK_SIZE - 1;
        }
    }

    class RootBlock implements Block<T> {
        private final int middle = (2 * BLOCK_SIZE) / 2;
        private int first = middle;
        private int last = middle;
        @SuppressWarnings("unchecked")
        private final T[] values = (T[]) new Object[2 * BLOCK_SIZE + 1];

        public void addFront(T value) {
            values[first--] = value;
            if (last == middle) {
                last++;
            }
        }

        @Override
        public T get(int index) {
            return values[index];
        }

        @Override
        public boolean set(int index, T value) {
            if (values[index] == null) {
                return false;
            }
            if (value == null) {
                final int firstUsed = getFirst();
                final int lastUsed = getLast();
                if (index != firstUsed && index != lastUsed) {
                    throw new IllegalStateException("You can only remove the first or last element!");
                }
                if (index == firstUsed && index == lastUsed) {
                    first = middle;
                    last = middle;
                } else {
                    if (index == firstUsed) {
                        first++;
                    }
                    if (index == lastUsed) {
                        last--;
                    }
                }
                size--;
            }
            values[index] = value;
            return true;
        }

        public void addBack(T value) {
            values[last++] = value;
            if (first == middle) {
                first--;
            }
        }

        public int getFirst() {
            return first + 1;
        }

        public int getLast() {
            return last - 1;
        }

        public boolean isFrontFull() {
            return first == -1;
        }

        public boolean isBackFull() {
            return last == 2 * BLOCK_SIZE + 1;
        }

        public int size() {
            if (first == middle && last == middle) {
                return 0;
            }
            int space = 1;
            if (getFirst() != middle) {
                space += BLOCK_SIZE - getFirst();
            }
            if (getLast() != middle) {
                space += getLast() - middle;
            }
            return space;
        }

        public boolean isEmpty() {
            return false;
        }

        public void clear() {
            first = middle;
            last = middle;
            Arrays.fill(values, null);
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public T getLast() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return get(size - 1);
    }

    @Override
    public T peekFirst() {
        return get(0);
    }

    @Override
    public T peekLast() {
        return get(size - 1);
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return false;
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        return false;
    }

    @Override
    public boolean add(T t) {
        addLast(t);
        return true;
    }

    @Override
    public boolean offer(T t) {
        return offerLast(t);
    }

    @Override
    public T remove() {
        return removeFirst();
    }

    @Override
    public T poll() {
        return pollFirst();
    }

    @Override
    public T element() {
        return getFirst();
    }

    @Override
    public T peek() {
        return peekFirst();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        for (T t : c) {
            addLast(t);
        }
        return true;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        size = 0;
        front.clear();
        back.clear();
        root.clear();
    }

    @Override
    public void push(T t) {
        addFirst(t);
    }

    @Override
    public T pop() {
        return removeFirst();
    }

    @Override
    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        for (T t : this) {
            if (t.equals(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public T getFirst() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return get(0);
    }

    @Override
    public void addFirst(T t) {
        if (t == null) {
            throw new NullPointerException();
        }
        pushFirst(t);
    }

    @Override
    public void addLast(T t) {
        if (t == null) {
            throw new NullPointerException();
        }
        pushLast(t);
    }

    @Override
    public boolean offerFirst(T t) {
        pushFirst(t);
        return true;
    }

    @Override
    public boolean offerLast(T t) {
        pushLast(t);
        return true;
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return get(0);
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return get(size - 1);
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        Object[] array = new Object[size];
        for (int i = 0; i < size; i++) {
            array[i] = get(i);
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T1> T1 @NotNull [] toArray(@NotNull T1[] a) {
        if (a.length < size) {
            return (T1[]) toArray();
        }
        for (int i = 0; i < size; i++) {
            a[i] = (T1) get(i);
        }
        return a;
    }

    @Override
    public @NotNull Iterator<T> descendingIterator() {
        return new IndexedBlockDequeIterator(true);
    }
}
