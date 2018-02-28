package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.object.common.DBSchemaObject;

public interface DBTrigger extends DBSchemaObject {
    TriggerType TRIGGER_TYPE_BEFORE     = new TriggerType(0, "before");
    TriggerType TRIGGER_TYPE_AFTER      = new TriggerType(1, "after");
    TriggerType TRIGGER_TYPE_INSTEAD_OF = new TriggerType(2, "instead of");
    TriggerType TRIGGER_TYPE_UNKNOWN    = new TriggerType(3, "unknown");

    TriggeringEvent TRIGGERING_EVENT_INSERT   = new TriggeringEvent(0, "insert");
    TriggeringEvent TRIGGERING_EVENT_UPDATE   = new TriggeringEvent(1, "update");
    TriggeringEvent TRIGGERING_EVENT_DELETE   = new TriggeringEvent(2, "delete");
    TriggeringEvent TRIGGERING_EVENT_TRUNCATE = new TriggeringEvent(3, "truncate");
    TriggeringEvent TRIGGERING_EVENT_DROP     = new TriggeringEvent(4, "drop");
    TriggeringEvent TRIGGERING_EVENT_LOGON    = new TriggeringEvent(5, "logon");
    TriggeringEvent TRIGGERING_EVENT_ALTER    = new TriggeringEvent(5, "alter");
    TriggeringEvent TRIGGERING_EVENT_CREATE   = new TriggeringEvent(5, "create");
    TriggeringEvent TRIGGERING_EVENT_RENAME   = new TriggeringEvent(5, "rename");
    TriggeringEvent TRIGGERING_EVENT_DDL      = new TriggeringEvent(5, "ddl");
    TriggeringEvent TRIGGERING_EVENT_UNKNOWN  = new TriggeringEvent(6, "unknown");

    boolean isForEachRow();
    TriggerType getTriggerType();
    TriggeringEvent[] getTriggeringEvents();

    class TriggerType {
        private int id;
        private String name;

        public TriggerType(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    class TriggeringEvent {
        private int id;
        private String name;

        public TriggeringEvent(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}