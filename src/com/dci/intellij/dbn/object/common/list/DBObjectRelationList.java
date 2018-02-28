package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DBObjectRelationList<T extends DBObjectRelation> extends DynamicContent<T> {
    DBObjectRelationType getObjectRelationType();
    ConnectionHandler getConnectionHandler();
    @NotNull List<T> getObjectRelations();
    List<DBObjectRelation> getRelationBySourceName(String sourceName);
    List<DBObjectRelation> getRelationByTargetName(String targetName);
}
