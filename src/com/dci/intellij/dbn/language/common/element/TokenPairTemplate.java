package com.dci.intellij.dbn.language.common.element;

public enum TokenPairTemplate {
    PARENTHESES("CHR_LEFT_PARENTHESIS", "CHR_RIGHT_PARENTHESIS", false),
    BRACKETS("CHR_LEFT_BRACKET", "CHR_RIGHT_BRACKET", false),
    BEGIN_END("KW_BEGIN", "KW_END", true);

    private String beginToken;
    private String endToken;
    private boolean block;

    private TokenPairTemplate(String beginToken, String endToken, boolean block) {
        this.beginToken = beginToken;
        this.endToken = endToken;
        this.block = block;
    }

    public String getBeginToken() {
        return beginToken;
    }

    public String getEndToken() {
        return endToken;
    }

    public boolean isBlock() {
        return block;
    }

    public static TokenPairTemplate get(String tokenTypeId) {
        for (TokenPairTemplate tokenPairTemplate : values()) {
            if (tokenPairTemplate.beginToken.equals(tokenTypeId) || tokenPairTemplate.endToken.equals(tokenTypeId)) {
                return tokenPairTemplate;
            }
        }
        return null;
    }
}
