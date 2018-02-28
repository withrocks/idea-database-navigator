package com.dci.intellij.dbn.code.common.completion;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.code.common.completion.options.filter.CodeCompletionFilterSettings;
import com.dci.intellij.dbn.common.content.DatabaseLoadMonitor;
import com.dci.intellij.dbn.common.lookup.ConsumerStoppedException;
import com.dci.intellij.dbn.common.lookup.LookupConsumer;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.IdentifierElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.impl.QualifiedIdentifierVariant;
import com.dci.intellij.dbn.language.common.element.lookup.ElementLookupContext;
import com.dci.intellij.dbn.language.common.element.lookup.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.Branch;
import com.dci.intellij.dbn.language.common.element.path.ASTPathNode;
import com.dci.intellij.dbn.language.common.element.path.PathNode;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.LeafPsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.common.psi.QualifiedIdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.lookup.AliasDefinitionLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.LookupAdapterCache;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBVirtualObject;
import com.dci.intellij.dbn.object.common.ObjectTypeFilter;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.FileElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ProcessingContext;
import gnu.trove.THashMap;

public class CodeCompletionProvider extends CompletionProvider<CompletionParameters> {
    public static final CodeCompletionProvider INSTANCE = new CodeCompletionProvider();


    public CodeCompletionProvider() {
        super();
    }

    @Override
    protected void addCompletions(
            @NotNull CompletionParameters parameters,
            ProcessingContext processingContext,
            @NotNull CompletionResultSet result) {
        try {
            DatabaseLoadMonitor.setEnsureDataLoaded(false);
            doAddCompletions(parameters, result);
        } finally {
            DatabaseLoadMonitor.setEnsureDataLoaded(true);
        }

    }

    private static void doAddCompletions(CompletionParameters parameters, CompletionResultSet result) {
        PsiFile originalFile = parameters.getOriginalFile();
        if (originalFile instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile file = (DBLanguagePsiFile) originalFile;

            CodeCompletionContext context = new CodeCompletionContext(file, parameters, result);
            CodeCompletionLookupConsumer consumer = new CodeCompletionLookupConsumer(context);

            int caretOffset = parameters.getOffset();
            if (file.findElementAt(caretOffset) instanceof PsiComment) return;

            LeafPsiElement leafBeforeCaret = PsiUtil.lookupLeafBeforeOffset(file, caretOffset);


            int invocationCount = parameters.getInvocationCount();
            if (invocationCount > 1) context.setExtended(true);

            try {

                if (leafBeforeCaret == null) {
                    addFileRootCompletions(consumer);
                } else {
                    leafBeforeCaret = (LeafPsiElement) leafBeforeCaret.getOriginalElement();
                    buildElementRelativeVariants(leafBeforeCaret, consumer);
                }
            } catch (ConsumerStoppedException e) {

            }
        }
    }

    private static void addFileRootCompletions(CodeCompletionLookupConsumer consumer) throws ConsumerStoppedException {
        CodeCompletionContext context = consumer.getContext();
        DBLanguagePsiFile file = context.getFile();

        ElementTypeBundle elementTypeBundle = file.getElementTypeBundle();
        ElementTypeLookupCache lookupCache = elementTypeBundle.getRootElementType().getLookupCache();
        ElementLookupContext lookupContext = new ElementLookupContext(context.getDatabaseVersion());
        Set<LeafElementType> firstPossibleLeafs = lookupCache.collectFirstPossibleLeafs(lookupContext);
        for (LeafElementType firstPossibleLeaf : firstPossibleLeafs) {
            if (firstPossibleLeaf instanceof TokenElementType) {
                TokenElementType tokenElementType = (TokenElementType) firstPossibleLeaf;
                consumer.consume(tokenElementType);
            }
        }
    }

    private static String getLeafUniqueKey(LeafElementType leaf) {
        if (leaf instanceof TokenElementType) {
            TokenElementType tokenElementType = (TokenElementType) leaf;
            return tokenElementType.getTokenType().getId();
        } else if (leaf instanceof IdentifierElementType){
            IdentifierElementType identifierElementType = (IdentifierElementType) leaf;
            return identifierElementType.getQualifiedObjectTypeName();
        }
        return null;
    }

    private static void buildElementRelativeVariants(LeafPsiElement element, CodeCompletionLookupConsumer consumer) throws ConsumerStoppedException {

        CodeCompletionContext context = consumer.getContext();
        ConnectionHandler connectionHandler = context.getConnectionHandler();
        boolean isValidConnection = connectionHandler != null && !connectionHandler.isVirtual();

        CodeCompletionFilterSettings filterSettings = context.getCodeCompletionFilterSettings();
        Map<String, LeafElementType> nextPossibleLeafs = new THashMap<String, LeafElementType>();
        IdentifierPsiElement parentIdentifierPsiElement = null;
        DBObject parentObject = null;
        PsiElement parent = element.getParent();
        if (parent instanceof QualifiedIdentifierPsiElement) {
            QualifiedIdentifierPsiElement qualifiedIdentifier = (QualifiedIdentifierPsiElement) parent;
            ElementType separator = qualifiedIdentifier.getElementType().getSeparatorToken();

            if (element.getElementType() == separator){
                BasePsiElement parentPsiElement = element.getPrevElement();
                if (parentPsiElement != null && parentPsiElement instanceof IdentifierPsiElement) {
                    parentIdentifierPsiElement = (IdentifierPsiElement) parentPsiElement;
                    parentObject = parentIdentifierPsiElement.resolveUnderlyingObject();

                    if (parentObject != null) {
                        for (QualifiedIdentifierVariant parseVariant : qualifiedIdentifier.getParseVariants()){
                            boolean match = parseVariant.matchesPsiElement(qualifiedIdentifier);
                            if (match) {
                                int index = qualifiedIdentifier.getIndexOf(parentIdentifierPsiElement);
                                LeafElementType leafElementType = parseVariant.getLeaf(index + 1);
                                if (leafElementType != null) {
                                    nextPossibleLeafs.put(getLeafUniqueKey(leafElementType), leafElementType);
                                }
                            }
                        }
                    }
                }
            }
        } else if (element.getElementType().getTokenType() == element.getLanguage().getSharedTokenTypes().getChrDot()) {
            LeafPsiElement parentPsiElement = element.getPrevLeaf();
            if (parentPsiElement instanceof IdentifierPsiElement) {
                parentIdentifierPsiElement = (IdentifierPsiElement) parentPsiElement;
                parentObject = parentIdentifierPsiElement.resolveUnderlyingObject();
            }
        } else if (parent instanceof BasePsiElement) {
            BasePsiElement basePsiElement = (BasePsiElement) parent;
            ElementType elementType = basePsiElement.getElementType();
            if (elementType.isWrappingBegin(element.getElementType())) {
                Set<LeafElementType> firstPossibleLeafs = elementType.getLookupCache().getFirstPossibleLeafs();
                for (LeafElementType leafElementType : firstPossibleLeafs) {
                    String leafUniqueKey = getLeafUniqueKey(leafElementType);
                    if (leafUniqueKey != null) {
                        nextPossibleLeafs.put(leafUniqueKey, leafElementType);
                    }
                }
            }
        }

        if (nextPossibleLeafs.isEmpty()) {
            LeafElementType elementType = element.getElementType();
            PathNode pathNode = new ASTPathNode(element.getNode());
            ElementLookupContext lookupContext = computeParseBranches(element.getNode(), context.getDatabaseVersion());
            for (LeafElementType leafElementType : elementType.getNextPossibleLeafs(pathNode, lookupContext)) {
                String leafUniqueKey = getLeafUniqueKey(leafElementType);
                if (leafUniqueKey != null) {
                    nextPossibleLeafs.put(leafUniqueKey, leafElementType);    
                }
            }
        }

        for (LeafElementType nextPossibleLeaf : nextPossibleLeafs.values()) {
            consumer.check();
            //boolean addParenthesis =
            //        nextPossibleLeaf.getLookupCache().getNextRequiredTokens().contains(
            //                element.getLanguage().getSharedTokenTypes().getLeftParenthesis());
            //consumer.setAddParenthesis(addParenthesis);

            if (nextPossibleLeaf instanceof TokenElementType) {
                TokenElementType tokenElementType = (TokenElementType) nextPossibleLeaf;
                //consumer.setAddParenthesis(addParenthesis && tokenType.isFunction());
                consumer.consume(tokenElementType);
            }
            else if (nextPossibleLeaf instanceof IdentifierElementType) {
                IdentifierElementType identifierElementType = (IdentifierElementType) nextPossibleLeaf;
                if (identifierElementType.isReference()) {
                    DBObjectType objectType = identifierElementType.getObjectType();
                    if (identifierElementType.isObject()) {
                        PsiLookupAdapter lookupAdapter = LookupAdapterCache.OBJECT_DEFINITION.get(objectType);
                        Set<BasePsiElement> objectDefinitions = lookupAdapter.collectInParentScopeOf(element);
                        if (objectDefinitions != null && parentIdentifierPsiElement == null) {
                            for (BasePsiElement psiElement : objectDefinitions) {
                                if (psiElement instanceof IdentifierPsiElement) {
                                    IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) psiElement;
                                    PsiElement referencedPsiElement = identifierPsiElement.resolve();
                                    if (referencedPsiElement instanceof DBObject) {
                                        DBObject object = (DBObject) referencedPsiElement;
                                        consumer.consume(object);
                                    } else {
                                        consumer.consume(identifierPsiElement);
                                    }
                                }
                            }
                        }

                        if (parentIdentifierPsiElement == null) {
                            BasePsiElement scope = element.getEnclosingScopePsiElement();
                            collectObjectMatchingScope(consumer, identifierElementType, filterSettings, scope, context);
                        }
                    } else if (parentIdentifierPsiElement == null) {
                        if (identifierElementType.isAlias()) {
                            PsiLookupAdapter lookupAdapter = LookupAdapterCache.ALIAS_DEFINITION.get(objectType);
                            Set<BasePsiElement> aliasPsiElements = lookupAdapter.collectInParentScopeOf(element);
                            consumer.consume(aliasPsiElements);
                        } else if (identifierElementType.isVariable()) {
                            PsiLookupAdapter lookupAdapter = LookupAdapterCache.VARIABLE_DEFINITION.get(objectType);
                            Set<BasePsiElement> variablePsiElements = lookupAdapter.collectInParentScopeOf(element);
                            consumer.consume(variablePsiElements);
                        }
                    }

                    if (parentObject != null && (isValidConnection || parentObject instanceof DBVirtualObject)) {
                        List<DBObject> childObjects = parentObject.getChildObjects(identifierElementType.getObjectType());
                        consumer.consume(childObjects);
                    }
                } else if (identifierElementType.isDefinition()) {
                    if (identifierElementType.isAlias()) {
                        String[] aliasNames = buildAliasDefinitionNames(element);
                        consumer.consume(aliasNames);
                    }
                }
            }
        }
    }

    @Nullable
    private static ElementLookupContext computeParseBranches(ASTNode astNode, double databaseVersion) {
        ElementLookupContext lookupContext = new ElementLookupContext(databaseVersion);
        while (astNode != null && !(astNode instanceof FileElement)) {
            IElementType elementType = astNode.getElementType();
            if (elementType instanceof ElementType) {
                ElementType basicElementType = (ElementType) elementType;
                Branch branch = basicElementType.getBranch();
                if (branch != null) {
                    lookupContext.addBranchMarker(astNode, branch);
                }
            }
            ASTNode prevAstNode = astNode.getTreePrev();
            if (prevAstNode == null) {
                prevAstNode = astNode.getTreeParent();
            }
            astNode = prevAstNode;
        }
        return lookupContext;
    }

    public static String[] buildAliasDefinitionNames(BasePsiElement aliasElement) {
        IdentifierPsiElement aliasedObject = PsiUtil.lookupObjectPriorTo(aliasElement, DBObjectType.ANY);
        if (aliasedObject != null && aliasedObject.isObject()) {
            String[] aliasNames = NamingUtil.createAliasNames(aliasedObject.getUnquotedText());

            BasePsiElement scope = aliasElement.getEnclosingScopePsiElement();

            for (int i = 0; i< aliasNames.length; i++) {
                while (true) {
                    PsiLookupAdapter lookupAdapter = new AliasDefinitionLookupAdapter(null, DBObjectType.ANY, aliasNames[i]);
                    boolean isExisting = lookupAdapter.findInScope(scope) != null;
                    boolean isKeyword = aliasElement.getLanguageDialect().isReservedWord(aliasNames[i]);
                    if (isKeyword || isExisting) {
                        aliasNames[i] = NamingUtil.getNextNumberedName(aliasNames[i], false);
                    } else {
                        break;
                    }
                }
            }
            return aliasNames;
        }
        return new String[0];
    }

    private static void collectObjectMatchingScope(
            LookupConsumer consumer,
            IdentifierElementType identifierElementType,
            ObjectTypeFilter filter,
            BasePsiElement sourceScope,
            CodeCompletionContext context) throws ConsumerStoppedException {
        DBObjectType objectType = identifierElementType.getObjectType();
        PsiElement sourceElement = context.getElementAtCaret();
        ConnectionHandler connectionHandler = context.getConnectionHandler();

        if (connectionHandler != null && !connectionHandler.isDisposed() && !connectionHandler.isVirtual()) {
            DBObjectBundle objectBundle = connectionHandler.getObjectBundle();
            if (sourceElement.getParent() instanceof QualifiedIdentifierPsiElement && sourceElement.getParent().getFirstChild() != sourceElement) {
                QualifiedIdentifierPsiElement qualifiedIdentifierPsiElement = (QualifiedIdentifierPsiElement) sourceElement.getOriginalElement().getParent();
                DBObject parentObject = qualifiedIdentifierPsiElement.lookupParentObjectFor(identifierElementType);
                if (parentObject != null) {
                    DBSchema currentSchema = PsiUtil.getCurrentSchema(sourceScope);
                    objectBundle.lookupChildObjectsOfType(
                            consumer,
                            parentObject,
                            objectType,
                            filter,
                            currentSchema);

                }
            } else if (!identifierElementType.isLocalReference()){
                Set<DBObject> parentObjects = LeafPsiElement.identifyPotentialParentObjects(objectType, filter, sourceScope, null);
                if (parentObjects != null && parentObjects.size() > 0) {
                    for (DBObject parentObject : parentObjects) {
                        DBSchema currentSchema = PsiUtil.getCurrentSchema(sourceScope);
                        objectBundle.lookupChildObjectsOfType(
                                consumer,
                                parentObject.getUndisposedElement(),
                                objectType,
                                filter,
                                currentSchema);
                    }
                } else {
                    if (filter.acceptsRootObject(objectType)) {
                        objectBundle.lookupObjectsOfType(
                                consumer,
                                objectType);
                    }
                }
            }
        }
    }
}
