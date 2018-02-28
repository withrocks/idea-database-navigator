package com.dci.intellij.dbn.execution.compiler.ui;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.object.common.DBSchemaObject;

public class CompilerTypeSelectionForm extends DBNFormImpl<CompilerTypeSelectionDialog> {
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JCheckBox rememberSelectionCheckBox;
    private JTextArea hintTextArea;

    public CompilerTypeSelectionForm(final CompilerTypeSelectionDialog parentComponent, @Nullable DBSchemaObject object) {
        super(parentComponent);
        if (object == null) {
            headerPanel.setVisible(false);
        } else {
            DBNHeaderForm headerForm = new DBNHeaderForm(object);
            headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
        }
        hintTextArea.setFont(mainPanel.getFont());
        hintTextArea.setBackground(mainPanel.getBackground());
        hintTextArea.setText(StringUtil.wrap(
                "The compile option type \"Debug\" enables you to use the selected object(s) in debugging activities (i.e. pause/trace execution). " +
                        "For runtime performance reasons, it is recommended to use normal compile option, unless you plan to debug the selected element(s)." +
                        "\"Keep current\" will carry over the existing compile type.\n\n" +
                        "Please select your compile option.", 80, ": ,."));

        parentComponent.registerRememberSelectionCheckBox(rememberSelectionCheckBox);
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public void dispose() {
        super.dispose();
    }
}
