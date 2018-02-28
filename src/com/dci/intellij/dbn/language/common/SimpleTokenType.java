package com.dci.intellij.dbn.language.common;

import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingDefinition;
import com.dci.intellij.dbn.code.common.style.formatting.FormattingDefinitionFactory;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.language.common.element.TokenPairTemplate;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.lang.Language;
import com.intellij.psi.tree.IElementType;

public class SimpleTokenType extends IElementType implements TokenType {
    private String id;
    private String value;
    private String description;
    private boolean isSuppressibleReservedWord;
    private TokenTypeCategory category;
    private DBObjectType objectType;
    private int idx;
    private int hashCode;
    private FormattingDefinition formatting;
    private TokenPairTemplate tokenPairTemplate;

    public SimpleTokenType(@NotNull @NonNls String debugName, @Nullable Language language) {
        super(debugName, language);
    }

    public SimpleTokenType(SimpleTokenType source, Language language) {
        super(source.toString(), language);
        this.id = source.id;
        this.value = source.getValue();
        this.description = source.description;
        isSuppressibleReservedWord = source.isSuppressibleReservedWord();
        this.category = source.category;
        this.objectType = source.objectType;
        this.idx = source.idx;

        formatting = FormattingDefinitionFactory.cloneDefinition(source.getFormatting());
        tokenPairTemplate = TokenPairTemplate.get(id);
    }

    public SimpleTokenType(Element element, Language language) {
        super(element.getAttributeValue("id"), language);
        id = element.getAttributeValue("id");
        value = element.getAttributeValue("value");
        description = element.getAttributeValue("description");

        String indexString = element.getAttributeValue("index");
        if (StringUtil.isNotEmptyOrSpaces(indexString)) {
            idx = Integer.parseInt(indexString);
        }

        String type = element.getAttributeValue("type");
        category = TokenTypeCategory.getCategory(type);
        isSuppressibleReservedWord = isReservedWord() && !Boolean.parseBoolean(element.getAttributeValue("reserved"));
        hashCode = (language.getDisplayName() + id).hashCode();

        String objectType = element.getAttributeValue("objectType");
        if (StringUtil.isNotEmpty(objectType)) {
            this.objectType = DBObjectType.getObjectType(objectType);
        }

        formatting = FormattingDefinitionFactory.loadDefinition(element);
        tokenPairTemplate = TokenPairTemplate.get(id);
    }

    @Override
    public TokenPairTemplate getTokenPairTemplate() {
        return tokenPairTemplate;
    }

    public void setDefaultFormatting(FormattingDefinition defaultFormatting) {
        formatting = FormattingDefinitionFactory.mergeDefinitions(formatting, defaultFormatting);
    }

    public String getId() {
        return id;
    }


    public int getIdx() {
        return idx;
    }

    public String getValue() {
        return value == null ? "" : value;
    }

    public String getDescription() {
        return description;
    }

    public String getTypeName() {
        return category.getName();
    }

    public int compareTo(Object o) {
        SimpleTokenType tokenType = (SimpleTokenType) o;
        return getValue().compareTo(tokenType.getValue());
    }

    public boolean isSuppressibleReservedWord() {
        return isReservedWord() && isSuppressibleReservedWord;
    }

    public boolean isIdentifier() {
        return category == TokenTypeCategory.IDENTIFIER;
    }

    public boolean isVariable() {
        return getSharedTokenTypes().isVariable(this);
    }

    public boolean isQuotedIdentifier() {
        return this == getSharedTokenTypes().getQuotedIdentifier();
    }

    public boolean isKeyword() {
        return category == TokenTypeCategory.KEYWORD;
    }

    public boolean isFunction() {
        return category == TokenTypeCategory.FUNCTION;
    }

    public boolean isParameter() {
        return category == TokenTypeCategory.PARAMETER;
    }

    public boolean isDataType() {
        return category == TokenTypeCategory.DATATYPE;
    }

    @Override
    public boolean isLiteral() {
        return category == TokenTypeCategory.LITERAL;
    }

    @Override
    public boolean isNumeric() {
        return category == TokenTypeCategory.NUMERIC;
    }

    @Override
    public boolean isCharacter() {
        return category == TokenTypeCategory.CHARACTER;
    }

    @Override
    public boolean isOperator() {
        return category == TokenTypeCategory.OPERATOR;
    }

    @Override
    public boolean isChameleon() {
        return category == TokenTypeCategory.CHAMELEON;
    }

    @Override
    public boolean isReservedWord() {
        return isKeyword() || isFunction() || isParameter() || isDataType();
    }

    @Override
    public boolean isParserLandmark() {
        return !isIdentifier();
        //return isKeyword() || isFunction() || isParameter() || isCharacter() || isOperator();
        //return isCharacter() || isOperator() || !isSuppressibleReservedWord();
    }

    public TokenTypeCategory getCategory() {
        return category;
    }

    @Nullable
    @Override
    public DBObjectType getObjectType() {
        return objectType;
    }

    @Override
    public FormattingDefinition getFormatting() {
        if (formatting == null) {
            formatting = new FormattingDefinition();
        }
        return formatting;
    }

    private SharedTokenTypeBundle getSharedTokenTypes() {
        Language lang = getLanguage();
        if (lang instanceof DBLanguageDialect) {
            DBLanguageDialect languageDialect = (DBLanguageDialect) lang;
            return languageDialect.getSharedTokenTypes();
        } else if (lang instanceof DBLanguage) {
            DBLanguage language = (DBLanguage) lang;
            return language.getSharedTokenTypes();
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof SimpleTokenType) {
            SimpleTokenType simpleTokenType = (SimpleTokenType) obj;
            return simpleTokenType.getLanguage().equals(getLanguage()) &&
                    simpleTokenType.id.equals(id);
        }
        return false;
    }

    @Override
    public boolean matches(TokenType tokenType) {
        if (this.equals(tokenType)) return true;
        if (this.isIdentifier() && tokenType.isIdentifier()) return true;
        return false;
    }

    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean isOneOf(TokenType... tokenTypes) {
        for (TokenType tokenType : tokenTypes) {
            if (this == tokenType) return true;
        }
        return false;
    }
}
