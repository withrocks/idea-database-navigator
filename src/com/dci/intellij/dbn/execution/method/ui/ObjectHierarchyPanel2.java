package com.dci.intellij.dbn.execution.method.ui;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.common.DBObject;

public class ObjectHierarchyPanel2 extends JPanel {
    private DBObject object;

    public ObjectHierarchyPanel2(DBObject object) {
        super();
        this.object = object;
        this.setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        ConnectionHandler connectionHandler = FailsafeUtil.get(object.getConnectionHandler());
        JLabel connectionLabel = new JLabel(
                connectionHandler.getName(),
                connectionHandler.getIcon(),
                SwingConstants.LEFT);
        add(connectionLabel);
        add(panel, BorderLayout.SOUTH );

        List<DBObject> chain = new ArrayList<DBObject>();
        while (object != null) {
            chain.add(0, object);
            object = object.getParentObject();
        }

        for (int i=0; i<chain.size(); i++) {
            object = chain.get(i);
            if ( i > 0) panel.add(new JLabel(" > "));

            JLabel objectLabel = new JLabel(object.getName(), object.getIcon(), SwingConstants.LEFT);
            if (object == this.object) {
                Font font = objectLabel.getFont().deriveFont(Font.BOLD);
                objectLabel.setFont(font);
            }
            panel.add(objectLabel);
        }
    }
}