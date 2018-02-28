package com.dci.intellij.dbn.language.common.element.impl;

import java.util.Set;
import org.jdom.Element;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.NamedElementType;
import com.dci.intellij.dbn.language.common.element.lookup.NamedElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.impl.NamedElementTypeParser;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.NamedPsiElement;
import com.dci.intellij.dbn.language.common.psi.RootPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import gnu.trove.THashSet;

public class NamedElementTypeImpl extends SequenceElementTypeImpl implements NamedElementType {
    private boolean definitionLoaded;
    private Set<ElementType> parents;
    private boolean truncateOnExecution;

    public NamedElementTypeImpl(ElementTypeBundle bundle, String id) {
        super(bundle, null, id);
        parents = new THashSet<ElementType>();
    }

    public NamedElementTypeLookupCache createLookupCache() {
        return new NamedElementTypeLookupCache(this);
    }

    public NamedElementTypeParser createParser() {
        return new NamedElementTypeParser(this);
    }

    public PsiElement createPsiElement(ASTNode astNode) {
        return is(ElementTypeAttribute.ROOT) ? new RootPsiElement(astNode, this) :
               is(ElementTypeAttribute.EXECUTABLE) ? new ExecutablePsiElement(astNode, this) :
                                new NamedPsiElement(astNode, this);
    }

    @Override
    public void loadDefinition(Element def) throws ElementTypeDefinitionException {
        super.loadDefinition(def);
        String description = ElementTypeBundle.determineMandatoryAttribute(def, "description", "Invalid definition of complex element '" + getId() + "'.");
        setDescription(description);
        truncateOnExecution = Boolean.parseBoolean(def.getAttributeValue("truncate-on-execution"));

        definitionLoaded = true;
    }

    public void update(NamedElementType elementType) {
        setDescription(elementType.getDescription());
        children = elementType.getChildren();
        definitionLoaded = true;
    }

    public boolean isDefinitionLoaded() {
        return definitionLoaded;
    }

    public String getDebugName() {
        return getId().toUpperCase();
    }

    public void addParent(ElementType parent) {
        parents.add(parent);
    }

    public Set<ElementType> getParents() {
        return parents;
    }

    public boolean truncateOnExecution() {
        return truncateOnExecution;
    }
}
