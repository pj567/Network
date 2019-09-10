package com.hzjy.network.template;

/**
 * 单例模板
 */

public abstract class SingletonTemplate<T> {
    private T mInstance;

    protected abstract T create();

    public final T get() {
        synchronized (this) {
            if (mInstance == null) {
                mInstance = create();
            }
            return mInstance;
        }
    }
}
