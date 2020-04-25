package com.github.il4enkodev.sandbox.rx.util;

import io.reactivex.FlowableSubscriber;
import io.reactivex.disposables.Disposable;

public interface DisposableSubscriber<T> extends FlowableSubscriber<T>, Disposable {
}
