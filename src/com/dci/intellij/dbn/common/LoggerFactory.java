package com.dci.intellij.dbn.common;

import com.intellij.openapi.diagnostic.Logger;

public class LoggerFactory {

    /**
     * Creates or returns the instance of a Logger for the class within this method is issued.
     * The name of the class is dynamically determined by inquiring the
     * stacktrace of the method execution.
     * NOTE: do not use this method but for static loggers in class initialisations.  
     */
    public static Logger createLogger() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement stackTraceElement = stackTraceElements[0];
        for (int i=0; i<stackTraceElements.length; i++) {
            if (stackTraceElements[i].getMethodName().equals("createLogger")) {
                stackTraceElement = i+1 <stackTraceElements.length ? stackTraceElements[i+1] : stackTraceElements[i];
                break;
            }
        }

        String className = stackTraceElement.getClassName();
        return Logger.getInstance(className);
    }

    /**
     * Creates or returns the instance of a Logger for the given class.
     */
    public static Logger createLogger(Class clazz) {
        return Logger.getInstance(clazz.getName());
    }

    /**
     * Creates or returns the instance of a Logger for the given name.
     */
    public static Logger createLogger(String name) {
        return Logger.getInstance(name);
    }
}
