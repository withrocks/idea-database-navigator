package com.dci.intellij.dbn.code.common.completion;

import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.code.common.intention.DatabaseConnectIntentionAction;
import com.dci.intellij.dbn.code.common.intention.DebugMethodIntentionAction;
import com.dci.intellij.dbn.code.common.intention.ExecuteStatementIntentionAction;
import com.dci.intellij.dbn.code.common.intention.ExplainPlanIntentionAction;
import com.dci.intellij.dbn.code.common.intention.JumpToExecutionResultIntentionAction;
import com.dci.intellij.dbn.code.common.intention.RunMethodIntentionAction;
import com.dci.intellij.dbn.code.common.intention.SelectConnectionIntentionAction;
import com.dci.intellij.dbn.code.common.intention.SelectCurrentSchemaIntentionAction;
import com.dci.intellij.dbn.code.common.intention.ToggleDatabaseLoggingIntentionAction;
import com.intellij.codeInsight.intention.IntentionManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;

@State(
        name = "DBNavigator.Project.CodeCompletionManager",
        storages = {
                @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
                @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class CodeCompletionManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final int BASIC_CODE_COMPLETION = 0;
    public static final int SMART_CODE_COMPLETION = 1;

    private CodeCompletionManager(Project project) {
        super(project);
        // fixme move these calls to a more appropriate place (nothing to do with code completion)
        IntentionManager intentionManager = IntentionManager.getInstance(project);
        intentionManager.addAction(new ExecuteStatementIntentionAction());
        intentionManager.addAction(new RunMethodIntentionAction());
        intentionManager.addAction(new DebugMethodIntentionAction());
        intentionManager.addAction(new ExplainPlanIntentionAction());
        intentionManager.addAction(new DatabaseConnectIntentionAction());
        intentionManager.addAction(new JumpToExecutionResultIntentionAction());
        intentionManager.addAction(new SelectConnectionIntentionAction());
        intentionManager.addAction(new SelectCurrentSchemaIntentionAction());
        intentionManager.addAction(new ToggleDatabaseLoggingIntentionAction());
        //intentionManager.addAction(new SetupCodeCompletionIntentionAction());
    }

    public static CodeCompletionManager getInstance(Project project) {
        return project.getComponent(CodeCompletionManager.class);
    }

    /***************************************
    *            ProjectComponent           *
    ****************************************/
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.CodeCompletionManager";
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        return null;
    }

    @Override
    public void loadState(Element element) {
    }
}
