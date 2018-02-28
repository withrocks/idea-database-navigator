package com.dci.intellij.dbn.language.common.psi;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingAttributes;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.language.common.element.IdentifierElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.impl.QualifiedIdentifierVariant;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.IdentifierType;
import com.dci.intellij.dbn.language.common.psi.lookup.AliasDefinitionLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.IdentifierLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.LookupAdapterCache;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectDefinitionLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.VariableDefinitionLookupAdapter;
import com.dci.intellij.dbn.language.common.resolve.AliasObjectResolver;
import com.dci.intellij.dbn.language.common.resolve.SurroundingVirtualObjectResolver;
import com.dci.intellij.dbn.language.common.resolve.UnderlyingObjectResolver;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBSynonym;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBVirtualObject;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.IncorrectOperationException;
import gnu.trove.THashSet;

public class IdentifierPsiElement extends LeafPsiElement implements PsiNamedElement {
    public IdentifierPsiElement(ASTNode astNode, IdentifierElementType elementType) {
        super(astNode, elementType);

    }

    public IdentifierElementType getElementType() {
        return (IdentifierElementType) super.getElementType();
    }

    public ItemPresentation getPresentation() {
        return this;
    }

    public boolean isQuoted() {
        CharSequence charSequence = getChars();

        char firstChar = charSequence.charAt(0);
        char lastChar = charSequence.charAt(charSequence.length() - 1);

        if (!Character.isLetterOrDigit(firstChar) && !Character.isLetterOrDigit(lastChar)) {
            char quotesChar = getIdentifierQuotesChar();
            return (firstChar == quotesChar && lastChar == quotesChar);
        }
        return false;
    }

    @Override
    public String getName() {
        return getText();
    }

    @Override
    public FormattingAttributes getFormattingAttributes() {
        return super.getFormattingAttributes();
    }

    /**
     * ******************************************************
     * ItemPresentation                *
     * *******************************************************
     */
    public String getPresentableText() {
        StringBuilder builder = new StringBuilder();
        StringUtil.appendToUpperCase(builder,  getUnquotedText());
        builder.append(" (");
        builder.append(getObjectType());
        builder.append(")");
        return builder.toString();
    }

    @Nullable
    public String getLocationString() {
        return null;
    }

    @Nullable
    public Icon getIcon(boolean open) {
        DBObjectType type = getObjectType();
        return type.getIcon();
    }

    @Nullable
    public TextAttributesKey getTextAttributesKey() {
        return null;
    }


    /**
     * ******************************************************
     * Lookup routines                 *
     * *******************************************************
     */
    @Nullable
    public BasePsiElement findPsiElement(PsiLookupAdapter lookupAdapter, int scopeCrossCount) {
        if (lookupAdapter instanceof IdentifierLookupAdapter) {
            IdentifierLookupAdapter identifierLookupAdapter = (IdentifierLookupAdapter) lookupAdapter;
            if (identifierLookupAdapter.matchesName(this)) {
                /*PsiElement parentPsiElement = getParent();
                if (parentPsiElement instanceof QualifiedIdentifierPsiElement) {
                    QualifiedIdentifierPsiElement qualifiedIdentifierPsiElement = (QualifiedIdentifierPsiElement) parentPsiElement;
                    QualifiedIdentifierElementType qualifiedIdentifierElementType = qualifiedIdentifierPsiElement.getElementType();
                    if (!qualifiedIdentifierElementType.containsObjectType(identifierLookupAdapter.getObjectType())) {
                        return null;
                    }
                }*/
                return lookupAdapter.matches(this) ? this : null;
            }
        }
        return null;

    }

    @Nullable
    public Set<BasePsiElement> collectPsiElements(PsiLookupAdapter lookupAdapter, @Nullable Set<BasePsiElement> bucket, int scopeCrossCount) {
        if (lookupAdapter instanceof IdentifierLookupAdapter) {
            IdentifierLookupAdapter identifierLookupAdapter = (IdentifierLookupAdapter) lookupAdapter;
            if (identifierLookupAdapter.matchesName(this)) {
                if (lookupAdapter.matches(this)) {
                    if (bucket == null) bucket = new THashSet<BasePsiElement>();
                    bucket.add(this);
                }
            }
        }

        return bucket;
    }

    public void collectSubjectPsiElements(@NotNull Set<IdentifierPsiElement> bucket) {
        if (getElementType().is(ElementTypeAttribute.SUBJECT)) {
            bucket.add(this);
        }
    }

    public void collectExecVariablePsiElements(@NotNull Set<ExecVariablePsiElement> bucket) {
    }

    /**
     * ******************************************************
     * Miscellaneous                     *
     * *******************************************************
     */
    public boolean isObject() {
        return getElementType().isObject();
    }

    public boolean isAlias() {
        return getElementType().isAlias();
    }

    public boolean isVariable() {
        return getElementType().isVariable();
    }


    public boolean isDefinition() {
        return getElementType().isDefinition();
    }


    public boolean isSubject() {
        return getElementType().isSubject();
    }

    public boolean isReference() {
        return getElementType().isReference();
    }
    
    public boolean isReferenceable() {
        return getElementType().isReferenceable();
    }

    public boolean isObjectOfType(DBObjectType objectType) {
        return getElementType().isObjectOfType(objectType);
    }

    public boolean isLocalReference() {
        return getElementType().isLocalReference();
    }

    public DBObjectType getObjectType() {
        if (ref != null && ref.getObjectType() != null) {
            return ref.getObjectType();
        }
        return getElementType().getObjectType();
    }

    public String getObjectTypeName() {
        return getElementType().getObjectTypeName();
    }

    /**
     * TODO: !!method arguments resolve into the object type from their definition
     */
    public synchronized DBObject resolveUnderlyingObjectType() {
        return null;
    }
    /**
     * Looks-up whatever underlying database object may be referenced from this identifier.
     * - if this references to a synonym, the DBObject behind the synonym is returned.
     * - if this is an alias reference or definition, it returns the underlying DBObject of the aliased identifier.
     *
     * @return real underlying database object behind the identifier.
     */
    @Nullable
    public DBObject resolveUnderlyingObject() {
        UnderlyingObjectResolver underlyingObjectResolver = getElementType().getUnderlyingObjectResolver();
        if (underlyingObjectResolver != null) {
            DBObject underlyingObject = underlyingObjectResolver.resolve(this);
            return resolveActualObject(underlyingObject);
        }


        PsiElement psiReferenceElement = resolve();
        if (psiReferenceElement != this) {
            if (psiReferenceElement instanceof DBObject) {
                DBObject underlyingObject = (DBObject) psiReferenceElement;
                return resolveActualObject(underlyingObject.getUndisposedElement());
            }

            if (psiReferenceElement instanceof IdentifierPsiElement) {
                IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) psiReferenceElement;
                return identifierPsiElement.resolveUnderlyingObject();
            }
        }

        if (isAlias() && isDefinition()) {
            DBObject underlyingObject = AliasObjectResolver.getInstance().resolve(this);
            return resolveActualObject(underlyingObject);
        }

        DBObject underlyingObject = SurroundingVirtualObjectResolver.getInstance().resolve(this);
        if (underlyingObject != null) {
            return underlyingObject;
        }

/*        DBObjectType objectType = getObjectType();
        if (isObject()) {
            if (psiReferenceElement instanceof DBObject) {
                DBObject object = (DBObject) psiReferenceElement;
                underlyingObject = object.getUndisposedElement();
            } else if (psiReferenceElement instanceof IdentifierPsiElement) {
                IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) psiReferenceElement;
                PsiElement underlyingPsiElement = identifierPsiElement.resolve();
                if (underlyingPsiElement == null) {
                    BasePsiElement virtualObjectPsiElement = identifierPsiElement.findEnclosingVirtualObjectPsiElement(objectType);
                    if (virtualObjectPsiElement != null) {
                        underlyingObject = virtualObjectPsiElement.resolveUnderlyingObject();
                    }
                }
                else if (underlyingPsiElement instanceof DBObject) {
                    underlyingObject = (DBObject) underlyingPsiElement;
                }
            }
        } else if (isAlias()) {
            BasePsiElement aliasDefinition;
            if (isDefinition()) {
                aliasDefinition = this;
            } else {
                BasePsiElement resolveScope = getEnclosingScopePsiElement();

                PsiLookupAdapter lookupAdapter = new AliasDefinitionLookupAdapter(this, objectType, getUnquotedText());
                aliasDefinition = lookupAdapter.findInScope(resolveScope);
            }

            if (aliasDefinition != null && aliasDefinition instanceof IdentifierPsiElement) {
                BasePsiElement aliasedObject = PsiUtil.resolveAliasedEntityElement((IdentifierPsiElement) aliasDefinition);
                if (aliasedObject != null) {
                    if (aliasedObject.isVirtualObject()) {
                        underlyingObject = aliasedObject.resolveUnderlyingObject();
                    } else if (aliasedObject instanceof IdentifierPsiElement) {
                        IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) aliasedObject;
                        PsiElement underlyingPsiElement = identifierPsiElement.resolve();
                        if (underlyingPsiElement != null && underlyingPsiElement instanceof DBObject) {
                            underlyingObject = (DBObject) underlyingPsiElement;
                        }
                    }
                }
            }
        } else if (isVariable()) {
            if (isReference()) {
                if (psiReferenceElement instanceof IdentifierPsiElement) {
                    IdentifierPsiElement variablePsiElement = (IdentifierPsiElement) psiReferenceElement;
                    if (variablePsiElement.isDefinition()) {
                        PsiElement result = variablePsiElement.resolve();
                        if (result == null) {
                            BasePsiElement virtualObjectPsiElement = variablePsiElement.findEnclosingVirtualObjectPsiElement(objectType);
                            if (virtualObjectPsiElement != null) {
                                return virtualObjectPsiElement.resolveUnderlyingObject();
                            }
                        }
                        else if (result instanceof IdentifierPsiElement) {
                            IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) result;
                            return identifierPsiElement.resolveUnderlyingObject();
                        }
                    }
                }
            }
        }*/

        return resolveActualObject(underlyingObject);
    }

    private static DBObject resolveActualObject(DBObject object) {
        while (object != null && object instanceof DBSynonym) {
            DBSynonym synonym = (DBSynonym) object;
            object = synonym.getUnderlyingObject();
            if (object == null) return synonym;
        }
        return object;
    }

    public NamedPsiElement findNamedPsiElement(String id) {
        return null;
    }

    public BasePsiElement findPsiElementBySubject(ElementTypeAttribute attribute, CharSequence subjectName, DBObjectType subjectType) {
        if (getElementType().is(attribute) && getElementType().is(ElementTypeAttribute.SUBJECT)) {
            if (subjectType == getObjectType() && StringUtil.equalsIgnoreCase(subjectName, this.getChars())) {
                return this;
            }
        }
        return null;
    }

    /********************************************************
     *                      Variant builders                *
     *******************************************************/

    private Object[] buildAliasRefVariants() {
        SequencePsiElement statement = (SequencePsiElement) findEnclosingPsiElement(ElementTypeAttribute.STATEMENT);
        BasePsiElement sourceScope = getEnclosingScopePsiElement();
        DBObjectType objectType = getObjectType();
        PsiLookupAdapter lookupAdapter = LookupAdapterCache.ALIAS_DEFINITION.get(objectType);
        Set<BasePsiElement> aliasDefinitions = lookupAdapter.collectInScope(statement, null);
        return aliasDefinitions == null ? new Object[0] : aliasDefinitions.toArray();
    }

    /********************************************************
     *                      Rersolvers                      *
     ********************************************************/

    private void resolveWithinQualifiedIdentifierElement(QualifiedIdentifierPsiElement qualifiedIdentifier) {
        int index = qualifiedIdentifier.getIndexOf(this);

        BasePsiElement parentObjectElement = null;
        DBObject parentObject = null;
        if (index > 0) {
            IdentifierPsiElement parentElement = qualifiedIdentifier.getLeafAtIndex(index - 1);
            if (parentElement.resolve() != null) {
                parentObjectElement = parentElement.isObject() || parentElement.isVariable() ? parentElement : PsiUtil.resolveAliasedEntityElement(parentElement);
                parentObject = parentObjectElement != null ? parentElement.resolveUnderlyingObject() : null;
            } else {
                return;
            }
        }

        for (QualifiedIdentifierVariant parseVariant : qualifiedIdentifier.getParseVariants()) {
            LeafElementType parseVariantElementType = parseVariant.getLeaf(index);

            if (parseVariantElementType instanceof IdentifierElementType) {
                IdentifierElementType substitutionCandidate = (IdentifierElementType) parseVariantElementType;
                DBObjectType objectType = substitutionCandidate.getObjectType();

                if (parentObject == null) {  // index == 0
                    if (parentObjectElement == null) {
                        if (substitutionCandidate.isObject()) {
                            resolveWithScopeParentLookup(objectType, substitutionCandidate);
                        } else if (substitutionCandidate.isAlias()) {
                            PsiLookupAdapter lookupAdapter = new AliasDefinitionLookupAdapter(this, objectType, ref.getText());
                            BasePsiElement referencedElement = lookupAdapter.findInParentScopeOf(this);
                            if (referencedElement != this && isValidReference(referencedElement)) {
                                setElementType(parseVariantElementType);
                                ref.setReferencedElement(referencedElement);
                                ref.setParent(null);
                            }

                        } else if (substitutionCandidate.isVariable()) {
                            PsiLookupAdapter lookupAdapter = new VariableDefinitionLookupAdapter(this, DBObjectType.ANY, ref.getText());
                            BasePsiElement referencedElement = lookupAdapter.findInParentScopeOf(this);
                            if (referencedElement != this && isValidReference(referencedElement)) {
                                setElementType(parseVariantElementType);
                                ref.setReferencedElement(referencedElement);
                                ref.setParent(null);
                            }
                        }
                    }
                } else { // index > 0
                    IdentifierElementType parentElementType = (IdentifierElementType) parseVariant.getLeaf(index - 1);
                    if (parentObject.isOfType(parentElementType.getObjectType())) {
                        DBObject referencedElement = parentObject.getChildObject(objectType, ref.getText().toString(), false);
                        if (isValidReference(referencedElement)) {
                            setElementType(parseVariantElementType);
                            ref.setReferencedElement(referencedElement);
                            ref.setParent(parentObjectElement);
                        }
                    }

                }
                if (ref.getReferencedElement() != null) {
                    return;
                }
            }
        }
    }

    private void resolveWithScopeParentLookup(DBObjectType objectType, IdentifierElementType substitutionCandidate) {
        if (isPrecededByDot()) {
            LeafPsiElement prevLeaf = getPrevLeaf();
            if (prevLeaf != null) {
                LeafPsiElement parentPsiElement = prevLeaf.getPrevLeaf();
                if (parentPsiElement != null) {
                    DBObject object = parentPsiElement.resolveUnderlyingObject();
                    if (object != null) {
                        PsiElement referencedElement = object.getChildObject(ref.getText().toString(), 0, false);
                        if (isValidReference(referencedElement)) {
                            ref.setParent(parentPsiElement);
                            ref.setReferencedElement(referencedElement);
                            setElementType(substitutionCandidate);
                            return;
                        }
                    }

                }
            }
        }

        if (substitutionCandidate.isObject()) {
            ConnectionHandler activeConnection = ref.getActiveConnection();
            if (!substitutionCandidate.isLocalReference() && activeConnection != null && !activeConnection.isVirtual()) {
                Set<DBObject> parentObjects = identifyPotentialParentObjects(objectType, null, this, this);
                if (parentObjects != null && parentObjects.size() > 0) {
                    for (DBObject parentObject : parentObjects) {
                        PsiElement referencedElement = parentObject.getChildObject(objectType, ref.getText().toString(), false);
                        if (isValidReference(referencedElement)) {
                            ref.setParent(null);
                            ref.setReferencedElement(referencedElement);
                            setElementType(substitutionCandidate);
                            return;
                        }
                    }
                }

                DBObjectBundle objectBundle = activeConnection.getObjectBundle();
                PsiElement referencedElement = objectBundle.getObject(objectType, ref.getText().toString(), 0);
                if (isValidReference(referencedElement)) {
                    ref.setParent(null);
                    ref.setReferencedElement(referencedElement);
                    setElementType(substitutionCandidate);
                    return;
                }

                DBSchema schema = getCurrentSchema();
                if (schema != null && objectType.isSchemaObject()) {
                    referencedElement = schema.getChildObject(objectType, ref.getText().toString(), false);
                    if (isValidReference(referencedElement)) {
                        ref.setParent(null);
                        ref.setReferencedElement(referencedElement);
                        setElementType(substitutionCandidate);
                        return;
                    }
                }
            }
            if (!substitutionCandidate.isDefinition()){
                PsiLookupAdapter lookupAdapter = new ObjectDefinitionLookupAdapter(this, objectType, ref.getText());
                PsiElement referencedElement = lookupAdapter.findInParentScopeOf(this);
                if (referencedElement != this && isValidReference(referencedElement)) {
                    ref.setParent(null);
                    ref.setReferencedElement(referencedElement);
                    setElementType(substitutionCandidate);
                    return;
                }
            }
        } else if (substitutionCandidate.isAlias()) {
            PsiLookupAdapter lookupAdapter = new AliasDefinitionLookupAdapter(this, objectType, ref.getText());
            BasePsiElement referencedElement = lookupAdapter.findInParentScopeOf(this);
            if (referencedElement != null && referencedElement != this) {
                ref.setParent(null);
                ref.setReferencedElement(referencedElement);
            }
        } else if (substitutionCandidate.isVariable()) {
            if (substitutionCandidate.isReference()) {
                PsiLookupAdapter lookupAdapter = new VariableDefinitionLookupAdapter(this, DBObjectType.ANY, ref.getText());
                BasePsiElement referencedElement = lookupAdapter.findInParentScopeOf(this);
                if (referencedElement != null && referencedElement != this) {
                    ref.setParent(null);
                    ref.setReferencedElement(referencedElement);
                }
            }
        }
    }

    public boolean isPrecededByDot() {
        LeafPsiElement prevLeaf = getPrevLeaf();
        if (prevLeaf instanceof TokenPsiElement) {
            TokenPsiElement tokenPsiElement = (TokenPsiElement) prevLeaf;
            return tokenPsiElement.getElementType().getTokenType() == tokenPsiElement.getLanguage().getSharedTokenTypes().getChrDot();
        }
        return false;
    }

    private boolean isValidReference(PsiElement referencedElement) {
        if (referencedElement != null && referencedElement != this) {
            if (referencedElement instanceof DBVirtualObject) {
                DBVirtualObject object = (DBVirtualObject) referencedElement;
                if (object.getUnderlyingPsiElement().containsPsiElement(this)) {
                    return false;
                }
            }
            // check if inside same scope
            if (referencedElement instanceof IdentifierPsiElement) {
                IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) referencedElement;
                if (identifierPsiElement.isReference() && identifierPsiElement.isReferenceable()) {
                    return identifierPsiElement.findEnclosingScopeDemarcationPsiElement() == findEnclosingScopeDemarcationPsiElement();
                }
            }
            return true;
        }
        return false;
    }

    /**
     * ******************************************************
     * PsiReference                    *
     * *******************************************************
     */
    private PsiResolveResult ref;

    @Nullable
    public PsiElement resolve() {
        if (isResolving()) {
            return ref.getReferencedElement();
        }
        if (isDefinition() && (isAlias() || (isVariable() && !isSubject()))) {
            // alias definitions do not have references.
            // underlying object is determined on runtime
            return null;
        }

        ConnectionHandler connectionHandler = getActiveConnection();
        if ((connectionHandler == null || connectionHandler.isVirtual()) && isObject() && isDefinition()) {
            return null;
        }
        if (ref == null) ref = new PsiResolveResult(this);
        if (ref.isDirty()) {
            //System.out.println("resolving " + getTextRange() + " " + getText());
            try {
                //DatabaseLoadMonitor.setEnsureDataLoaded(false);

                ref.preResolve(this);
                if (getParent() instanceof QualifiedIdentifierPsiElement) {
                    QualifiedIdentifierPsiElement qualifiedIdentifier = (QualifiedIdentifierPsiElement) getParent();
                    resolveWithinQualifiedIdentifierElement(qualifiedIdentifier);
                } else {
                    resolveWithScopeParentLookup(getObjectType(), getElementType());
                }
            } finally {
                ref.postResolve();
                //DatabaseLoadMonitor.setEnsureDataLoaded(false);
            }
        }
        return ref.getReferencedElement();
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return element != this && ref != null && element == ref.getReferencedElement();
    }

    public CharSequence getUnquotedText() {
        CharSequence text = getChars();
        if (isQuoted() && text.length() > 1) {
            return text.subSequence(1, text.length() - 1);
        }
        return text;
    }

    public boolean textMatches(@NotNull CharSequence text) {
        CharSequence chars = getChars();
        if (isQuoted())  {
            return chars.length() == text.length() + 2 && StringUtil.indexOfIgnoreCase(chars, text, 0) == 1;
        } else {
            return StringUtil.equalsIgnoreCase(chars, text);
        }
    }

    public boolean isSoft() {
        return isDefinition();
    }

    public boolean hasErrors() {
        return false;
    }

    @Override
    public boolean matches(BasePsiElement basePsiElement, MatchType matchType) {
        if (basePsiElement instanceof IdentifierPsiElement) {
            IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) basePsiElement;
            return matchType == MatchType.SOFT || StringUtil.equalsIgnoreCase(identifierPsiElement.getChars(), getChars());
        }

        return false;
    }

    public boolean isResolved() {
        return ref != null && !ref.isDirty();
    }

    public boolean isResolving() {
        return ref != null && ref.isResolving();
    }

    public PsiElement setName(@NotNull @NonNls String name) throws IncorrectOperationException {
        return null;
    }

    public int getResolveTrialsCount() {
        return ref == null ? 0 : ref.getOverallResolveTrials();
    }

    public IdentifierType getIdentifierType() {
        return getElementType().getIdentifierType();
    }

    public List<BasePsiElement> findQualifiedUsages() {
        List<BasePsiElement> qualifiedUsages= new ArrayList<BasePsiElement>();
        BasePsiElement scopePsiElement = getEnclosingScopePsiElement();
        IdentifierLookupAdapter identifierLookupAdapter = new IdentifierLookupAdapter(this, null, null, null, getChars());
        Set<BasePsiElement> basePsiElements = identifierLookupAdapter.collectInElement(scopePsiElement, null);
        if (basePsiElements != null) {
            for (BasePsiElement basePsiElement : basePsiElements) {
                QualifiedIdentifierPsiElement qualifiedIdentifierPsiElement = basePsiElement.findEnclosingPsiElement(QualifiedIdentifierPsiElement.class);
                if (qualifiedIdentifierPsiElement != null && qualifiedIdentifierPsiElement.getElementsCount() > 1) {
                    qualifiedUsages.add(qualifiedIdentifierPsiElement);
                }
            }
        }
        return qualifiedUsages;
    }

    @Nullable
    public QualifiedIdentifierPsiElement getParentQualifiedIdentifier() {
        return findEnclosingPsiElement(QualifiedIdentifierPsiElement.class);
    }
}
