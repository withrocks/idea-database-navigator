package com.dci.intellij.dbn.language.common;

import org.jdom.Document;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.NamedElementType;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;

public abstract class DBLanguageParser implements PsiParser {
    private DBLanguageDialect languageDialect;
    private ElementTypeBundle elementTypes;
    private TokenTypeBundle tokenTypes;
    private String defaultParseRootId;

    public DBLanguageParser(DBLanguageDialect languageDialect, String tokenTypesFile, String elementTypesFile, String defaultParseRootId) {
        this.languageDialect = languageDialect;
        this.tokenTypes = new TokenTypeBundle(languageDialect, CommonUtil.loadXmlFile(getClass(), tokenTypesFile));
        Document document = CommonUtil.loadXmlFile(getClass(), elementTypesFile);
        this.elementTypes = new ElementTypeBundle(languageDialect, tokenTypes, document);
        this.defaultParseRootId = defaultParseRootId;
    }

    public DBLanguageDialect getLanguageDialect() {
        return languageDialect;
    }

    @NotNull
    public ASTNode parse(IElementType rootElementType, PsiBuilder builder) {
        return parse(rootElementType, builder, defaultParseRootId, 9999);
    }

    @NotNull
    public ASTNode parse(IElementType rootElementType, PsiBuilder psiBuilder, String parseRootId, double databaseVersion) {
        ParserContext context = new ParserContext(psiBuilder, languageDialect, databaseVersion);
        ParserBuilder builder = context.getBuilder();
        if (parseRootId == null ) parseRootId = defaultParseRootId;
        builder.setDebugMode(SettingsUtil.isDebugEnabled);
        PsiBuilder.Marker marker = builder.mark(null);
        NamedElementType root =  elementTypes.getNamedElementType(parseRootId);
        if (root == null) {
            root = elementTypes.getRootElementType();
        }

        boolean advancedLexer = false;
        ParsePathNode rootParseNode = new ParsePathNode(root, null, 0, 0);

        try {
            while (!builder.eof()) {
                int currentOffset =  builder.getCurrentOffset();
                root.getParser().parse(rootParseNode, true, 0, context);
                if (currentOffset == builder.getCurrentOffset()) {
                    TokenType tokenType = builder.getTokenType();
                    /*if (tokenType.isChameleon()) {
                        PsiBuilder.Marker injectedLanguageMarker = builder.mark();
                        builder.advanceLexer();
                        injectedLanguageMarker.done((IElementType) tokenType);
                    }
                    else*/ if (tokenType instanceof ChameleonTokenType) {
                        PsiBuilder.Marker injectedLanguageMarker = builder.mark(null);
                        builder.advanceLexer(rootParseNode);
                        injectedLanguageMarker.done((IElementType) tokenType);
                    } else {
                        builder.advanceLexer(rootParseNode);
                    }
                    advancedLexer = true;
                }
            }
        } catch (ParseException e) {
            while (!builder.eof()) {
                builder.advanceLexer(rootParseNode);
                advancedLexer = true;
            }
        } catch (StackOverflowError e) {
            marker.rollbackTo();
            marker = builder.mark(null);
            while (!builder.eof()) {
                builder.advanceLexer(rootParseNode);
                advancedLexer = true;
            }

        }

        if (!advancedLexer) builder.advanceLexer(rootParseNode);
        marker.done(rootElementType);
        return builder.getTreeBuilt();
    }

    public TokenTypeBundle getTokenTypes() {
        return tokenTypes;
    }

    public ElementTypeBundle getElementTypes() {
        return elementTypes;
    }
}
