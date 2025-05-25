package me.chrommob.minestore.common.placeholder;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SparseIndexedBlockDeque<T> implements Iterable<Map.Entry<Integer, T>> {
    private final int BLOCK_SIZE;
    private final TreeMap<Integer, IndexedBlockDeque<T>> dequeues = new TreeMap<>();

    public SparseIndexedBlockDeque() {
        this(1000);
    }

    public SparseIndexedBlockDeque(int blockSize) {
        BLOCK_SIZE = blockSize;
    }

    public int getBlockCount() {
        return dequeues.values().stream().mapToInt(IndexedBlockDeque::getBlockCount).sum();
    }

    public int getNumberOfDeques() {
        return dequeues.size();
    }

    public int size() {
        return dequeues.values().stream().mapToInt(IndexedBlockDeque::size).sum();
    }

    public void pushFirst(T value) {
        Map.Entry<Integer, IndexedBlockDeque<T>> entry = dequeues.firstEntry();
        if (entry == null) {
            entry = new AbstractMap.SimpleImmutableEntry<>(0, new IndexedBlockDeque<>(BLOCK_SIZE));
            dequeues.put(0, entry.getValue());
        }
        entry.getValue().pushFirst(value);
        shiftBehind(0);
    }

    public void pushLast(T value) {
        Map.Entry<Integer, IndexedBlockDeque<T>> entry = dequeues.lastEntry();
        if (entry == null) {
            pushFirst(value);
            return;
        }
        IndexedBlockDeque<T> deque = entry.getValue();
        deque.pushLast(value);
    }

    public void set(int index, T value, boolean shift) throws IllegalArgumentException {
        Map.Entry<Integer, IndexedBlockDeque<T>> entry = dequeues.floorEntry(index);
        Map.Entry<Integer, IndexedBlockDeque<T>> last = dequeues.higherEntry(index);

        if (entry != null) {
            if (entry.getKey() == index && shift) {
                entry.getValue().pushFirst(value);
                shiftBehind(index);
                return;
            }
            if (entry.getKey() + entry.getValue().size() == index) {
                entry.getValue().pushLast(value);
                if (shift) {
                    shiftBehind(index);
                }
                if (last != null && last.getKey() == index + 1 && !shift) {
                    dequeues.remove(last.getKey());
                    for (T t : last.getValue()) {
                        entry.getValue().pushLast(t);
                    }
                    return;
                }

                return;
            }
            if (index <= entry.getKey() + entry.getValue().size()) {
                entry.getValue().set(index - entry.getKey(), value);
                return;
            }
        }

        if (last != null) {
            if (last.getKey() == index) {
                last.getValue().pushFirst(value);
                if (shift) {
                    shiftBehind(index);
                }
                return;
            }
            if (last.getKey() == index + 1 && !shift) {
                dequeues.remove(last.getKey());
                dequeues.put(index, last.getValue());
                last.getValue().pushFirst(value);
                return;
            }
        }

        IndexedBlockDeque<T> deque = new IndexedBlockDeque<>(BLOCK_SIZE);
        deque.pushFirst(value);
        dequeues.put(index, deque);
        if (shift) {
            shiftBehind(index);
        }
    }

    private void shiftBehind(int index) {
        Map<Integer, IndexedBlockDeque<T>> toShift = new HashMap<>();
        Iterator<Map.Entry<Integer, IndexedBlockDeque<T>>> it = dequeues.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, IndexedBlockDeque<T>> entry = it.next();
            if (entry.getKey() > index) {
                toShift.put(entry.getKey(), entry.getValue());
                it.remove();
            }
        }

        for (Map.Entry<Integer, IndexedBlockDeque<T>> entry : toShift.entrySet()) {
            dequeues.put(entry.getKey() + 1, entry.getValue());
        }
    }

    public T getFirst() {
        Map.Entry<Integer, IndexedBlockDeque<T>> entry = dequeues.firstEntry();
        if (entry == null) {
            return null;
        }
        return entry.getValue().getFirst();
    }

    public T pollFirst() {
        Map.Entry<Integer, IndexedBlockDeque<T>> entry = dequeues.firstEntry();
        if (entry == null) {
            return null;
        }
        return entry.getValue().pollFirst();
    }

    public T getLast() {
        Map.Entry<Integer, IndexedBlockDeque<T>> entry = dequeues.lastEntry();
        if (entry == null) {
            return null;
        }
        return entry.getValue().getLast();
    }

    public T pollLast() {
        Map.Entry<Integer, IndexedBlockDeque<T>> entry = dequeues.lastEntry();
        if (entry == null) {
            return null;
        }
        return entry.getValue().pollLast();
    }

    public T get(int index) {
        Map.Entry<Integer, IndexedBlockDeque<T>> entry = dequeues.floorEntry(index);
        if (entry == null) {
            return null;
        }
        int dequeIndex = index - entry.getKey();
        IndexedBlockDeque<T> deque = entry.getValue();
        if (deque.size() <= dequeIndex) {
            return null;
        }
        return deque.get(dequeIndex);
    }


    public boolean has(int index) {
        return get(index) != null;
    }

    @Override
    public @NotNull Iterator<Map.Entry<Integer, T>> iterator() {
        return new SparseDequeIterator();
    }

    public void clear() {
        dequeues.clear();
    }

    private class SparseDequeIterator implements Iterator<Map.Entry<Integer, T>> {
        private Map.Entry<Integer, IndexedBlockDeque<T>> deque = dequeues.firstEntry();
        private int index = 0;

        @Override
        public boolean hasNext() {
            if (deque == null) {
                return false;
            }
            if (index < deque.getValue().size()) {
                return true;
            }
            deque = dequeues.higherEntry(deque.getKey());
            index = 0;
            return deque != null;
        }

        @Override
        public Map.Entry<Integer, T> next() {
            if (deque == null) {
                throw new IllegalStateException("Iterator is not initialized!");
            }
            int totalIndex = deque.getKey() + index;
            T value = deque.getValue().get(index++);
            return new AbstractMap.SimpleImmutableEntry<>(totalIndex, value);
        }
    }
}
