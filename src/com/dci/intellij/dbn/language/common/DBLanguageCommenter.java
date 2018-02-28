package com.dci.intellij.dbn.language.common;

public class DBLanguageCommenter implements com.intellij.lang.Commenter {

    public static final DBLanguageCommenter COMMENTER = new DBLanguageCommenter();

    public String getLineCommentPrefix() {
        return "--";
    }

    public boolean isLineCommentPrefixOnZeroColumn() {
        return false;
    }

    public String getBlockCommentPrefix() {
        return "/*";
    }

    public String getBlockCommentSuffix() {
        return "*/";
    }

    public String getCommentedBlockCommentPrefix() {
        return null;
    }

    public String getCommentedBlockCommentSuffix() {
        return null;
    }
}
