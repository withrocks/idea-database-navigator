package com.dci.intellij.dbn.database.generic;

import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseDDLInterface;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.database.DatabaseExecutionInterface;
import com.dci.intellij.dbn.database.DatabaseMessageParserInterface;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.database.common.DatabaseInterfaceProviderImpl;
import com.dci.intellij.dbn.database.common.DatabaseNativeDataTypes;
import com.dci.intellij.dbn.language.common.DBLanguageDialectIdentifier;
import com.dci.intellij.dbn.language.sql.SQLLanguage;

public class GenericInterfaceProvider extends DatabaseInterfaceProviderImpl {
    private DatabaseMessageParserInterface MESSAGE_PARSER_INTERFACE = new GenericMessageParserInterface();
    private DatabaseCompatibilityInterface COMPATIBILITY_INTERFACE = new GenericCompatibilityInterface(this);
    private DatabaseMetadataInterface METADATA_INTERFACE = new GenericMetadataInterface(this);
    private DatabaseDDLInterface DDL_INTERFACE = new GenericDDLInterface(this);
    private DatabaseExecutionInterface EXECUTION_INTERFACE = new GenericExecutionInterface();
    private DatabaseNativeDataTypes NATIVE_DATA_TYPES = new GenericNativeDataTypes();

    public GenericInterfaceProvider() {
        super(SQLLanguage.INSTANCE.getLanguageDialect(DBLanguageDialectIdentifier.ISO92_SQL), null);
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.UNKNOWN;
    }

    public DatabaseNativeDataTypes getNativeDataTypes() {
        return NATIVE_DATA_TYPES;
    }

    public DatabaseMessageParserInterface getMessageParserInterface() {
        return MESSAGE_PARSER_INTERFACE;
    }

    public DatabaseCompatibilityInterface getCompatibilityInterface() {
        return COMPATIBILITY_INTERFACE;
    }

    public DatabaseMetadataInterface getMetadataInterface() {
        return METADATA_INTERFACE;
    }

    public DatabaseDebuggerInterface getDebuggerInterface() {
        return null;
    }

    public DatabaseDDLInterface getDDLInterface() {
        return DDL_INTERFACE;
    }

    public DatabaseExecutionInterface getDatabaseExecutionInterface() {
        return EXECUTION_INTERFACE;
    }


}
