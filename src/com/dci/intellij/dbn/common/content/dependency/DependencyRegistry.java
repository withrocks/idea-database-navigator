package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;
import gnu.trove.THashMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DependencyRegistry {
    private Map<String, Set<DynamicContent>> registry = new THashMap<String, Set<DynamicContent>>();

    public void registerDependency(String dependencyKey, DynamicContent dynamicContent){
        Set<DynamicContent> dynamicContents = registry.get(dependencyKey);
        if (dynamicContents == null) {
            dynamicContents = new HashSet<DynamicContent>();
            registry.put(dependencyKey, dynamicContents);
        }
        dynamicContents.add(dynamicContent);
    }

    public void markContentsDirty(String dependencyKey) {
        Set<DynamicContent> dynamicContents = registry.get(dependencyKey);
        for (DynamicContent dynamicContent : dynamicContents) {
            dynamicContent.setDirty(true);
        }
    }
}
