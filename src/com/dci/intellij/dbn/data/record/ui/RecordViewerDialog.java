package com.dci.intellij.dbn.data.record.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.data.record.DatasetRecord;
import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
import com.intellij.openapi.project.Project;

public class RecordViewerDialog extends DBNDialog<RecordViewerForm> {
    private DatasetRecord record;

    public RecordViewerDialog(Project project, DatasetRecord record) {
        super(project, "View Record", true);
        this.record = record; 
        setModal(false);
        setResizable(true);
        component = new RecordViewerForm(this, record);
        getCancelAction().putValue(Action.NAME, "Close");
        init();
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return component.getPreferredFocusedComponent();
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                new OpenInEditorAction(),
                getCancelAction(),
                getHelpAction()
        };
    }
    
    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    private class OpenInEditorAction extends AbstractAction {
        public OpenInEditorAction() {
            super("Open In Editor", Icons.OBEJCT_EDIT_DATA);
        }

        public void actionPerformed(ActionEvent e) {
            DatasetEditorManager datasetEditorManager = DatasetEditorManager.getInstance(record.getDataset().getProject());
            datasetEditorManager.openDataEditor(record.getFilterInput());
            doCancelAction();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        record = null;
    }
}
