package com.dci.intellij.dbn.language.common.navigation;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class NavigateToDefinitionAction extends NavigationAction{
    public NavigateToDefinitionAction(DBObject parentObject, BasePsiElement navigationElement, DBObjectType objectType) {
        super("Go to " + objectType.getName() + " Definition", Icons.NAVIGATION_GO_TO_BODY, parentObject, navigationElement);
    }
}