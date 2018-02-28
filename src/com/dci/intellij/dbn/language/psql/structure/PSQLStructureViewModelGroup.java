package com.dci.intellij.dbn.language.psql.structure;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.ide.util.treeView.smartTree.Group;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;

public class PSQLStructureViewModelGroup implements Group {
    private static TextAttributesKey TEXT_ATTRIBUTES_KEY =
            TextAttributesKey.createTextAttributesKey(
                    "PSQLStructureViewModelGroup",
                    new TextAttributes(Color.BLACK, null, null, null, Font.BOLD));

    private DBObjectType objectType;
    private List<TreeElement> children = new ArrayList<TreeElement>();


    public PSQLStructureViewModelGroup(DBObjectType objectType) {
        this.objectType = objectType;
    }

    public void addChild(TreeElement treeElement) {
        children.add(treeElement);
    }

    @NotNull
    public ItemPresentation getPresentation() {
        return itemPresentation;
    }

    @NotNull
    public Collection<TreeElement> getChildren() {
        return children;
    }


    private ItemPresentation itemPresentation = new ItemPresentation(){
        public String getPresentableText() {
            return NamingUtil.capitalize(objectType.getListName());
        }

        public String getLocationString() {
            return null;
        }

        public Icon getIcon(boolean open) {
            return null;//objectType.getListIcon();
        }

        public TextAttributesKey getTextAttributesKey() {
            return TEXT_ATTRIBUTES_KEY;
        }
    };
}