package com.dci.intellij.dbn.editor.session.ui.table;

import javax.swing.ListCellRenderer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableGutter;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableGutterCellRenderer;

public class SessionBrowserTableGutter extends BasicTableGutter<SessionBrowserTable> {
    public SessionBrowserTableGutter(SessionBrowserTable table) {
        super(table);
        addMouseListener(mouseListener);
    }

    @Override
    protected ListCellRenderer createCellRenderer() {
        return new BasicTableGutterCellRenderer();
    }

    MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
            }
        }
    };

    @Override
    public void dispose() {
        if (!isDisposed()) {
            removeMouseListener(mouseListener);
            mouseListener = null;
            super.dispose();
        }
    }
}
