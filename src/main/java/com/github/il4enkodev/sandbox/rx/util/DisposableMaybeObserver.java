package com.github.il4enkodev.sandbox.rx.util;

import io.reactivex.MaybeObserver;
import io.reactivex.disposables.Disposable;

public interface DisposableMaybeObserver<T> extends Disposable, MaybeObserver<T> {
}
