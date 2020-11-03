package com.github.il4enkodev.sandbox.rx.util;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.ObservableConverter;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.internal.functions.ObjectHelper;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public final class IndexedValue<T> {

    public static <T> ObservableTransformer<T, IndexedValue<T>> zipWithIndex() {
        return upstream -> upstream.zipWith(indexes(), IndexedValue::new);
    }

    public static <T> ObservableConverter<T, Maybe<IndexedValue<T>>> firstMatch(final Predicate<? super T> condition) {
        ObjectHelper.requireNonNull(condition, "condition is null");
        return upstream -> upstream.takeUntil(condition)
                .collect(MatchIndexer.<T>deferredCreate(condition), MatchIndexer::accept)
                .flatMapMaybe(MatchIndexer::indexedValue);
    }

    private static Observable<Integer> indexes() {
        return Observable.generate(AtomicInteger::new, (current, emitter) -> {
            emitter.onNext(current.getAndIncrement());
        });
    }

    private final int index;
    private final T value;

    private IndexedValue(T value, int index) {
        this.index = index;
        this.value = value;
    }

    public int index() {
        return index;
    }

    public T value() {
        return value;
    }

    @Override
    public String toString() {
        return "IndexedValue[" + index() + "]{" + value() + '}';
    }

    private static class MatchIndexer<T> implements Consumer<T> {

        static <T> Callable<MatchIndexer<T>> deferredCreate(final Predicate<? super T> condition) {
            return () -> new MatchIndexer<>(condition);
        }

        int index;
        T value;
        final Predicate<? super T> condition;

        MatchIndexer(Predicate<? super T> condition) {
            this.condition = condition;
        }

        Maybe<IndexedValue<T>> indexedValue() {
            return value != null ? Maybe.just(new IndexedValue<>(value, index)) : Maybe.empty();
        }

        @Override
        public void accept(T next) throws Exception {
            if (condition.test(next)){
                value = next;
            } else {
                ++index;
            }
        }
    }
}