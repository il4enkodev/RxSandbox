package com.github.il4enkodev.sandbox.rx.collection;

import java.util.*;

public abstract class ImmutableList<T> extends AbstractList<T> implements List<T> {

    public static <T> ImmutableList<T> empty() {
        //noinspection unchecked
        return (ImmutableList<T>) EMPTY;
    }

    public static <T> ImmutableList<T> singleton(T value) {
        Objects.requireNonNull(value, "value is null");
        return new ImmutableList.Singleton<>(value);
    }

    public static <T> ImmutableList<T> from(List<? extends T> list) {
        Objects.requireNonNull(list, "list is null");
        switch (list.size()) {
            case 0:
                return empty();
            case 1:
                return singleton(list.get(0));
            default:
                //noinspection unchecked
                return new ImmutableList.Default<>((T[]) list.toArray());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> ImmutableList<T> from(Collection<? extends T> collection) {
        Objects.requireNonNull(collection, "collection is null");
        if (collection.isEmpty()) {
            return empty();
        } else return from((T[]) collection.toArray());
    }

    public static <T> ImmutableList<T> from(Iterable<? extends T> iterable) {
        Objects.requireNonNull(iterable, "iterable is null");
        final Iterator<? extends T> iterator = iterable.iterator();
        if (!iterator.hasNext()) {
            return empty();
        } else {
            T next = iterator.next();
            if (!iterator.hasNext()) {
                return singleton(next);
            } else {
                int capacity = 16, pos = 0;
                Object[] array = new Object[capacity];
                array[0] = next;
                do {
                    next = iterator.next();
                    if (pos == capacity - 1) {
                        array = Arrays.copyOf(array, (capacity <<= 1));
                    }
                    array[++pos] = next;
                } while (iterator.hasNext());

                //noinspection unchecked
                return new ImmutableList.Default<>((T[]) array);
            }
        }
    }

    @SafeVarargs
    public static <T> ImmutableList<T> from(T... array) {
        Objects.requireNonNull(array, "array is null");
        switch (array.length) {
            case 0:
                return empty();
            case 1:
                return singleton(array[0]);
            default:
                return new ImmutableList.Default<>(Arrays.copyOf(array, array.length));
        }
    }

    public MutableList<T> toMutableList() {
        return new MutableList<>(this);
    }

    private ImmutableList() {
    }

    private static final class Default<T> extends ImmutableList<T> {

        private final T[] elements;

        Default(T[] elements) {
            this.elements = elements;
        }

        @Override
        public T get(int index) {
            if (index < 0 || index >= elements.length) {
                throw new IndexOutOfBoundsException();
            }
            return elements[index];
        }

        @Override
        public int size() {
            return elements.length;
        }

        @Override
        public Object[] toArray() {
            final Object[] array = new Object[elements.length];
            System.arraycopy(elements, 0, array, 0, elements.length);
            return array;
        }
    }

    private static final class Singleton<T> extends ImmutableList<T> {

        private final T value;

        private Singleton(T value) {
            this.value = value;
        }

        @Override
        public T get(int index) {
            if (index != 0) {
                throw new IndexOutOfBoundsException();
            }
            return value;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public Object[] toArray() {
            return new Object[] {value};
        }
    }

    private static final Object[] EMPTY_ARRAY = new Object[0];
    private static final ImmutableList<Object> EMPTY = new ImmutableList.Empty<>();
    private static final class Empty<T> extends ImmutableList<T> {

        @Override
        public T get(int index) {
            throw new IndexOutOfBoundsException();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Object[] toArray() {
            return EMPTY_ARRAY;
        }
    }
}
