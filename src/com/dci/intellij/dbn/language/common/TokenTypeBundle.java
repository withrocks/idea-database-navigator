package com.dci.intellij.dbn.language.common;

import org.jdom.Document;

import com.dci.intellij.dbn.language.common.element.ChameleonElementType;
import com.intellij.psi.tree.TokenSet;

public class TokenTypeBundle extends DBLanguageTokenTypeBundle {
    private DBLanguage language;

    public TokenTypeBundle(DBLanguageDialect languageDialect, Document document) {
        super(languageDialect, document);
        language = languageDialect.getBaseLanguage();
    }

    protected void loadDefinition(DBLanguageDialect languageDialect, Document document) {
        super.loadDefinition(languageDialect, document);
    }

    public SharedTokenTypeBundle getSharedTokenTypes() {
        return language.getSharedTokenTypes();
    }

    public DBLanguageDialect getLanguageDialect() {
        return (DBLanguageDialect) getLanguage();
    }

    @Override
    public SimpleTokenType getCharacterTokenType(int index) {
        return getSharedTokenTypes().getCharacterTokenType(index);
    }

    @Override
    public SimpleTokenType getOperatorTokenType(int index) {
        return getSharedTokenTypes().getOperatorTokenType(index);
    }

    public SimpleTokenType getTokenType(String id) {
        SimpleTokenType tokenType = super.getTokenType(id);
        if (tokenType == null) {
            tokenType = getSharedTokenTypes().getTokenType(id);
            if (tokenType == null) {
                System.out.println("DEBUG - [" + getLanguage().getID() + "] undefined token type: " + id);
                //log.info("[DBN-WARNING] Undefined token type: " + id);
                return getSharedTokenTypes().getIdentifier();
            }
        }
        return tokenType;
    }

    public TokenSet getTokenSet(String id) {
        TokenSet tokenSet = super.getTokenSet(id);
        if (tokenSet == null) {
            tokenSet = getSharedTokenTypes().getTokenSet(id);
            if (tokenSet == null) {
                System.out.println("DEBUG - [" + getLanguage().getID() + "] undefined token set: " + id);
                //log.info("[DBN-WARNING] Undefined token set '" + id + "'");
                tokenSet = super.getTokenSet("UNDEFINED");
            }
        }
        return tokenSet;
    }

    public SimpleTokenType getIdentifier() {
        return getSharedTokenTypes().getIdentifier();
    }

    public SimpleTokenType getVariable() {
        return getSharedTokenTypes().getVariable();
    }

    public SimpleTokenType getString() {
        return getSharedTokenTypes().getString();
    }


    public ChameleonElementType getChameleon(DBLanguageDialectIdentifier dialectIdentifier) {
        return getLanguageDialect().getChameleonTokenType(dialectIdentifier);
    }
}
