package com.dci.intellij.dbn.common;

import java.util.HashSet;
import java.util.Set;

public class ThreadLocalRegister {
    private static ThreadLocal<Set<Object>> threadLocal = new ThreadLocal<Set<Object>>();

    static {
        threadLocal.set(new HashSet<Object>());
    }

    private static Set<Object> getRegister() {
        Set<Object> register = threadLocal.get();
        if (register == null) {
            register = new HashSet<Object>();
            threadLocal.set(register);
        }
        return register;
    }

    public static void register(Object object) {
        getRegister().add(object);
    }

    public static void unregister(Object object) {
        getRegister().remove(object);
    }

    public static boolean isRegistered(Object object) {
        return getRegister().contains(object);
    }
}
