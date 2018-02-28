package com.dci.intellij.dbn.data.grid.ui.table.basic;

import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import com.intellij.ide.IdeTooltip;
import com.intellij.ide.IdeTooltipManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.UIUtil;

import javax.swing.JLabel;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseWheelEvent;

public class BasicTableScrollPane extends JBScrollPane{
    @Override
    protected void processMouseWheelEvent(MouseWheelEvent e) {
        if (e.isControlDown()) {
            Component view = getViewport().getView();
            assert view instanceof BasicTable;

            BasicTable resultTable = (BasicTable) view;

            Project project = resultTable.getProject();
            DataGridSettings dataGridSettings = DataGridSettings.getInstance(project);
            if (dataGridSettings.getGeneralSettings().isZoomingEnabled()) {
                Font font = resultTable.getFont();
                float size = font.getSize() + e.getWheelRotation();
                if (size > 7 && size < 20) {
                    Font newFont = font.deriveFont(size);
                    resultTable.setFont(newFont);
                    float defaultSize = UIUtil.getLabelFont().getSize();
                    int percentage = (int) (size / defaultSize * 100);

                    IdeTooltip tooltip = new IdeTooltip(this, e.getPoint(), new JLabel(percentage + "%"));
                    tooltip.setFont(UIUtil.getLabelFont().deriveFont((float) 16));
                    IdeTooltipManager.getInstance().show(tooltip, true);
                }
            } else {
                super.processMouseWheelEvent(e);
            }

        } else{
            super.processMouseWheelEvent(e);
        }
    }
}
