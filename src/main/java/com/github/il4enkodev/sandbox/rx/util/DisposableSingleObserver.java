package com.github.il4enkodev.sandbox.rx.util;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

public interface DisposableSingleObserver<T>
extends Disposable, SingleObserver<T> {

}
