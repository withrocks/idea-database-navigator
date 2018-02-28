package com.dci.intellij.dbn.language.common.element;

import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import org.jdom.Element;

import java.util.Set;

public interface NamedElementType extends SequenceElementType {
    boolean isDefinitionLoaded();

    void update(NamedElementType unknown);

    void addParent(ElementType parent);

    void loadDefinition(Element def) throws ElementTypeDefinitionException;

    Set<ElementType> getParents();

    boolean truncateOnExecution();
}
