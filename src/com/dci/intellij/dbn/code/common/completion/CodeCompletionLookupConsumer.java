package com.dci.intellij.dbn.code.common.completion;

import java.util.Collection;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.code.common.completion.options.filter.CodeCompletionFilterSettings;
import com.dci.intellij.dbn.code.common.lookup.AliasLookupItemBuilder;
import com.dci.intellij.dbn.code.common.lookup.BasicLookupItemBuilder;
import com.dci.intellij.dbn.code.common.lookup.IdentifierLookupItemBuilder;
import com.dci.intellij.dbn.code.common.lookup.LookupItemBuilder;
import com.dci.intellij.dbn.code.common.lookup.VariableLookupItemBuilder;
import com.dci.intellij.dbn.common.lookup.ConsumerStoppedException;
import com.dci.intellij.dbn.common.lookup.LookupConsumer;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.util.IdentifierType;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class CodeCompletionLookupConsumer implements LookupConsumer {
    private CodeCompletionContext context;
    boolean addParenthesis;

    public CodeCompletionLookupConsumer(CodeCompletionContext context) {
        this.context = context;
    }

    @Override
    public void consume(Object object) throws ConsumerStoppedException {
        check();

        LookupItemBuilder lookupItemBuilder = null;
        if (object instanceof DBObject) {
            DBObject dbObject = (DBObject) object;
            lookupItemBuilder = dbObject.getLookupItemBuilder(context.getLanguage());

        } else if (object instanceof TokenElementType) {
            TokenElementType tokenElementType = (TokenElementType) object;
            CodeCompletionFilterSettings filterSettings = context.getCodeCompletionFilterSettings();
            TokenTypeCategory tokenTypeCategory = tokenElementType.getTokenTypeCategory();
            if (tokenTypeCategory == TokenTypeCategory.OBJECT) {
                TokenType tokenType = tokenElementType.getTokenType();
                DBObjectType objectType = tokenType.getObjectType();
                if (objectType != null) {
                    if (filterSettings.acceptsRootObject(objectType)) {
                        lookupItemBuilder = new BasicLookupItemBuilder(tokenType.getValue(), objectType.getName(), objectType.getIcon());
                    }
                }
            } else if (filterSettings.acceptReservedWord(tokenTypeCategory)) {
                lookupItemBuilder = tokenElementType.getLookupItemBuilder(context.getLanguage());
            }
        } else if (object instanceof IdentifierPsiElement) {
            IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) object;
            if (identifierPsiElement.isValid()) {
                CharSequence chars = identifierPsiElement.getChars();
                IdentifierType identifierType = identifierPsiElement.getIdentifierType();
                if (identifierType == IdentifierType.VARIABLE) {
                    lookupItemBuilder = new VariableLookupItemBuilder(chars, true);
                } else if (identifierType == IdentifierType.ALIAS) {
                    lookupItemBuilder = new AliasLookupItemBuilder(chars, true);
                } else if (identifierType == IdentifierType.OBJECT && identifierPsiElement.isDefinition()) {
                    lookupItemBuilder = new IdentifierLookupItemBuilder(identifierPsiElement);

                }
            }
        } else if (object instanceof String) {
            lookupItemBuilder = new AliasLookupItemBuilder((CharSequence) object, true);
        }

        if (lookupItemBuilder != null) {
            lookupItemBuilder.createLookupItem(object, this);
        }
    }

    @Override
    public void consume(Object[] objects) throws ConsumerStoppedException {
        check();
        for (Object object : objects) {
            consume(object);
        }

    }

    public void consume(@Nullable Collection objects) throws ConsumerStoppedException {
        if (objects != null) {
            check();
            for (Object object : objects) {
                consume(object);
            }
        }
    }

    public void setAddParenthesis(boolean addParenthesis) {
        this.addParenthesis = addParenthesis;
    }

    @Override
    public void check() throws ConsumerStoppedException {
        if (context.getResult().isStopped()) {
            throw new ConsumerStoppedException();
        }
    }

    public CodeCompletionContext getContext() {
        return context;
    }

    public boolean isAddParenthesis() {
        return addParenthesis;
    }
}
