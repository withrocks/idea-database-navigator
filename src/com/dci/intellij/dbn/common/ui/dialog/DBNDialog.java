package com.dci.intellij.dbn.common.ui.dialog;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

public abstract class DBNDialog<C extends DBNForm> extends DialogWrapper implements DisposableProjectComponent{
    protected C component;
    private Project project;
    private boolean disposed;
    private boolean rememberSelection;

    protected DBNDialog(Project project, String title, boolean canBeParent) {
        super(project, canBeParent);
        setTitle(Constants.DBN_TITLE_PREFIX + title);
        this.project = project;
    }

    public final C getComponent() {
        return component;
    }

    @Nullable
    protected final JComponent createCenterPanel() {
        if (component == null) throw new IllegalStateException("Component not created");
        return component.getComponent();
    }

    protected final String getDimensionServiceKey() {
        return "DBNavigator." + getClass().getSimpleName();
    }


    public Project getProject() {
        return project;
    }

    public boolean isRememberSelection() {
        return rememberSelection;
    }

    public void registerRememberSelectionCheckBox(final JCheckBox rememberSelectionCheckBox) {
        rememberSelectionCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rememberSelection = rememberSelectionCheckBox.isSelected();
            }
        });
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            DisposerUtil.dispose(component);
            component = null;
            project = null;
            super.dispose();
        }
    }

    public boolean isDisposed() {
        return disposed;
    }
}
