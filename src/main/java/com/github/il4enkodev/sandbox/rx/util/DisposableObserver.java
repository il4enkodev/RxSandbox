package com.github.il4enkodev.sandbox.rx.util;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


public interface DisposableObserver<T> extends Disposable, Observer<T> {

}
