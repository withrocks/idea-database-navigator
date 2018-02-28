package com.dci.intellij.dbn.execution.common.message;

import com.dci.intellij.dbn.common.message.Message;
import com.dci.intellij.dbn.common.message.MessageType;
import com.intellij.openapi.Disposable;

public abstract class ConsoleMessage extends Message implements Disposable {
    public ConsoleMessage(MessageType type, String text) {
        super(type, text);
    }
}
