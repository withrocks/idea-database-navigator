package com.dci.intellij.dbn.common.dispose;

import java.util.Collection;
import java.util.Map;

import com.dci.intellij.dbn.common.list.FiltrableList;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;

public class DisposerUtil {

    public static void dispose(Disposable disposable) {
        if (disposable != null) {
            Disposer.dispose(disposable);
        }
    }

    public static void dispose(Disposable[] array) {
        if (array != null && array.length> 0) {
            for(Disposable disposable : array) {
                dispose(disposable);
            }
        }
    }
    
    public static void dispose(Collection<? extends Disposable> collection) {
        if (collection instanceof FiltrableList) {
            FiltrableList<? extends Disposable> filtrableList = (FiltrableList) collection;
            collection = filtrableList.getFullList();
        }
        if (collection != null && collection.size()> 0) {
            for(Disposable disposable : collection) {
                dispose(disposable);
            }
            collection.clear();
        }
    }

    public static void dispose(Map<?, ? extends Disposable> map) {
        if (map != null) {
            for (Disposable disposable : map.values()) {
                dispose(disposable);
            }
            map.clear();
        }
    }


    public static void register(Disposable parent, Collection<? extends Disposable> collection) {
        for (Disposable disposable : collection) {
            Disposer.register(parent, disposable);
        }
    }
}