package com.dci.intellij.dbn.language.common.element;

import com.dci.intellij.dbn.language.common.element.util.IdentifierCategory;
import com.dci.intellij.dbn.language.common.element.util.IdentifierType;
import com.dci.intellij.dbn.language.common.resolve.UnderlyingObjectResolver;
import com.dci.intellij.dbn.object.common.DBObjectType;

public interface IdentifierElementType extends LeafElementType {
    IdentifierType getIdentifierType();

    IdentifierCategory getIdentifierCategory();

    boolean isObject();

    boolean isAlias();

    boolean isVariable();

    boolean isReference();

    boolean isReferenceable();

    boolean isDefinition();

    boolean isLocalReference();

    DBObjectType getObjectType();

    String getObjectTypeName();

    String getQualifiedObjectTypeName();

    boolean isObjectOfType(DBObjectType type);

    boolean isSubject();

    UnderlyingObjectResolver getUnderlyingObjectResolver();
}
