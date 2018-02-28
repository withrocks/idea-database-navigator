package com.dci.intellij.dbn.language.common;

public enum DBLanguageDialectIdentifier {
    ORACLE_SQL("ORACLE-SQL"),
    ORACLE_PLSQL("ORACLE-PLSQL"),
    MYSQL_SQL("MYSQL-SQL"),
    MYSQL_PSQL("MYSQL-PSQL"),
    POSTGRES_SQL("POSTGRES-SQL"),
    POSTGRES_PSQL("POSTGRES-PSQL"),
    ISO92_SQL("ISO92-SQL");

    private String value;

    DBLanguageDialectIdentifier(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
