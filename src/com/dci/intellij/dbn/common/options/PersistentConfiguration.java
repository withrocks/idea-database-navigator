package com.dci.intellij.dbn.common.options;

import org.jdom.Element;

public interface PersistentConfiguration {
    void readConfiguration(Element element);
    void writeConfiguration(Element element);
}