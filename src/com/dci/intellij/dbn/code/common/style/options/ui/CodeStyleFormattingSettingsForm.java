package com.dci.intellij.dbn.code.common.style.options.ui;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleFormattingOption;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleFormattingSettings;
import com.dci.intellij.dbn.code.common.style.presets.CodeStylePreset;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

public class CodeStyleFormattingSettingsForm extends ConfigurationEditorForm<CodeStyleFormattingSettings> {
    private JPanel mainPanel;
    private JPanel settingsPanel;
    private JCheckBox enableCheckBox;
    private Map<CodeStyleFormattingOption, DBNComboBox<CodeStylePreset>> mappings = new HashMap<CodeStyleFormattingOption, DBNComboBox<CodeStylePreset>>();

    public CodeStyleFormattingSettingsForm(CodeStyleFormattingSettings settings) {
        super(settings);
        List<CodeStyleFormattingOption> options = settings.getOptions();
        settingsPanel.setLayout(new GridLayoutManager(options.size() + 1, 2, new Insets(4, 4, 4, 4), -1, -1));
        updateBorderTitleForeground(mainPanel);
        for (int i=0; i< options.size(); i++) {
            CodeStyleFormattingOption option = options.get(i);
            JLabel label = new JLabel(option.getDisplayName());
            settingsPanel.add(label,
                    new GridConstraints(i, 0, 1, 1,
                            GridConstraints.ANCHOR_WEST,
                            GridConstraints.FILL_NONE,
                            GridConstraints.SIZEPOLICY_FIXED,
                            GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

            DBNComboBox<CodeStylePreset> comboBox = new DBNComboBox<CodeStylePreset>(option.getPresets());
            label.setLabelFor(comboBox);

            settingsPanel.add(comboBox,
                    new GridConstraints(i, 1, 1, 1,
                            GridConstraints.ANCHOR_WEST,
                            GridConstraints.FILL_HORIZONTAL,
                            GridConstraints.SIZEPOLICY_CAN_GROW,
                            GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

            mappings.put(option, comboBox);
        }

        resetFormChanges();
        enableDisableOptions();
        
        settingsPanel.add(new Spacer(),
                new GridConstraints(options.size(), 1, 1, 1,
                        GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_VERTICAL, 1,
                        GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));

        registerComponent(mainPanel);
        enableCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableDisableOptions();
            }
        });

        //Shortcut[] basicShortcuts = KeyUtil.getShortcuts("ReformatCode");
        //useOnReformatCheckBox.setText("Use on reformat code (" + KeymapUtil.getShortcutsText(basicShortcuts) + ")");
    }

    private void enableDisableOptions() {
        boolean selected = enableCheckBox.isSelected();
        for (DBNComboBox<CodeStylePreset> optionComboBox : mappings.values()) {
            optionComboBox.setEnabled(selected);
        }
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        for (CodeStyleFormattingOption option : mappings.keySet()) {
            DBNComboBox<CodeStylePreset> comboBox = mappings.get(option);
            option.setPreset(comboBox.getSelectedValue());
        }
        getConfiguration().setEnabled(enableCheckBox.isSelected());
    }


    public void resetFormChanges() {
        for (CodeStyleFormattingOption option : mappings.keySet()) {
            DBNComboBox<CodeStylePreset> comboBox = mappings.get(option);
            comboBox.setSelectedValue(option.getPreset());
        }
        enableCheckBox.setSelected(getConfiguration().isEnabled());
    }
}
