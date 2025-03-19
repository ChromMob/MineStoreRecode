package me.chrommob.minestore.common.placeholder;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SparseWriteOnlyBlockDeque<T> implements Iterable<Map.Entry<Integer, T>> {

    private final TreeMap<Integer, WriteOnlyBlockDeque<T>> dequeues = new TreeMap<>();

    public int getBlockCount() {
        return dequeues.values().stream().mapToInt(WriteOnlyBlockDeque::getBlockCount).sum();
    }

    public int getNumberOfDeques() {
        return dequeues.size();
    }

    public int size() {
        return dequeues.values().stream().mapToInt(WriteOnlyBlockDeque::size).sum();
    }

    public void pushFront(T value) {
        Map.Entry<Integer, WriteOnlyBlockDeque<T>> entry = dequeues.firstEntry();
        if (entry == null) {
            entry = new AbstractMap.SimpleImmutableEntry<>(0, new WriteOnlyBlockDeque<>());
            dequeues.put(0, entry.getValue());
        }
        entry.getValue().addFront(value);
        shiftBehind(0);
    }

    public void pushBack(T value) {
        Map.Entry<Integer, WriteOnlyBlockDeque<T>> entry = dequeues.lastEntry();
        if (entry == null) {
            pushFront(value);
            return;
        }
        WriteOnlyBlockDeque<T> deque = entry.getValue();
        deque.addBack(value);
    }

    public void set(int index, T value, boolean shift) throws IllegalArgumentException {
        Map.Entry<Integer, WriteOnlyBlockDeque<T>> entry = dequeues.floorEntry(index);
        Map.Entry<Integer, WriteOnlyBlockDeque<T>> last = dequeues.higherEntry(index);

        if (entry != null) {
            if (entry.getKey() == index && shift) {
                entry.getValue().addFront(value);
                shiftBehind(index);
                return;
            }
            if (entry.getKey() + entry.getValue().size() == index) {
                entry.getValue().addBack(value);
                if (shift) {
                    shiftBehind(index);
                }
                if (last != null && last.getKey() == index + 1 && !shift) {
                    dequeues.remove(last.getKey());
                    for (T t : last.getValue()) {
                        entry.getValue().addBack(t);
                    }
                    return;
                }

                return;
            }
            if (index <= entry.getKey() + entry.getValue().size()) {
                throw new IllegalArgumentException("Index " + index + " is already occupied!");
            }
        }

        if (last != null) {
            if (last.getKey() == index) {
                last.getValue().addFront(value);
                if (shift) {
                    shiftBehind(index);
                }
                return;
            }
            if (last.getKey() == index + 1 && !shift) {
                dequeues.remove(last.getKey());
                dequeues.put(index, last.getValue());
                last.getValue().addFront(value);
                return;
            }
        }

        WriteOnlyBlockDeque<T> deque = new WriteOnlyBlockDeque<>();
        deque.addFront(value);
        dequeues.put(index, deque);
        if (shift) {
            shiftBehind(index);
        }
    }

    private void shiftBehind(int index) {
        Map<Integer, WriteOnlyBlockDeque<T>> toShift = new HashMap<>();
        Iterator<Map.Entry<Integer, WriteOnlyBlockDeque<T>>> it = dequeues.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, WriteOnlyBlockDeque<T>> entry = it.next();
            if (entry.getKey() > index) {
                toShift.put(entry.getKey(), entry.getValue());
                it.remove();
            }
        }

        for (Map.Entry<Integer, WriteOnlyBlockDeque<T>> entry : toShift.entrySet()) {
            dequeues.put(entry.getKey() + 1, entry.getValue());
        }
    }

    public T getFirst() {
        Map.Entry<Integer, WriteOnlyBlockDeque<T>> entry = dequeues.firstEntry();
        if (entry == null) {
            return null;
        }
        return entry.getValue().getFirst();
    }

    public T getLast() {
        Map.Entry<Integer, WriteOnlyBlockDeque<T>> entry = dequeues.lastEntry();
        if (entry == null) {
            return null;
        }
        return entry.getValue().getLast();
    }

    public T get(int index) {
        Map.Entry<Integer, WriteOnlyBlockDeque<T>> entry = dequeues.floorEntry(index);
        if (entry == null) {
            return null;
        }
        int dequeIndex = index - entry.getKey();
        WriteOnlyBlockDeque<T> deque = entry.getValue();
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
        private Map.Entry<Integer, WriteOnlyBlockDeque<T>> deque = dequeues.firstEntry();
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
