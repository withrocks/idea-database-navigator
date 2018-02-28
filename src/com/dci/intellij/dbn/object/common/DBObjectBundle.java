package com.dci.intellij.dbn.object.common;

import java.util.List;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.lookup.ConsumerStoppedException;
import com.dci.intellij.dbn.common.lookup.LookupConsumer;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.DBNativeDataType;
import com.dci.intellij.dbn.database.DatabaseObjectIdentifier;
import com.dci.intellij.dbn.object.DBCharset;
import com.dci.intellij.dbn.object.DBPrivilege;
import com.dci.intellij.dbn.object.DBRole;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBSystemPrivilege;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.intellij.openapi.Disposable;

public interface DBObjectBundle extends BrowserTreeNode, Disposable {
    List<DBSchema> getSchemas();
    List<DBUser> getUsers();
    List<DBRole> getRoles();
    List<DBSystemPrivilege> getSystemPrivileges();
    List<DBCharset> getCharsets();
    List<DBNativeDataType> getNativeDataTypes();
    DBNativeDataType getNativeDataType(String name);

    DBSchema getSchema(String name);
    DBSchema getPublicSchema();
    DBSchema getUserSchema();
    DBUser getUser(String name);
    DBRole getRole(String name);
    DBPrivilege getPrivilege(String name);
    DBSystemPrivilege getSystemPrivilege(String name);
    DBCharset getCharset(String name);
    List<DBDataType> getCachedDataTypes();

    DBObject getObject(DatabaseObjectIdentifier objectIdentifier);
    DBObject getObject(DBObjectType objectType, String name);
    DBObject getObject(DBObjectType objectType, String name, int overload);
    void lookupObjectsOfType(LookupConsumer consumer, DBObjectType objectType) throws ConsumerStoppedException;
    void lookupChildObjectsOfType(LookupConsumer consumer, DBObject parentObject, DBObjectType objectType, ObjectTypeFilter filter, DBSchema currentSchema) throws ConsumerStoppedException;
    void refreshObjectsStatus(DBSchemaObject requester);

    DBObjectListContainer getObjectListContainer();
    boolean isValid();
}
