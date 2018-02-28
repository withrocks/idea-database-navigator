package com.dci.intellij.dbn.execution.method.browser.action;

import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.execution.method.browser.ui.MethodExecutionBrowserForm;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class SelectSchemaAction extends DumbAwareAction {
    private DBSchema schema;
    private MethodExecutionBrowserForm browserComponent;

    public SelectSchemaAction(MethodExecutionBrowserForm browserComponent, DBSchema schema) {
        super(NamingUtil.enhanceUnderscoresForDisplay(schema.getQualifiedName()), null, schema.getIcon());
        this.browserComponent = browserComponent;
        this.schema = schema;


    }

    public void actionPerformed(AnActionEvent e) {
        browserComponent.setSchema(schema);
    }


}
