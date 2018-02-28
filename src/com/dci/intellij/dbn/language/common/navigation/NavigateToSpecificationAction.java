package com.dci.intellij.dbn.language.common.navigation;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class NavigateToSpecificationAction extends NavigationAction{
    public NavigateToSpecificationAction(DBObject parentObject, BasePsiElement navigationElement, DBObjectType objectType) {
        super("Go to " + objectType.getName() + " Specification", Icons.NAVIGATION_GO_TO_SPEC, parentObject, navigationElement);
    }
}
