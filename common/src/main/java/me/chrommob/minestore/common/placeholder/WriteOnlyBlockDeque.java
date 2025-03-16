package me.chrommob.minestore.common.placeholder;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class WriteOnlyBlockDeque<T> implements Iterable<T> {
    private final int BLOCK_SIZE;

    @Override
    public @NotNull Iterator<T> iterator() {
        return new WriteOnlyDequeIterator();
    }

    private class WriteOnlyDequeIterator implements Iterator<T> {
        private int index = 0;

        @Override
        public boolean hasNext() {
            return has(index);
        }

        @Override
        public T next() {
            return get(index++);
        }
    }

    private final Block root;
    private final List<Block> front = new ArrayList<>();
    private final List<Block> back = new ArrayList<>();
    private int size = 0;

    public WriteOnlyBlockDeque(int blockSize) {
        BLOCK_SIZE = blockSize;
        root = new Block();
    }

    public WriteOnlyBlockDeque() {
        this(1000);
    }

    public int getBlockCount() {
        return front.size() + 1 + back.size();
    }

    public boolean has(int index) {
        return get(index) != null;
    }

    public T getFirst() {
        return get(0);
    }

    public T getLast() {
        return get(size - 1);
    }

    public void addFront(T value) {
        size++;
        if (!root.isFull()) {
            root.add(value);
            return;
        }
        Block block = getLast(front);
        if (block == null || block.isFull()) {
            block = new Block();
            front.add(block);
        }
        block.add(value);
    }

    public void addBack(T value) {
        size++;
        Block block = getLast(back);
        if (block == null || block.isFull()) {
            block = new Block();
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

    public T get(int originalIndex) {
        int index = originalIndex;
        Block last = getLast(front);
        if (last != null) {
            index += last.space();
        }
        int blockLocation = index / (BLOCK_SIZE );
        index = index % BLOCK_SIZE;
        if (blockLocation < front.size()) {
            Block block = front.get(front.size() - blockLocation - 1);
            if (block != null) {
                return block.get(index);
            }
        }

        if (blockLocation == front.size()) {
            index += root.space();
            if (index < BLOCK_SIZE) {
                return root.get(index);
            }
        }

        int frontSize = (front.size() + 1) * BLOCK_SIZE;
        if (last != null) {
            frontSize -= last.space();
        }
        frontSize -= root.space();
        index = originalIndex - frontSize;
        blockLocation = index / BLOCK_SIZE;
        index = index % BLOCK_SIZE;
        if (blockLocation >= back.size()) {
            return null;
        }
        Block block = back.get(blockLocation);
        if (block != null) {
            index = BLOCK_SIZE - index - 1;
            return block.get(index);
        }
        return null;
    }

    class Block {
        private int index = BLOCK_SIZE - 1;
        @SuppressWarnings("unchecked")
        private final T[] values = (T[]) new Object[BLOCK_SIZE];

        public void add(T value) {
            values[index--] = value;
        }

        public boolean isFull() {
            return index == -1;
        }

        private T get(int index) {
            return values[index];
        }

        private int space() {
            return index + 1;
        }
    }

    public int size() {
        return size;
    }
}
