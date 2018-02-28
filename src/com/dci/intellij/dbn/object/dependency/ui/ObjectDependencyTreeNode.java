package com.dci.intellij.dbn.object.dependency.ui;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.thread.SimpleBackgroundTask;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.dependency.ObjectDependencyType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.Disposable;

public class ObjectDependencyTreeNode implements Disposable{
    private DBObjectRef<DBObject> objectRef;
    private List<ObjectDependencyTreeNode> dependencies;
    private ObjectDependencyTreeModel model;
    private ObjectDependencyTreeNode parent;

    public ObjectDependencyTreeNode(ObjectDependencyTreeNode parent, DBObject object) {
        this.parent = parent;
        this.objectRef = DBObjectRef.from(object);
    }

    @Nullable
    DBObject getObject() {
        return DBObjectRef.get(objectRef);
    }

    public ObjectDependencyTreeNode(ObjectDependencyTreeModel model, DBObject object) {
        this.model = model;
        this.objectRef = DBObjectRef.from(object);
    }

    public ObjectDependencyTreeModel getModel() {
        return model == null ? getParent().getModel() : model;
    }

    public ObjectDependencyTreeNode getParent() {
        return parent;
    }

    public synchronized List<ObjectDependencyTreeNode> getChildren() {
        final DBObject object = getObject();
        if (dependencies == null) {
            dependencies = new ArrayList<ObjectDependencyTreeNode>();
            if (!isRecursive(object)) {
                dependencies.add(new ObjectDependencyTreeNode(this, null));
                new SimpleBackgroundTask("load dependencies") {
                    @Override
                    protected void execute() {
                        //System.out.println(object == null ? null : object.getQualifiedName());

                        List<ObjectDependencyTreeNode> loadedDependencies = new ArrayList<ObjectDependencyTreeNode>();
                        if (object instanceof DBSchemaObject) {
                            DBSchemaObject schemaObject = (DBSchemaObject) object;
                            List<DBObject> dependentObjects = loadDependencies(schemaObject);

                            if (dependentObjects != null) {
                                for (DBObject dependentObject : dependentObjects) {
/*
                                    if (dependentObject instanceof DBSchemaObject) {
                                        loadDependencies((DBSchemaObject) dependentObject);
                                    }
*/
                                    ObjectDependencyTreeNode node = new ObjectDependencyTreeNode(ObjectDependencyTreeNode.this, dependentObject);
                                    loadedDependencies.add(node);
                                }
                            }
                        }

                        dependencies = loadedDependencies;
                        getModel().notifyNodeLoaded(ObjectDependencyTreeNode.this);
                    }
                }.start();
            }
        }
        return dependencies;
    }

    @Nullable
    private List<DBObject> loadDependencies(DBSchemaObject schemaObject) {
        ObjectDependencyType dependencyType = getModel().getDependencyType();
        return
            dependencyType == ObjectDependencyType.INCOMING ? schemaObject.getReferencedObjects() :
            dependencyType == ObjectDependencyType.OUTGOING ? schemaObject.getReferencingObjects() : null;
    }

    boolean isRecursive(DBObject object) {
        ObjectDependencyTreeNode parent = getParent();
        while (parent != null) {
            if (object != null && object.equals(parent.getObject())) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    public ObjectDependencyTreeNode[] getTreePath() {
        List<ObjectDependencyTreeNode> path = new ArrayList<ObjectDependencyTreeNode>();
        path.add(this);
        ObjectDependencyTreeNode parent = getParent();
        while (parent != null) {
            path.add(0, parent);
            parent = parent.getParent();
        }
        return path.toArray(new ObjectDependencyTreeNode[path.size()]);
    }

    @Override
    public void dispose() {
        DisposerUtil.dispose(dependencies);
        model = null;
        parent = null;
    }
}
