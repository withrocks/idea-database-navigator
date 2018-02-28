package com.dci.intellij.dbn.language.sql;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class SQLFileType extends DBLanguageFileType {

    public static final SQLFileType INSTANCE = new SQLFileType(
            SQLLanguage.INSTANCE,
            "sql", "SQL files (DBN)", DBContentType.CODE);

    public static final SQLFileType DATA_FILE_TYPE = new SQLFileType(
            SQLLanguage.INSTANCE,
            "ds", "Dataset", DBContentType.DATA);

    public SQLFileType(@NotNull Language language, String extension, String description, DBContentType contentType) {
        super(language, extension, description, contentType);
    }


    @NotNull
    public String getName() {
        return "DBN-SQL";
    }

    public Icon getIcon() {
        return Icons.FILE_SQL;
    }


}
