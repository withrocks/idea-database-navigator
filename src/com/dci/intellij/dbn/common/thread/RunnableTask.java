package com.dci.intellij.dbn.common.thread;

public interface RunnableTask<T> extends Runnable{
    void start();
    void setResult(T result);
    T getResult();

}
