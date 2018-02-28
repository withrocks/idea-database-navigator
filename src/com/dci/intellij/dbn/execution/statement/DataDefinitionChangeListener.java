package com.dci.intellij.dbn.execution.statement;

import java.util.EventListener;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.util.messages.Topic;

public interface DataDefinitionChangeListener extends EventListener {
    Topic<DataDefinitionChangeListener> TOPIC = Topic.create("Data Model event", DataDefinitionChangeListener.class);
    void dataDefinitionChanged(@NotNull DBSchemaObject schemaObject);
    void dataDefinitionChanged(DBSchema schema, DBObjectType objectType);
}
