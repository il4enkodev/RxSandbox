package com.github.il4enkodev.sandbox.rx.collection;

import java.util.ArrayList;
import java.util.Collection;

public class MutableList<T> extends ArrayList<T> {

    public MutableList() {
    }

    public MutableList(int initialCapacity) {
        super(initialCapacity);
    }

    public MutableList(Collection<? extends T> c) {
        super(c);
    }

    public ImmutableList<T> toImmutableList() {
        return ImmutableList.from(this);
    }
}
