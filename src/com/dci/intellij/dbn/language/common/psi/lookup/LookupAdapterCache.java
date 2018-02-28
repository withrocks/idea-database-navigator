package com.dci.intellij.dbn.language.common.psi.lookup;

import java.util.EnumMap;
import java.util.Map;

import com.dci.intellij.dbn.object.common.DBObjectType;

public class LookupAdapterCache {
    public static Cache<DBObjectType, ObjectDefinitionLookupAdapter> OBJECT_DEFINITION = new Cache<DBObjectType, ObjectDefinitionLookupAdapter>(DBObjectType.class) {
        @Override
        protected ObjectDefinitionLookupAdapter createValue(DBObjectType key) {
            return new ObjectDefinitionLookupAdapter(null, key, null);
        }
    };

    public static Cache<DBObjectType, AliasDefinitionLookupAdapter> ALIAS_DEFINITION = new Cache<DBObjectType, AliasDefinitionLookupAdapter>(DBObjectType.class) {
        @Override
        protected AliasDefinitionLookupAdapter createValue(DBObjectType key) {
            return new AliasDefinitionLookupAdapter(null, key);
        }
    };
    public static Cache<DBObjectType, VariableDefinitionLookupAdapter> VARIABLE_DEFINITION = new Cache<DBObjectType, VariableDefinitionLookupAdapter>(DBObjectType.class) {
        @Override
        protected VariableDefinitionLookupAdapter createValue(DBObjectType key) {
            return new VariableDefinitionLookupAdapter(null, key, null);
        }
    };


    public abstract static class Cache <K extends Enum<K>, V extends IdentifierLookupAdapter> {
        private Map<K, V> CACHE;

        public Cache(Class<K> keyClass) {
            CACHE = new EnumMap<K, V>(keyClass);
        }

        public V get(K key) {
            V value = CACHE.get(key);
            if (value == null) {
                value = createValue(key);
                CACHE.put(key, value);
            }
            return value;
        }

        protected abstract V createValue(K key);
    }
}
