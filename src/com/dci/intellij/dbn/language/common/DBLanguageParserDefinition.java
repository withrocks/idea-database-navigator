package com.dci.intellij.dbn.language.common;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public abstract class DBLanguageParserDefinition implements ParserDefinition {
    private DBLanguageParser parser;

    public DBLanguageParserDefinition(DBLanguageParser parser) {
        this.parser = parser;
    }

    public DBLanguageParser getParser() {
        return parser;
    }

    @NotNull
    public PsiElement createElement(ASTNode astNode) {
        IElementType et = astNode.getElementType();
        if(et instanceof ElementType) {
            ElementType elementType = (ElementType) et;
            //SQLFile file = lookupFile(astNode);
            return elementType.createPsiElement(astNode);
        }
        return new ASTWrapperPsiElement(astNode);
    }

    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return null;
    }

    public IFileElementType getFileNodeType() {
        return parser.getLanguageDialect().getBaseLanguage().getFileElementType();
        /*DBLanguageDialect languageDialect = parser.getLanguageDialect();
        return languageDialect.getFileElementType();*/
    }

    @NotNull
    public TokenSet getWhitespaceTokens() {
        return parser.getTokenTypes().getSharedTokenTypes().getWhitespaceTokens();
    }

    @NotNull
    public TokenSet getCommentTokens() {
        return parser.getTokenTypes().getSharedTokenTypes().getCommentTokens();
    }

    @NotNull
    public TokenSet getStringLiteralElements() {
        return parser.getTokenTypes().getSharedTokenTypes().getStringTokens();
    }
}
