package com.dci.intellij.dbn.object.common.ui;

import com.dci.intellij.dbn.common.ui.tree.DBNTree;

import javax.swing.tree.TreeModel;

public class ObjectTree extends DBNTree {

    public ObjectTree() {
        super(new ObjectTreeModel(null, null, null));
    }

    public void setModel(TreeModel newModel) {
        assert newModel instanceof ObjectTreeModel;
        super.setModel(newModel);
        new ObjectTreeSpeedSearch(this);
    }

    public ObjectTreeModel getModel() {
        return (ObjectTreeModel) super.getModel();
    }

}
