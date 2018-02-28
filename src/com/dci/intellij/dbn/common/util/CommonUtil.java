package com.dci.intellij.dbn.common.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import org.jdom.Document;
import org.jdom.adapters.XML4JDOMAdapter;
import org.jdom.input.DOMBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.intellij.openapi.diagnostic.Logger;

public class CommonUtil {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    public static boolean isPluginCall() {
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
            if (stackTraceElement.getClassName().contains(".dbn.")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCalledThrough(Class clazz) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        try {
            for (int i=3; i<stackTraceElements.length; i++) {
                StackTraceElement stackTraceElement = stackTraceElements[i];
                Class stackTraceClass = Class.forName(stackTraceElement.getClassName());
                if (clazz.isAssignableFrom(stackTraceClass)) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static double getProgressPercentage(int is, int should) {
        BigDecimal fraction = new BigDecimal(is).divide(new BigDecimal(should), 6, BigDecimal.ROUND_HALF_UP);
        return fraction.doubleValue();
    }

    @NotNull
    public static <T> T nvl(@Nullable T value, @NotNull T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static <T> T nvln(@Nullable T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static String nullIfEmpty(String string) {
        if (string != null) {
            string = string.trim();
            if (string.length() == 0) {
                string = null;
            }
        }
        return string;
    }

    private static final Object NULL_OBJECT = new Object();

    public static <T> boolean safeEqual(@Nullable T value1, @Nullable T value2) {
        return nvl(value1, "").equals(nvl(value2, ""));
    }

    public static Document loadXmlFile(Class clazz, String name) {
        InputStream inputStream = clazz.getResourceAsStream(name);
        return createXMLDocument(inputStream);
    }

    public static Properties loadProperties(Class clazz, String name) {
        InputStream inputStream = clazz.getResourceAsStream(name);
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (Exception e) {
        }
        return properties;
    }

    @Nullable
    public static Document createXMLDocument(InputStream inputStream) {
        try {
            return new DOMBuilder().build(new XML4JDOMAdapter().getDocument(inputStream, false));
        } catch (Exception e) {
            //LOGGER.warn(e);
        }
        return null;
    }

    public static String readFile(File file) throws IOException {
        Reader in = new FileReader(file);
        StringBuilder buffer = new StringBuilder();
        int i;
        while ((i = in.read()) != -1) buffer.append((char) i);
        in.close();
        return buffer.toString();
    }

    public static String readInputStream(InputStream inputStream) throws IOException {
        Reader in = new InputStreamReader(inputStream);
        StringBuilder buffer = new StringBuilder();
        int i;
        while ((i = in.read()) != -1) buffer.append((char) i);
        in.close();
        return buffer.toString();
    }

    public static Set<String> commaSeparatedTokensToSet(String commaSeparated) {
        Set<String> set = new HashSet<String>();
        StringTokenizer stringTokenizer = new StringTokenizer(commaSeparated, ",");
        while (stringTokenizer.hasMoreTokens()) {
            set.add(stringTokenizer.nextToken());
        }
        return set;
    }

    public static <T> boolean isOneOf(T[] objects, T object) {
        for (T obj: objects ) {
            if (obj == object) return true;
        }
        return false;
    }

    public static <T> int indexOf(T[] objects, T object){
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] == object) return i;
        }
        return -1;
    }
}
